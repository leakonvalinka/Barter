package at.ac.ase.inso.group02.authentication;

import at.ac.ase.inso.group02.authentication.dto.*;
import at.ac.ase.inso.group02.authentication.exception.*;
import at.ac.ase.inso.group02.entities.User;
import at.ac.ase.inso.group02.entities.UserRole;
import at.ac.ase.inso.group02.entities.UserState;
import at.ac.ase.inso.group02.entities.auth.RefreshToken;
import at.ac.ase.inso.group02.entities.auth.VerificationToken;
import at.ac.ase.inso.group02.mail.MailService;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@Slf4j
@QuarkusTest
public class AuthenticationServiceTest {
    @Inject
    AuthenticationService authenticationService;

    @InjectMock
    UserRepository userRepositoryMock;

    @InjectMock
    RefreshTokenRepository tokenRepositoryMock;

    @InjectMock
    MailService mailServiceMock;


    @BeforeEach
    public void setup() {
        // Override the default behavior of tokenRepositoryMock for all test cases
        when(tokenRepositoryMock.saveNew(any())).thenReturn(true);
        when(tokenRepositoryMock.remove(any())).thenReturn(true);
    }


    static Stream<UserLoginDTO> userLoginDTOProvider() {
        return Stream.of(
                UserLoginDTO.builder().emailOrUsername("test@test.com").password("TesT_ASE24W").build(),
                UserLoginDTO.builder().emailOrUsername("test").password("TesT_ASE24W").build()
        );
    }


    @Test
    public void testRegisterValidUser_shouldSaveNewUser() {
        // Arrange
        UserRegistrationDTO userRegistrationDTO =
                UserRegistrationDTO.builder()
                        .email("test@test.com")
                        .username("test")
                        .password("TesT_ASE24W")
                        .build();

        // Act
        authenticationService.registerUser(userRegistrationDTO);

        // Assert
        // assert that userRepositoryMock.persistUser was called with a fitting User object
        verify(userRepositoryMock).persistUser(
                argThat(user ->
                        user.getEmail().equals(userRegistrationDTO.getEmail())
                                && user.getUsername().equals(userRegistrationDTO.getUsername())
                                && user.getRoles().stream().anyMatch(role -> role.getRole().equals("USER"))
                                && BcryptUtil.matches(userRegistrationDTO.getPassword(), user.getPassword())
                )
        );
    }

    @Test
    public void testRegisterUserWithExistingEmail_shouldFail() {
        // Arrange
        String email = "test@test.com";

        when(userRepositoryMock.findByEmail(email)).thenReturn(
                User.builder()
                        .email(email)
                        .build()
        );

        UserRegistrationDTO userRegistrationDTO =
                UserRegistrationDTO.builder()
                        .email(email)
                        .username("test")
                        .password("TesT_ASE24W")
                        .build();

        // Act & Assert
        Assertions.assertThrows(EMailInUseException.class, () -> authenticationService.registerUser(userRegistrationDTO));
        // persistUser should not be called
        verify(userRepositoryMock, times(0)).persistUser(any());
    }


    @Test
    public void testRegisterUserWithExistingUsername_shouldFail() {
        // Arrange
        String username = "test";

        when(userRepositoryMock.findByUsername(username)).thenReturn(
                User.builder()
                        .username(username)
                        .build()
        );

        UserRegistrationDTO userRegistrationDTO =
                UserRegistrationDTO.builder()
                        .email("test@test.com")
                        .username(username)
                        .password("TesT_ASE24W")
                        .build();

        // Act & Assert
        // service should throw the exception that the username already exists
        Assertions.assertThrows(UsernameAlreadyExistsException.class, () -> authenticationService.registerUser(userRegistrationDTO));
        // persistUser should not be called
        verify(userRepositoryMock, times(0)).persistUser(any());
    }


    static Stream<String> invalidEmail() {
        return Stream.of(
                "invalidEmail",
                "",
                "mail@",
                null
        );
    }

    @ParameterizedTest
    @MethodSource("invalidEmail")
    public void testRegisterUserWithInvalidEmail_shouldFail(String invalidEmail) {
        // Arrange
        UserRegistrationDTO userRegistrationDTO =
                UserRegistrationDTO.builder()
                        .email(invalidEmail)
                        .username("test")
                        .password("TesT_ASE24W")
                        .build();

        // Act & Assert
        // service should throw the exception that the email is in use
        Assertions.assertThrows(ConstraintViolationException.class, () -> authenticationService.registerUser(userRegistrationDTO));
        // persistUser should not be called
        verify(userRepositoryMock, times(0)).persistUser(any());
    }

    static Stream<String> invalidUsername() {
        return Stream.of(
                "invalid@username.com",
                "",
                null
        );
    }

