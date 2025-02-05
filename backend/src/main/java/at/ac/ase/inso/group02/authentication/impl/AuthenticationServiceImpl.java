package at.ac.ase.inso.group02.authentication.impl;

import at.ac.ase.inso.group02.admin.UserBanRepository;
import at.ac.ase.inso.group02.admin.exception.UserIsBannedException;
import at.ac.ase.inso.group02.authentication.AuthenticationService;
import at.ac.ase.inso.group02.authentication.RefreshTokenRepository;
import at.ac.ase.inso.group02.authentication.UserRepository;
import at.ac.ase.inso.group02.authentication.UserRoleRepository;
import at.ac.ase.inso.group02.authentication.dto.*;
import at.ac.ase.inso.group02.authentication.exception.*;
import at.ac.ase.inso.group02.entities.User;
import at.ac.ase.inso.group02.entities.UserRole;
import at.ac.ase.inso.group02.entities.UserState;
import at.ac.ase.inso.group02.entities.admin.UserBan;
import at.ac.ase.inso.group02.entities.auth.RefreshToken;
import at.ac.ase.inso.group02.entities.auth.VerificationToken;
import at.ac.ase.inso.group02.exceptions.UnauthenticatedException;
import at.ac.ase.inso.group02.mail.MailService;
import at.ac.ase.inso.group02.util.MapperUtil;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.logging.Log;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.smallrye.jwt.auth.principal.JWTAuthContextInfo;
import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.auth.principal.ParseException;
import io.smallrye.jwt.build.Jwt;
import io.smallrye.jwt.build.JwtClaimsBuilder;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.SneakyThrows;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.Claims;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.hibernate.validator.internal.constraintvalidators.bv.EmailValidator;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class AuthenticationServiceImpl implements AuthenticationService {
    private UserRepository userRepository;

    private UserRoleRepository userRoleRepository;

    private RefreshTokenRepository refreshTokenRepository;

    private MailService mailService;

    private UserBanRepository userBanRepository;

    private Validator validator;

    // used for checking if a String is an EMail
    private final EmailValidator EMAIL_VALIDATOR = new EmailValidator();

    private JWTParser jwtParser;

    private Instance<JsonWebToken> jwtInstance;

    // custom auth-context used to verify refresh-tokens (which do not have the usually required subject and upn)
    private JWTAuthContextInfo refreshTokenAuthContextInfo;

    private Random random;

    @ConfigProperty(name = "token.authtoken.expiry", defaultValue = "15")
    private Long authTokenExpiryMinutes;

    @ConfigProperty(name = "token.refreshtoken.expiry", defaultValue = "1440")
    private Long refreshTokenExpiryMinutes;

    @ConfigProperty(name = "token.verification.expiry", defaultValue = "1440")
    private Long verificationExpiryMinutes;

    public AuthenticationServiceImpl(UserRepository userRepository, UserRoleRepository userRoleRepository, RefreshTokenRepository refreshTokenRepository, MailService mailService, UserBanRepository userBanRepository, Validator validator, JWTParser jwtParser, Instance<JsonWebToken> jwtInstance, JWTAuthContextInfo authContextInfo) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.mailService = mailService;
        this.userBanRepository = userBanRepository;
        this.validator = validator;
        this.jwtParser = jwtParser;
        this.jwtInstance = jwtInstance;


        this.refreshTokenAuthContextInfo = new JWTAuthContextInfo(authContextInfo);
        this.userRoleRepository = userRoleRepository;
        this.refreshTokenAuthContextInfo.setRequireNamedPrincipal(false); // do not require a named principal in refresh tokens

        this.random = new Random();
    }


    @Override
    @Transactional
    public UserInfoDTO registerUser(UserRegistrationDTO userRegistrationDTO) {
        if (userRepository.findByEmail(userRegistrationDTO.getEmail()) != null) {
            throw new EMailInUseException("E-Mail already exists");
        }

        if (userRepository.findByUsername(userRegistrationDTO.getUsername()) != null) {
            throw new UsernameAlreadyExistsException("Username already exists");
        }

        // validate DTO constraints (if not already done in the Controller
        Set<ConstraintViolation<UserRegistrationDTO>> violations = validator.validate(userRegistrationDTO);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        // convert DTO to Entity
        User user = MapperUtil.map(userRegistrationDTO, User.class);

        // hash password
        user.setPassword(BcryptUtil.bcryptHash(userRegistrationDTO.getPassword()));

        // add default user role
        user.setRoles(getDefaultUserRoles());

        user.setVerificationToken(generateVerificationToken());

        mailService.sendAccountVerificationMail(user, user.getVerificationToken().getCode()).await().indefinitely();

        userRepository.persistUser(user);

        return MapperUtil.map(user, UserInfoDTO.class);
    }


    /**
     * @return the default roles a newly registered user should always be assigned
     */
    private Set<UserRole> getDefaultUserRoles() {
        Set<UserRole> userRoles = new HashSet<>();
        Set<String> defaultUserRoles = Set.of("USER");

        QuarkusTransaction.requiringNew()
                .run(() -> defaultUserRoles.forEach(roleString -> {
                    Optional<UserRole> userRole = userRoleRepository.find("role", roleString).firstResultOptional();
                    userRole.ifPresentOrElse(
                            userRoles::add,
                            () -> {
                                UserRole roleToAdd = UserRole.builder()
                                        .role(roleString)
                                        .build();
                                userRoleRepository.persist(roleToAdd);
                                userRoles.add(roleToAdd);
                            }
                    );
                }));

        return userRoles;
    }

    @Override
    @Transactional
    public LoginResponseDTO loginUser(UserLoginDTO userLoginDTO) throws ConstraintViolationException {

        // get user by email or username
        User user;
        if (isEmail(userLoginDTO.getEmailOrUsername())) {
            user = userRepository.findByEmail(userLoginDTO.getEmailOrUsername());
        } else {
            user = userRepository.findByUsername(userLoginDTO.getEmailOrUsername());
        }

        if (user == null) {
            throw new InvalidCredentialsException("Invalid credentials: Email/Username does not exist: " + userLoginDTO.getEmailOrUsername());
        }

        UserBan ban = userBanRepository.findByUserId(user.getId());
        if (ban != null) {
            throw new UserIsBannedException("The user " + userLoginDTO.getEmailOrUsername() + " has been banned. Reason: " + ban.getReason());
        }

        // check password matches
        if (!BcryptUtil.matches(userLoginDTO.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials: Incorrect Password attempt: " + userLoginDTO);
        }

        // check email confirmation state
        if (user.getState() == UserState.NEEDS_EMAIL_CONFIRM) {
            throw new EMailNotConfirmedException("Confirm your email before logging in");
        }

        LoginResponseDTO resp = getJWTForUser(user, false);

        user.setFirstLogin(false);

        return resp;
    }

    /**
     * generates JWT auth and refresh tokens for the given user, knowing that whatever was used to authenticate user is correct
     * This could have been username/password or a refresh-token.
     * Also saves the refresh-token
     *
     * @param user          an authenticated user
     * @param passwordReset boolean indicating if JWT token will be used for resetting password
     * @return JWT auth and refresh tokens
     */
    private LoginResponseDTO getJWTForUser(User user, boolean passwordReset) {
        // set user state to active
        user.setState(UserState.ACTIVE);
        userRepository.persistUser(user);

        String jwt = generateJWTForUser(user, passwordReset);

        String refreshToken;
        RefreshToken refreshTokenEntity;

        do {
            UUID tokenUUID;
            // generate a refresh token that is not yet contained in the DB
            do {
                tokenUUID = UUID.randomUUID();
            } while (refreshTokenRepository.findByTokenUUID(tokenUUID) != null);

            Instant expiration = Instant.now().plusSeconds(refreshTokenExpiryMinutes * 60).truncatedTo(ChronoUnit.SECONDS);

            if (passwordReset) {
                expiration = Instant.now().plusSeconds(900L).truncatedTo(ChronoUnit.SECONDS);
            }


            refreshToken = generateRefreshTokenForUser(user, tokenUUID, expiration);
            refreshTokenEntity = RefreshToken.builder()
                    .uuid(tokenUUID)
                    .user(user)
                    .expiration(expiration)
                    .build();
        } while (!refreshTokenRepository.saveNew(refreshTokenEntity));

        return LoginResponseDTO.builder()
                .jwt(jwt)
                .refreshToken(refreshToken)
                .firstLogin(user.getFirstLogin())
                .build();
    }

    @Override
    @Transactional
    public LoginResponseDTO refreshLogin(TokenRefreshDTO tokenRefreshDTO) throws InvalidRefreshTokenException {

        // process the refresh-token
        JsonWebToken jsonWebToken;
        try {
            jsonWebToken = jwtParser.parse(tokenRefreshDTO.getRefreshToken(), this.refreshTokenAuthContextInfo);
        } catch (ParseException e) {
            throw new InvalidRefreshTokenException("Invalid refresh token: parse failed", e);
        }

        // extract the individual claims
        String type = jsonWebToken.getClaim("type");
        String username = jsonWebToken.getClaim("userName");
        String userEmail = jsonWebToken.getClaim("userEmail");
        String tokenUUID = jsonWebToken.getClaim("tokenUUID");
        Instant expiration = Instant.ofEpochSecond(jsonWebToken.getExpirationTime());

        if (!"refresh".equals(type) || username == null || userEmail == null || tokenUUID == null) {
            throw new InvalidRefreshTokenException("Invalid refresh token, invalid claims format");
        }

        // fetch the corresponding refresh token from the DB
        RefreshToken refreshToken = refreshTokenRepository.findByTokenUUID(UUID.fromString(tokenUUID));

        // validate the token from the DB
        if (refreshToken == null ||
                refreshToken.getExpiration().isBefore(Instant.now()) ||
                refreshToken.getUser() == null) {
            throw new InvalidRefreshTokenException("Invalid refresh token, token not found in token-store or expired");
        }

        User user = refreshToken.getUser();

        // validate the received token against the one from the DB
        if (!user.getEmail().equals(userEmail) ||
                !user.getUsername().equals(username) ||
                !refreshToken.getExpiration().equals(expiration)) {
            throw new InvalidRefreshTokenException("Invalid refresh token, claims do not correspond to stored claims");
        }

        // delete the old token
        if (!refreshTokenRepository.remove(refreshToken)) {
            throw new InvalidRefreshTokenException("Invalid refresh token: could not remove old token");
        }

        // return a fresh JWT token for the user
        return getJWTForUser(user, false);
    }

    @Override
    @Transactional
    public LoginResponseDTO verifyUser(UserVerificationDTO verificationDTO) throws VerificationTokenExpiredException, WrongVerificationTokenException, AlreadyVerifiedException {
        User user = userRepository.findByEmail(verificationDTO.getEmail());
        if (user == null) {
            throw new InvalidCredentialsException("Invalid credentials: No user with email: " + verificationDTO.getEmail());
        }
        VerificationToken storedToken = user.getVerificationToken();

        if (storedToken == null || storedToken.getCode() == null || storedToken.getExpiration() == null) {
            if (user.getState() != UserState.NEEDS_EMAIL_CONFIRM) {
                throw new AlreadyVerifiedException("user with this e-mail is already verified");
            } else {
                regenerateToken(user);
                throw new VerificationTokenExpiredException("No existing token, sending new one");
            }
        }


        if (!storedToken.getExpiration().isAfter(Instant.now())) {
            regenerateToken(user);
            throw new VerificationTokenExpiredException("Token already expired, sending a new one");
        }

        if (!storedToken.getCode().equals(verificationDTO.getVerificationToken())) {
            regenerateToken(user);
            throw new WrongVerificationTokenException(storedToken.getCode() + " != " + verificationDTO.getVerificationToken());
        }

        user.setState(UserState.ACTIVE);
        user.setVerificationToken(null);
        userRepository.persistUser(user);


        LoginResponseDTO resp = getJWTForUser(user, false);
        return resp;
    }


    private void regenerateToken(User userToUpdate) {
        VerificationToken newToken = generateVerificationToken();
        QuarkusTransaction.requiringNew()
                .run(() -> {
                    User user = userRepository.findByEmail(userToUpdate.getEmail());
                    user.setVerificationToken(newToken);
                    userRepository.persist(user);
                    userRepository.flush();
                    // this.verificationTokenRepository;
                });
        User updatedUser = userRepository.findByEmail(userToUpdate.getEmail());
        mailService.sendAccountVerificationMail(updatedUser, newToken.getCode()).await().indefinitely();
    }


    private VerificationToken generateVerificationToken() {
        return VerificationToken.builder()
                .code(generateVerificationCode())
                .expiration(Instant.now().plusSeconds(verificationExpiryMinutes * 60))
                .build();
    }

    private String generateVerificationCode() {
        return String.format("%06d", random.nextInt(1_000_000));
    }


    /**
     * @param input a string that could be either a username or email
     * @return true if input looks like an email, false otherwise
     */
    private boolean isEmail(String input) {
//        // https://emailregex.com
//        String emailRegex = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])";
//        return input.matches(emailRegex);

        return EMAIL_VALIDATOR.isValid(input, null);
    }


    /**
     * generates a JWT token for the given user
     *
     * @param user a user, not null
     * @return a JWT token that can be used to authenticate user
     */
    @SneakyThrows
    private String generateJWTForUser(User user, boolean passwordReset) {
        JwtClaimsBuilder jwt = Jwt
                .claim("type", "access")
                .upn(user.getEmail())
                .subject(user.getUsername());

        if (user.getDisplayName() != null && !user.getDisplayName().isBlank()) {
            jwt = jwt.claim(Claims.full_name, user.getDisplayName());
        }

        // add user groups
        if (passwordReset) {
            jwt = jwt.groups("PASSWORD-RESET");
        } else {
            jwt = jwt.groups(user.getRoles().stream().map(UserRole::getRole).collect(Collectors.toSet()));
        }

        // add expiry of (default) 15 minutes
        jwt = jwt.expiresIn(authTokenExpiryMinutes * 60);

        return jwt.sign();

        // Encryption of the JWT needs additional work, if required
        // return jwt.innerSign().encrypt();
    }

    /**
     * generates a JWT refresh token
     *
     * @param user       the user that this token is valid for
     * @param tokenUUID  the uuid that should be assigned to this token
     * @param expiration when the refresh-token should expire
     * @return a JWT refresh token that can be used to obtain a new JWT token
     */
    @SneakyThrows
    private String generateRefreshTokenForUser(User user, UUID tokenUUID, Instant expiration) {
        JwtClaimsBuilder jwt = Jwt
                .claim("type", "refresh")
                .claim("userEmail", user.getEmail()) // use custom claim names to keep jwt unsuitable for authentication
                .claim("userName", user.getUsername())
                .claim("tokenUUID", tokenUUID.toString());

        jwt = jwt.expiresAt(expiration);

        return jwt.sign();
        // Encryption of the JWT needs additional work, if required
        // return jwt.innerSign().encrypt();
    }

    @Transactional
    @Override
    public void sendPasswordResetEmail(PasswordResetRequestDTO passwordResetRequestDto) {
        String email = passwordResetRequestDto.getEmail();
        User user = userRepository.findByEmail(email);
        if (user != null) {
            LoginResponseDTO loginCredentials = this.getJWTForUser(user, true);
            this.mailService.sendPasswordResetMail(email, "/reset-password" + "?jwt=" + loginCredentials.getJwt().toString() + "&resetToken=" + loginCredentials.getRefreshToken().toString());
        }
        // Security :  Do not send error messages if user is not found
    }

    @Transactional
    @Override
    public UserInfoDTO resetPassword(TokenRefreshDTO resetTokenDTO, PasswordResetDTO passwordResetDTO) {
        LoginResponseDTO loginCredentials = this.refreshLogin(resetTokenDTO);

        JsonWebToken jsonWebToken;
        try {
            jsonWebToken = jwtParser.parse(loginCredentials.getRefreshToken(), this.refreshTokenAuthContextInfo);
        } catch (ParseException e) {
            throw new InvalidRefreshTokenException("Invalid reset token: parse failed", e);
        }
        String tokenUUID = jsonWebToken.getClaim("tokenUUID");
        String userEmail = jsonWebToken.getClaim("userEmail");
        RefreshToken validationRefreshToken = refreshTokenRepository.findByTokenUUID(UUID.fromString(tokenUUID));
        refreshTokenRepository.remove(validationRefreshToken);


        User user = userRepository.findByEmail(userEmail);
        user.setPassword(BcryptUtil.bcryptHash(passwordResetDTO.getPassword()));

        userRepository.persistUser(user);
        return MapperUtil.map(user, UserInfoDTO.class);
    }


    public User getCurrentUser() {
        User user = userRepository.findByUsername(getCurrentUsername());

        if (user == null) {
            throw new UnauthenticatedException("Valid JWT but user does not exist anymore");
        }
        return user;
    }

    public String getCurrentUsername() {
        return jwtInstance.get().getSubject();
    }
}
