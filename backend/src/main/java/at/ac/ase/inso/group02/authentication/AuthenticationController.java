package at.ac.ase.inso.group02.authentication;

import at.ac.ase.inso.group02.authentication.dto.*;
import at.ac.ase.inso.group02.views.Views;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.ResponseStatus;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface AuthenticationController {

    /**
     * Endpoint for user registration
     *
     * @param user the user registration data
     * @return Response with created user information
     */
    @POST
    @ResponseStatus(201)
    @PermitAll
    @Path("/register")
    @JsonView(Views.Private.class)
    UserInfoDTO register(@Valid UserRegistrationDTO user);

    @POST
    @ResponseStatus(200)
    @Path("/login")
    LoginResponseDTO login(@Valid UserLoginDTO userLoginDTO);

    @POST
    @ResponseStatus(200)
    @Path("/refresh-token")
    LoginResponseDTO refresh(@Valid TokenRefreshDTO tokenRefreshDTO);


    /**
     * Endpoint to request a password reset link.
     *
     * @param passwordResetRequestDto DTO containing the user's email.
     */
    @POST
    @ResponseStatus(200)
    @Path("/reset-password")
    @PermitAll
    void requestPasswordReset(PasswordResetRequestDTO passwordResetRequestDto);

    /**
     * Endpoint to reset the password.
     *
     * @param token            The token received in the reset link.
     * @param passwordResetDTO Object containing the new password.
     */
    @POST
    @ResponseStatus(200)
    @Path("/reset-password/{resetToken}")
    @RolesAllowed("PASSWORD-RESET")
    UserInfoDTO resetPassword(@PathParam("resetToken") String resetToken, @Valid PasswordResetDTO passwordResetDTO);

    /*
     * Endpoint to verify user account
     * @param userVerificationDTO dto consisting of the users email address and the verification code
     */
    @POST
    @ResponseStatus(200)
    @PermitAll
    @Path("/verify")
    LoginResponseDTO verify(@Valid UserVerificationDTO userVerificationDTO);
}