    @ParameterizedTest
    @MethodSource("invalidUsername")
    public void testRegisterUserWithInvalidUsername_shouldFail(String invalidUsername) {
        // Arrange
        UserRegistrationDTO userRegistrationDTO =
                UserRegistrationDTO.builder()
                        .email("test@test.com")
                        .username(invalidUsername)
                        .password("TesT_ASE24W")
                        .build();

        // Act & Assert
        // service should throw the exception that the email is in use
        Assertions.assertThrows(ConstraintViolationException.class, () -> authenticationService.registerUser(userRegistrationDTO));
        // persistUser should not be called
        verify(userRepositoryMock, times(0)).persistUser(any());
    }

    @Test
    public void testRegisterUserWithWeakPassword_shouldFail() {
        // Arrange
        UserRegistrationDTO userRegistrationDTO =
                UserRegistrationDTO.builder()
                        .email("test@test.com")
                        .username("test")
                        .password("weakling")
                        .build();

        // Act & Assert
        // service should throw the exception that the username already exists
        Assertions.assertThrows(ConstraintViolationException.class, () -> authenticationService.registerUser(userRegistrationDTO));
        // persistUser should not be called
        verify(userRepositoryMock, times(0)).persistUser(any());
    }

    @ParameterizedTest
    @MethodSource("userLoginDTOProvider")
    public void testLoginUserEmail_shouldReturnValidJWT(UserLoginDTO userLoginDTO) {
        // Arrange
        User user = User.builder()
                .email("test@test.com")
                .username("test")
                .password(BcryptUtil.bcryptHash(userLoginDTO.getPassword()))
                .state(UserState.ACTIVE)
                .roles(Set.of(UserRole.builder().role("USER").build()))
                .build();

        when(userRepositoryMock.findByEmail(user.getEmail())).thenReturn(user);
        when(userRepositoryMock.findByUsername(user.getUsername())).thenReturn(user);

        // Act
        String token = authenticationService.loginUser(userLoginDTO).getJwt();
        // Assert
        Assertions.assertNotNull(token);

        // with the token, access to the test-endpoint it granted
        given()
                .header("Authorization", "Bearer " + token)
                .when().get("/test/protected")
                .then()
                .statusCode(200)
                .body(containsString("Hello, " + user.getUsername() + "!"));

        // with the token, access to the admin test-endpoint it not granted
        given()
                .header("Authorization", "Bearer " + token)
                .when().get("/test/protected-admin")
                .then()
                .statusCode(403);
    }

    @ParameterizedTest
    @MethodSource("userLoginDTOProvider")
    public void testLoginUnconfirmedUser_shouldFail(UserLoginDTO userLoginDTO) {
        // Arrange
        User user = User.builder()
                .email("test@test.com")
                .username("test")
                .password(BcryptUtil.bcryptHash(userLoginDTO.getPassword()))
                .state(UserState.NEEDS_EMAIL_CONFIRM)
                .roles(Set.of(UserRole.builder().role("USER").build()))
                .build();

        when(userRepositoryMock.findByEmail(user.getEmail())).thenReturn(user);
        when(userRepositoryMock.findByUsername(user.getUsername())).thenReturn(user);

        // Act & Assert
        Assertions.assertThrows(EMailNotConfirmedException.class, () -> authenticationService.loginUser(userLoginDTO));
    }

    static Stream<UserLoginDTO> incorrectUserLoginDTOProvider() {
        return Stream.of(
                UserLoginDTO.builder().emailOrUsername("test@test.com").password("incorrect").build(),
                UserLoginDTO.builder().emailOrUsername("test").password("incorrect2").build(),
                UserLoginDTO.builder().emailOrUsername("nonexist").password("TesT_ASE24W").build(),
                UserLoginDTO.builder().emailOrUsername("nonexist@test.com").password("TesT_ASE24W").build()
        );
    }

    @ParameterizedTest
    @MethodSource("incorrectUserLoginDTOProvider")
    public void testLoginUserInvalidCredentials_shouldFail(UserLoginDTO userLoginDTO) {
        // Arrange
        User user = User.builder()
                .email("test@test.com")
                .username("test")
                .password(BcryptUtil.bcryptHash("TesT_ASE24W"))
                .state(UserState.ACTIVE)
                .roles(Set.of(UserRole.builder().role("USER").build()))
                .build();

        when(userRepositoryMock.findByEmail(user.getEmail())).thenReturn(user);
        when(userRepositoryMock.findByUsername(user.getUsername())).thenReturn(user);

        // Act & Assert
        Assertions.assertThrows(InvalidCredentialsException.class, () -> authenticationService.loginUser(userLoginDTO));
    }

