package at.ac.ase.inso.group02.authentication.impl;

import at.ac.ase.inso.group02.authentication.AuthenticationController;
import at.ac.ase.inso.group02.authentication.AuthenticationService;
import at.ac.ase.inso.group02.authentication.dto.*;
import at.ac.ase.inso.group02.views.Views;
import com.fasterxml.jackson.annotation.JsonView;
import io.quarkus.logging.Log;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class AuthenticationControllerImpl implements AuthenticationController {
    private AuthenticationService authenticationService;

    @Override
    @JsonView(Views.Private.class)
    public UserInfoDTO register(UserRegistrationDTO userRegistrationDTO) {
        Log.infov("New user registration with email: {0}", userRegistrationDTO.getEmail());
        return authenticationService.registerUser(userRegistrationDTO);
    }

    @Override
    public LoginResponseDTO login(UserLoginDTO userLoginDTO) {
        Log.infov("Login attempt with email/username: {0}", userLoginDTO.getEmailOrUsername());
        return authenticationService.loginUser(userLoginDTO);
    }

    @Override
    public LoginResponseDTO refresh(TokenRefreshDTO tokenRefreshDTO) {
        Log.infov("Refreshing token: " + tokenRefreshDTO);
        return authenticationService.refreshLogin(tokenRefreshDTO);
    }


    @Override
    public void requestPasswordReset(PasswordResetRequestDTO passwordResetRequestDto) {
        Log.infov("Password Reset Link requested for user with email: {0}", passwordResetRequestDto.getEmail());
        authenticationService.sendPasswordResetEmail(passwordResetRequestDto);
    }

    @Override
    public UserInfoDTO resetPassword(String resetToken, PasswordResetDTO passwordResetDTO) {
        Log.infov("Password Reset requested with token: {0}", resetToken);
        TokenRefreshDTO refreshTokenDto = TokenRefreshDTO.builder().refreshToken(resetToken).build();
        return authenticationService.resetPassword(refreshTokenDto, passwordResetDTO);
    }

    @Override
    public LoginResponseDTO verify(UserVerificationDTO userVerificationDTO) {
        Log.infov("Verifying user with email {0} and code {1}", userVerificationDTO.getEmail(), userVerificationDTO.getVerificationToken());
        return authenticationService.verifyUser(userVerificationDTO);
    }
}
