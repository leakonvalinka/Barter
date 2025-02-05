package at.ac.ase.inso.group02.authentication;

import at.ac.ase.inso.group02.authentication.dto.*;
import at.ac.ase.inso.group02.authentication.exception.*;
import at.ac.ase.inso.group02.entities.User;
import at.ac.ase.inso.group02.exceptions.UnauthenticatedException;
import jakarta.validation.ConstraintViolationException;

public interface AuthenticationService {

    /**
     * register a new user
     *
     * @param userRegistrationDTO necessary info to create a user
     * @return info about the newly registered user
     * @throws EMailInUseException            if the given email is already in use
     * @throws UsernameAlreadyExistsException if the given username is already in use
     * @throws ConstraintViolationException   if any other validation error occurs
     */
    UserInfoDTO registerUser(UserRegistrationDTO userRegistrationDTO) throws EMailInUseException, UsernameAlreadyExistsException, ConstraintViolationException;


    /**
     * login an existing user and generate an access token
     *
     * @param userLoginDTO login info, can include either username or email
     * @return a JWT token that can authenticate the user and a refresh token that can be used to get a new JET token without user credentials
     * @throws InvalidCredentialsException  if the provided credentials are invalid, i.e. username/email does not exist or the password is incorrect
     * @throws EMailNotConfirmedException   if credentials are correct, but the user did not confirm their email yet
     * @throws ConstraintViolationException if any other validation error occurs
     */
    LoginResponseDTO loginUser(UserLoginDTO userLoginDTO) throws InvalidCredentialsException, EMailNotConfirmedException, ConstraintViolationException;

    /**
     * refresh an access token using a previously issued refresh token
     *
     * @param tokenRefreshDTO the refresh token that was previously issued
     * @return a new JWT token and refresh token like loginUser()
     * @throws InvalidRefreshTokenException if the provided token is invalid
     */
    LoginResponseDTO refreshLogin(TokenRefreshDTO tokenRefreshDTO) throws InvalidRefreshTokenException;

    /**
     * Sends a password reset email to the user.
     *
     * @param passwordResetRequestDto contains the user's email.
     */
    void sendPasswordResetEmail(PasswordResetRequestDTO passwordResetRequestDto);

    /**
     * Resets the user's password using the provided token and new password.
     *
     * @param resetTokenDTO    The reset token.
     * @param passwordResetDTO Object containing the new password.
     */
    UserInfoDTO resetPassword(TokenRefreshDTO resetTokenDTO, PasswordResetDTO passwordResetDTO);

    /**
     * verify a registerd user
     *
     * @param verificationDTO email and verification code
     * @throws VerificationTokenExpiredException if the token has expired
     * @throws WrongVerificationTokenException   if the code does not match the stored code
     * @throws AlreadyVerifiedException          if the user is already verified
     */
    LoginResponseDTO verifyUser(UserVerificationDTO verificationDTO) throws VerificationTokenExpiredException, WrongVerificationTokenException, AlreadyVerifiedException;

    /**
     * retrieves the currently authenticated user (by the provided JWT)
     * @return the current user
     * @throws UnauthenticatedException if the JWT is invalid or expired, or the user could not be found in the DB
     */
    User getCurrentUser();

    String getCurrentUsername();
}