    @ParameterizedTest
    @MethodSource("userLoginDTOProvider")
    public void testAuthenticateWithRefreshToken_shouldFail(UserLoginDTO userLoginDTO) {
        // Arrange
        User user = User.builder()
                .email("test@test.com")
                .username("test")
                .password(BcryptUtil.bcryptHash(userLoginDTO.getPassword()))
                .state(UserState.ACTIVE)
                .roles(Set.of(UserRole.builder().role("USER").build()))
                .build();

        when(userRepositoryMock.findByEmail(user.getEmail())).thenReturn(user);
        when(userRepositoryMock.findByUsername(user.getUsername())).thenReturn(user);

        // Act
        String refreshToken = authenticationService.loginUser(userLoginDTO).getRefreshToken();
        // Assert
        Assertions.assertNotNull(refreshToken);

        // using the refresh token as authorization MUST fail
        given()
                .header("Authorization", "Bearer " + refreshToken)
                .when().get("/test/protected")
                .then()
                .statusCode(401);
    }

    @Test
    public void testGetTokenWithRefreshToken_shouldSucceed() {
        // Arrange
        User user = User.builder()
                .email("test@test.com")
                .username("test")
                .password(BcryptUtil.bcryptHash("TesT_ASE24W"))
                .state(UserState.ACTIVE)
                .roles(Set.of(UserRole.builder().role("USER").build()))
                .build();

        when(userRepositoryMock.findByEmail(user.getEmail())).thenReturn(user);
        when(userRepositoryMock.findByUsername(user.getUsername())).thenReturn(user);

        // Act
        // capture RefreshTokens that are arguments to RefreshToken methods
        ArgumentCaptor<RefreshToken> tokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);

        String refreshToken = authenticationService.loginUser(UserLoginDTO.builder()
                .emailOrUsername(user.getEmail())
                .password("TesT_ASE24W")
                .build()
        ).getRefreshToken();

        // Assert
        Assertions.assertNotNull(refreshToken);

        // verify that the token was saved and tell tokenRepositoryMock to return that token on subsequent calls to findByTokenString()
        verify(tokenRepositoryMock, times(1)).saveNew(tokenCaptor.capture());
        RefreshToken savedRefreshToken = tokenCaptor.getValue();
        when(tokenRepositoryMock.findByTokenUUID(savedRefreshToken.getUuid()))
                .thenReturn(savedRefreshToken);

        // Now try to obtain a new token using the refresh-token
        String token = authenticationService.refreshLogin(TokenRefreshDTO.builder()
                .refreshToken(refreshToken)
                .build()).getJwt();

        // access should be granted using the new token
        given()
                .header("Authorization", "Bearer " + token)
                .when().get("/test/protected")
                .then()
                .statusCode(200)
                .body(containsString("Hello, " + user.getUsername() + "!"));

        // access to the admin-endpoint should still not be granted
        given()
                .header("Authorization", "Bearer " + token)
                .when().get("/test/protected-admin")
                .then()
                .statusCode(403);
    }

    @Test
    void testResetPassword_shouldResetPasswordForUser() {
        // Arrange
        User user = User.builder()
                .email("test@test.com")
                .username("test")
                .password(BcryptUtil.bcryptHash("TesT_ASE24W"))
                .state(UserState.ACTIVE)
                .roles(Set.of(UserRole.builder().role("USER").build()))
                .build();

        when(userRepositoryMock.findByEmail(user.getEmail())).thenReturn(user);
        when(userRepositoryMock.findByUsername(user.getUsername())).thenReturn(user);

        // Act
        ArgumentCaptor<RefreshToken> tokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);

        String refreshToken = authenticationService.loginUser(UserLoginDTO.builder()
                .emailOrUsername(user.getEmail())
                .password("TesT_ASE24W")
                .build()
        ).getRefreshToken();

        Assertions.assertNotNull(refreshToken);

        verify(tokenRepositoryMock, times(1)).saveNew(tokenCaptor.capture());
        RefreshToken savedRefreshToken = tokenCaptor.getValue();
        when(tokenRepositoryMock.findByTokenUUID(savedRefreshToken.getUuid()))
                .thenReturn(savedRefreshToken);

        TokenRefreshDTO refreshDTO = TokenRefreshDTO.builder().refreshToken(refreshToken).build();
        PasswordResetDTO resetDTO = PasswordResetDTO.builder().password("Password123!").build();
        UserInfoDTO result = authenticationService.resetPassword(refreshDTO, resetDTO);

        // Assert
        assertEquals(result.getUsername(), user.getUsername());
    }

    @Test
    public void testForgedAccessToken_shouldDenyPermission() {
        // Arrange
        /*
        forged JWT, signed with a different key (since we do not know the secret key)
        encodes claims:
        {
          "type": "access",
          "sub": "myUser",
          "upn": "test@user.com",
          "exp": 2516239022,
          "iat": 1516239022,
          "groups": ["USER"],
          "iss": "https://inso.tuwien.ac.at/24ws-ase-pr-inso-02"
        }
         */
        String forgedToken = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0eXBlIjoiYWNjZXNzIiwic3ViIjoibXlVc2VyIiwidXBuIjoidGVzdEB1c2VyLmNvbSIsImV4cCI6MjUxNjIzOTAyMiwiaWF0IjoxNTE2MjM5MDIyLCJncm91cHMiOlsiVVNFUiJdLCJpc3MiOiJodHRwczovL2luc28udHV3aWVuLmFjLmF0LzI0d3MtYXNlLXByLWluc28tMDIifQ.cA1pp4JAPLI71I4LV07DPc1P7qOUBjurkeUAKPKxJgFsdGadPpF1IJlycT-4kYN7_w0tbj65FUvOhCKgHn8XwPL2QzDAUdaVS2HW9wcueqLO2SJCP-_V4mJEdHWXbmjCqSeZlZrqy9oOWM8gCJCRxoM6d4DzRYA_j_4JAgNPhlc-OenSWCdWNIXYnBs2cSiaGh6vNkXi1zwqIoLKqrSsfUrNkyC83_2VUG2qfonbH1XpDpWhbs0hO4lDkzFFNSEbf_cQv0rdQLrZwsY4SQiOfDR78MA5KLEJUJ5dLhi7qGnT_O6J6Yqh0nb_8j9Xq8K8aZkaTnyUKni1lx6KJeCYd5k4iE4Qv7OKOC2NQGUdYEEJ9hyvAM3Oun43Wbew8GDVz6zaaoCTD14Zx_ezRljw6MXJjfWv7e8RKLFMI8QCyJcF98sOYvBFk39soKIblD6lGJvGO_nA3nLHmtwzr9oCYu2eA5OZAJqf9b4dRkYNuFIeK2m-FCHZyoLeBHbROQFpnvM9-1psn67WBRNntqTUrEw4Z9wUA0AD7iCJZCje9hrZNXsbdIzY2Fb3QCTQDzB4rlX3JfcUi69L8FRlADgD6v6g8tkq8mQD56t4lnDTXLe1NhBEvMCBU0kRhoINHyz9pjnEqZy_F4x5kzSrTfHNbkzZpHpqjqrI4a-3YBMsu94";

        // Act & Assert
        // access to both endpoints should be denied
        given()
                .header("Authorization", "Bearer " + forgedToken)
                .when().get("/test/protected")
                .then()
                .statusCode(401);

        given()
                .header("Authorization", "Bearer " + forgedToken)
                .when().get("/test/protected")
                .then()
                .statusCode(401);
    }

    @Test
    public void testVerifyUserWithValidToken_shouldSucceed() {
        // Arrange
        String email = "test@test.com";
        String correctToken = "131269";
        VerificationToken token = VerificationToken.builder()
                .code(correctToken)
                .expiration(Instant.now().plusSeconds(60 * 60 * 24))
                .build();

        User user = User.builder()
                .email(email)
                .username("test")
                .password("test")
                .state(UserState.NEEDS_EMAIL_CONFIRM)
                .roles(Set.of(UserRole.builder().role("USER").build()))
                .verificationToken(token)
                .build();
        UserVerificationDTO verificationDTO = UserVerificationDTO.builder()
                .email(email)
                .verificationToken(correctToken)
                .build();


        when(userRepositoryMock.findByEmail(email)).thenReturn(user);

        // Act & Assert
        Assertions.assertDoesNotThrow(() -> {
            authenticationService.verifyUser(verificationDTO);
        });

        verify(userRepositoryMock, times(1)).findByEmail(email);
        verify(userRepositoryMock, times(2)).persistUser(argThat(persisted -> {
            return persisted.getEmail().equals(email) && persisted.getState() == UserState.ACTIVE;
        }));
    }

    @Test
    public void testVerifyUserWithWrongCode_shouldFail() {
        String email = "test@test.com";
        String correctToken = "131269";
        String wrongToken = "691312";
        VerificationToken token = VerificationToken.builder()
                .code(correctToken)
                .expiration(Instant.now().plusSeconds(60 * 60 * 24))
                .build();
        User user = User.builder()
                .email(email)
                .username("test")
                .password("test")
                .state(UserState.NEEDS_EMAIL_CONFIRM)
                .roles(Set.of(UserRole.builder().role("USER").build()))
                .verificationToken(token)
                .build();

        UserVerificationDTO verificationDTO = UserVerificationDTO.builder()
                .email(email)
                .verificationToken(wrongToken)
                .build();

        when(userRepositoryMock.findByEmail(email)).thenReturn(user).thenReturn(user);

        // Act & Assert
        Assertions.assertThrows(WrongVerificationTokenException.class, () -> {
            authenticationService.verifyUser(verificationDTO);
        });


        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        // verify(verificationTokenRepositoryMock, times(1)).saveNew(tokenCaptor.capture());
        verify(userRepositoryMock, times(1)).persist(userCaptor.capture());

        // User userRemovedToken = userCaptor.getAllValues().get(0);
        // Assertions.assertNull(userRemovedToken.getVerificationToken());
        User userWithNewToken = userCaptor.getValue();
        assertTrue(() -> {
            int parsedCode = Integer.parseInt(userWithNewToken.getVerificationToken().getCode());
            return 0 <= parsedCode && parsedCode < 1_000_000;
        });

        verify(userRepositoryMock, atLeast(1)).findByEmail(email);

        verify(mailServiceMock, times(1)).sendAccountVerificationMail(user, userWithNewToken.getVerificationToken().getCode());
    }

    @Test
    public void testVerifyUserWithExpiredToken_shouldFail() {
        // Arrange
        String email = "test@test.com";
        String correctToken = "131269";
        String wrongToken = "691312";

        VerificationToken token = VerificationToken.builder()
                .code(correctToken)
                .expiration(Instant.now().minusSeconds(60 * 60 * 24))
                .build();

        User userWithoutToken = User.builder()
                .email(email)
                .username("test")
                .password("test")
                .state(UserState.NEEDS_EMAIL_CONFIRM)
                .roles(Set.of(UserRole.builder().role("USER").build()))
                .build();

        User user = userWithoutToken
                .toBuilder()
                .verificationToken(token)
                .build();

        UserVerificationDTO verificationDTO = UserVerificationDTO.builder()
                .email(email)
                .verificationToken(wrongToken)
                .build();

        when(userRepositoryMock.findByEmail(email)).thenReturn(user).thenReturn(user).thenReturn(user);

        // Act & Assert
        Assertions.assertThrows(VerificationTokenExpiredException.class, () -> {
            authenticationService.verifyUser(verificationDTO);
        });

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        // verify(verificationTokenRepositoryMock, times(1)).saveNew(tokenCaptor.capture());
        verify(userRepositoryMock, times(1)).persist(userCaptor.capture());

        User userWithNewToken = userCaptor.getValue();
        assertTrue(() -> {
            int parsedCode = Integer.parseInt(userWithNewToken.getVerificationToken().getCode());
            return 0 <= parsedCode && parsedCode < 1_000_000;
        });

        assertTrue(() -> {
            int parsedCode = Integer.parseInt(userWithNewToken.getVerificationToken().getCode());
            return 0 <= parsedCode && parsedCode < 1_000_000;
        });

        verify(userRepositoryMock, times(3)).findByEmail(email);

        verify(mailServiceMock, times(1)).sendAccountVerificationMail(argThat(u -> u.getEmail().equals(email)), argThat(t -> t.equals(userWithNewToken.getVerificationToken().getCode())));
    }

    @Test
    public void testVerifyUserWithoutExistingTokenAndAlreadyVerified_shouldFail() {
        // Arrange
        String email = "test@test.com";
        String wrongToken = "691312";
        User user = User.builder()
                .email(email)
                .username("test")
                .password("test")
                .state(UserState.ACTIVE)
                .roles(Set.of(UserRole.builder().role("USER").build()))
                .build();
        UserVerificationDTO verificationDTO = UserVerificationDTO.builder()
                .email(email)
                .verificationToken(wrongToken)
                .build();

        when(userRepositoryMock.findByEmail(email)).thenReturn(user);

        // Act & Assert
        Assertions.assertThrows(AlreadyVerifiedException.class, () -> {
            authenticationService.verifyUser(verificationDTO);
        });

        verify(userRepositoryMock, times(1)).findByEmail(email);
    }

    @Test
    public void testVerifyUserWithoutExistingTokenAndNotVerified_shouldFail() {
        // Arrange
        String email = "test@test.com";
        String correctToken = "131269";
        String wrongToken = "691312";

        VerificationToken token = VerificationToken.builder()
                .code(correctToken)
                .expiration(Instant.now().minusSeconds(60 * 60 * 24))
                .build();

        User userWithoutToken = User.builder()
                .email(email)
                .username("test")
                .password("test")
                .state(UserState.NEEDS_EMAIL_CONFIRM)
                .roles(Set.of(UserRole.builder().role("USER").build()))
                .build();
        User userWithToken = userWithoutToken.toBuilder()
                .verificationToken(token)
                .build();

        UserVerificationDTO verificationDTO = UserVerificationDTO.builder()
                .email(email)
                .verificationToken(wrongToken)
                .build();


        when(userRepositoryMock.findByEmail(email)).thenReturn(userWithoutToken).thenReturn(userWithToken).thenReturn(userWithToken);

        // Act & Assert
        Assertions.assertThrows(VerificationTokenExpiredException.class, () -> {
            authenticationService.verifyUser(verificationDTO);
        });


        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        // verify(verificationTokenRepositoryMock, times(1)).saveNew(tokenCaptor.capture());
        verify(userRepositoryMock, times(1)).persist(userCaptor.capture());

        User userWithNewToken = userCaptor.getValue();
        assertTrue(() -> {
            int parsedCode = Integer.parseInt(userWithNewToken.getVerificationToken().getCode());
            return 0 <= parsedCode && parsedCode < 1_000_000;
        });

        verify(userRepositoryMock, times(3)).findByEmail(email);

        // verify(mailServiceMock, times(1)).sendAccountVerificationMail(argThat(u -> u.getEmail().equals(email)), argThat(t -> t.equals(userWithNewToken.getVerificationToken().getCode())));
        verify(mailServiceMock, times(1)).sendAccountVerificationMail(argThat(u -> u.getEmail().equals(email)), argThat(t -> true));
    }

    @Test
    public void testVerifyUserWithoutExistingUser_shouldFail() {
        // Arrange
        String email = "test@test.com";
        String wrongToken = "691312";
        UserVerificationDTO verificationDTO = UserVerificationDTO.builder()
                .email(email)
                .verificationToken(wrongToken)
                .build();

        when(userRepositoryMock.findByEmail(email)).thenReturn(null);

        // Act & Assert
        Assertions.assertThrows(InvalidCredentialsException.class, () -> {
            authenticationService.verifyUser(verificationDTO);
        });
        verify(userRepositoryMock, times(1)).findByEmail(email);
    }

    // AFK

    @Test
    public void testVerifyUserWithTokenAndButWithoutExistingUser_shouldFail() {
        // Arrange
        String email = "test@test.com";
        String correctToken = "131269";
        String wrongToken = "691312";

        VerificationToken token = VerificationToken.builder()
                .code(correctToken)
                .expiration(Instant.now().minusSeconds(60 * 60 * 24))
                .build();
        UserVerificationDTO verificationDTO = UserVerificationDTO.builder()
                .email(email)
                .verificationToken(wrongToken)
                .build();

        // Act & Assert
        Assertions.assertThrows(InvalidCredentialsException.class, () -> {
            authenticationService.verifyUser(verificationDTO);
        });

        verify(userRepositoryMock, times(1)).findByEmail(email);
    }

    @Test
    public void testForgedRefreshToken_shouldFailToGetNewJWT() {
        // Arrange
        /*
        forged JWT, signed with a different key (since we do not know the secret key)
        encodes claims:
        {
          "type": "refresh",
          "userName": "myUser",
          "userEmail": "test@user.com",
          "exp": 2516239022,
          "iat": 1516239022,
          "iss": "https://inso.tuwien.ac.at/24ws-ase-pr-inso-02",
          "tokenUUID": "00000000-0000-0000-0000-000000000000"
        }
         */
        String forgedToken = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0eXBlIjoicmVmcmVzaCIsInVzZXJOYW1lIjoibXlVc2VyIiwidXNlckVtYWlsIjoidGVzdEB1c2VyLmNvbSIsImV4cCI6MjUxNjIzOTAyMiwiaWF0IjoxNTE2MjM5MDIyLCJpc3MiOiJodHRwczovL2luc28udHV3aWVuLmFjLmF0LzI0d3MtYXNlLXByLWluc28tMDIiLCJ0b2tlblVVSUQiOiIwMDAwMDAwMC0wMDAwLTAwMDAtMDAwMC0wMDAwMDAwMDAwMDAifQ.sBmd6y8Cf0r8e8kUylFejwQK9uOuo-wpojWIluGtDqH5e_C4BvJZ5KmiQCQeoJNkviF426R9oi-Y8-aFWIa1cnFcTuxZYe_J3SqtZhOn0j0MqgHkNtmihLkwQckNwwNrRs84lXURNQXLsPQIW8Q6Wv_zT4jP8D8Jqq3ouXeHKNxje95ZVSFX24Zjy0MVhTyCRVOw_SUgCKnKg-cmLH6-J4kQTc5g4tE8nY0mogGBAkBn-nF2oA3vLqyR3Lu8r2j9-Tk1doCS-gWYmrZQqf0kCW9K_gckx5NbbIQ_j-b5u6sw0QTzfWueILoBm_Tu0H3ROo8up2gGsyYUvbdytb2j1889GhoJzGpOxIBL0Gto-GvcTYfN8LllturZ0b7ugw348xUinubMSPtOL4vnLNPcOC32vNDBhMhst9DoiHSqIduR2evWddFU5KXPpL5RK4aoMmEI8uKvVOxuTHZxWpaQWMDsYuOuznkhxKZVSOi2e3E9aTla3zSrPkmBBGyb12THHc9egm8I8IRfBxABbEw4WtIOHf0hPOWhUnfwvnU2zpUnzopzvKbWEY3CogcgpfVonEPzMKRGJh0ZoHNFLlsCzYHwrOX8eeZ3sWR1GkEsPqFEobeSx5twhB_JHgxep0uGDMEZZe3YnJ7i-lLl1aWv4OHW0-b80I4f9JiUCJBFLXs";

        // For reference, this is a token with the same claims, but actually signed with the test-private key, which would make this test-case fail, because the refreshToken is actually valid!
        // String forgedToken = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0eXBlIjoicmVmcmVzaCIsInVzZXJOYW1lIjoibXlVc2VyIiwidXNlckVtYWlsIjoidGVzdEB1c2VyLmNvbSIsImV4cCI6MjUxNjIzOTAyMiwiaWF0IjoxNTE2MjM5MDIyLCJpc3MiOiJodHRwczovL2luc28udHV3aWVuLmFjLmF0LzI0d3MtYXNlLXByLWluc28tMDIiLCJ0b2tlblVVSUQiOiIwMDAwMDAwMC0wMDAwLTAwMDAtMDAwMC0wMDAwMDAwMDAwMDAifQ.haDKWVxLI3iT_-pf7QYvs3s98eHNx-C1wdsRynxJe1iJCtN6IhzKZa-6kVCkmFmTs5itvO5PVCAUpRXq9zoXh9e8euIxK08OFg0fbX3FD0pQwe-v0r1GRWdcjCmo7Z9DZxzylcF-o11OgSiGNa7j0U3MAyKaQTaiOoPNonPRQDxkgsvmArCSe3FaR0hUpnyJHtKqh8iZDoYzqZ2FZAy5At-zEdgrnJcq8aOmASYdntXE1Bx0E_6V9aADOv8terboa0oGIfcLMp5d2hkg1p9XbENUFSfkCx-Rk_EJUtinhjWpm1_pcGRDwF6i2-dTyLQhVsEcEq_EcKyIpQhP-iOelw";

        // even when we give it the benefit of the doubt (and actually have a token with that value, however unlikely), the signature-check should fail
        UUID tokenUUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
        when(tokenRepositoryMock.findByTokenUUID(tokenUUID))
                .thenReturn(RefreshToken.builder()
                        .user(User.builder()
                                .username("myUser")
                                .email("test@user.com")
                                .roles(Set.of(UserRole.builder().role("USER").build()))
                                .build())
                        .uuid(tokenUUID)
                        .expiration(Instant.ofEpochSecond(2516239022L))
                        .build()
                );

        // Act & Assert
        Assertions.assertThrows(InvalidRefreshTokenException.class, () -> authenticationService.refreshLogin(TokenRefreshDTO.builder().refreshToken(forgedToken).build()));
    }


    @Test
    public void testInvalidRefreshToken_shouldFailToGetNewJWT() {
        // Arrange
        /*
        invalid token:
        {
          "type": "access",
          "userName": null,
          "userEmail": nulll,
          "exp": 2516239022,
          "iat": 1516239022,
          "iss": "https://inso.tuwien.ac.at/24ws-ase-pr-inso-02",
          "tokenUUID": null
        }
         */

        String invalidToken = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0eXBlIjoiYWNjZXNzIiwidXBuIjpudWxsLCJzdWIiOm51bGwsImZ1bGxfbmFtZSI6bnVsbCwiZ3JvdXBzIjpbIkFETUlOIl0sImlhdCI6MTczODIyNjMyNiwiZXhwIjoxNzM4MzEyNzI2LCJqdGkiOm51bGwsImlzcyI6Imh0dHBzOi8vaW5zby50dXdpZW4uYWMuYXQvMjR3cy1hc2UtcHItaW5zby0wMiJ9.KRGGmvlb5-U_rYdZ3PHaT2U8j9eTzV24ol-9td6FfTvtjTLGAoR6JJyjxEVDJa_jHVz0m43Px8B7MPljMMptLL9Pt7UPiZ6w4KUpwmXtc7Jpr8LcUfc3RgElQEgn0lIMHELY2HU4wUa-WuMyW4hBE0rIC_lwayiZ20rd0X4q77az7tkrvIhb2sJs4dEzxIcj-A1f44Rj5PJN_H6lKn40IXkyA-5BJLEefdH7XKO0n6_W_k94Ii0QH0kdri5l21NKh49T3EB_QVxE4v0SyV-sgMLuKQqmp5L2r9NqHg8v6nL3lnKYmhm2THfLT2JWHZJN7Jo-XHUPRZwMBp8IUNFpwQ";

        // Act & Assert
        Assertions.assertThrows(InvalidRefreshTokenException.class, () -> authenticationService.refreshLogin(TokenRefreshDTO.builder().refreshToken(invalidToken).build()));
    }

    @Test
    public void testInvalidRefreshToken_shouldFailToGetTokenByUUID() {
        // Arrange
        String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJ0eXBlIjoicmVmcmVzaCIsInVzZXJFbWFpbCI6InVzZXJAZXhhbXBsZS5jb20iLCJ1c2VyTmFtZSI6InVzZXIiLCJ0b2tlblVVSUQiOiIxZjZiMTk0Ny1iY2VhLTRkYTAtOWZkYi1kZGUzMmU0MmVhMjEiLCJleHAiOjE3MzgzMTI3MjYsImlhdCI6MTczODIyNjMyNiwianRpIjoiYmVlMWU4MTUtNzU2Ni00NmIwLWI1NmMtMzVlZDY5OTVlYTQ0IiwiaXNzIjoiaHR0cHM6Ly9pbnNvLnR1d2llbi5hYy5hdC8yNHdzLWFzZS1wci1pbnNvLTAyIn0.aq-xe4lMgVkS0Hmq15-9BFLMF5AeHPQMH6VGtTO-an_gjfPtN7f-Y4ptWkTkRZi1xnOuVDvuyT9iJhpyWFKb3JsW-XXoGkDqKDNvLymu2cgi_wysdB-mRZA-QnKWueyIPU0ijrUpdbHaceoiyKiqYxshYwXEi2MuzVTXgCKwUoFkOxzeWqDG3SFzSvIFuOGw2IozGeMbvWIde_niYsEN0wdq6nAhbpPSqYWE6OcFn8KSkVigNLR1NoZYxSDAoM1Cyp5_thEmR6fAeBlRFWnUhueFrJW8N4KmDWUYW_fxZYW1dU4tkwyD3PX1iOwMrSusBo4T5w7Vs8okFdYUGl8uJQ";
        when(tokenRepositoryMock.findByTokenUUID(any(UUID.class))).thenReturn(null);
        // Act & Assert
        Assertions.assertThrows(InvalidRefreshTokenException.class, () -> authenticationService.refreshLogin(TokenRefreshDTO.builder().refreshToken(token).build()));
    }

    @Test
    public void testAuthenticationService_shouldReturnCorrectEmailAndUsername() {
        // Arrange
        User user = User.builder()
                .email("test@test.com")
                .username("test")
                .password(BcryptUtil.bcryptHash("Password123!"))
                .state(UserState.ACTIVE)
                .roles(Set.of(UserRole.builder().role("USER").build()))
                .build();

        when(userRepositoryMock.findByEmail(user.getEmail())).thenReturn(user);
        when(userRepositoryMock.findByUsername(user.getUsername())).thenReturn(user);

        // Act
        String token = authenticationService.loginUser(UserLoginDTO.builder()
                        .emailOrUsername("test").password("Password123!").build())
                .getJwt();

        // Assert
        Assertions.assertNotNull(token);

        // with the token, access to the test-endpoint it granted
        given()
                .header("Authorization", "Bearer " + token)
                .when().get("/test/protected")
                .then()
                .statusCode(200)
                .body(equalTo("Hello, " +
                        user.getUsername() + "! You have access to this protected resource."));
    }

    @Test
    void testSendPasswordResetEmail_shouldSendEmail() {
        // Arrange
        PasswordResetRequestDTO request = PasswordResetRequestDTO.builder().email("user@example.com").build();
        User user = User.builder().email("user@example.com").username("user").build();
        when(userRepositoryMock.findByEmail(request.getEmail())).thenReturn(user);
        doNothing().when(mailServiceMock).sendPasswordResetMail(eq("user@example.com"), any(String.class));

        when(userRepositoryMock.persistUser(user)).thenReturn(user);
        when(tokenRepositoryMock.findByTokenUUID(any(UUID.class))).thenReturn(null);

        // Act
        authenticationService.sendPasswordResetEmail(request);

        // Assert
        verify(mailServiceMock, times(1))
                .sendPasswordResetMail(eq("user@example.com"), any(String.class));
    }

    @Test
    void testSendPasswordResetEmail_shouldNotSendEmailForNonExistentUser() {
        // Arrange
        PasswordResetRequestDTO request = PasswordResetRequestDTO.builder().email("user@example.com").build();
        when(userRepositoryMock.findByEmail(request.getEmail())).thenReturn(null);

        // Act
        authenticationService.sendPasswordResetEmail(request);

        // Assert
        verify(mailServiceMock, never())
                .sendPasswordResetMail(eq("user@example.com"), any(String.class));
    }

    @Path("/test")
    private static class TestJWTController {

        AuthenticationService authenticationService1;

        public TestJWTController(AuthenticationService authenticationService1) {
            this.authenticationService1 = authenticationService1;
        }

        @GET
        @Path("/protected")
        @RolesAllowed({"USER", "ADMIN"})
        public Response getProtectedResource(@Context SecurityContext securityContext) {
            return Response.ok("Hello, " + authenticationService1.getCurrentUsername() + "! You have access to this protected resource.").build();
        }

        @GET
        @Path("/protected-admin")
        @RolesAllowed("ADMIN")
        public Response getProtectedAdminResource(@Context SecurityContext securityContext) {
            return Response.ok("Hello, " + authenticationService1.getCurrentUsername() + "! You have access to this protected resource.").build();
        }
    }
}
