package at.ac.ase.inso.group02.authentication.exceptionhandler;

import at.ac.ase.inso.group02.authentication.exception.*;
import at.ac.ase.inso.group02.exceptions.ErrorResponse;
import io.quarkus.logging.Log;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

class UserExceptionHandler {
    @ServerExceptionMapper
    public RestResponse<ErrorResponse> handleEmailInUseException(EMailInUseException exception) {
        Log.warn(exception.getMessage());
        return RestResponse.status(Response.Status.CONFLICT, new ErrorResponse(exception.getMessage()));
    }

    @ServerExceptionMapper
    public RestResponse<ErrorResponse> handleUsernameAlreadyExistsException(UsernameAlreadyExistsException exception) {
        Log.warn(exception.getMessage());
        return RestResponse.status(Response.Status.CONFLICT, new ErrorResponse(exception.getMessage()));
    }

    @ServerExceptionMapper
    public RestResponse<ErrorResponse> handleEMailNotConfirmedException(EMailNotConfirmedException exception) {
        Log.warn(exception.getMessage());
        return RestResponse.status(Response.Status.FORBIDDEN, new ErrorResponse(exception.getMessage()));
    }

    @ServerExceptionMapper
    public RestResponse<ErrorResponse> handleInvalidCredentialsException(InvalidCredentialsException exception) {
        Log.warnv("Invalid login attempt: {0}", exception.getMessage());
        return RestResponse.status(Response.Status.UNAUTHORIZED, new ErrorResponse("Invalid credentials")); // do not expose what credential is invalid to protect user-emails
    }

    @ServerExceptionMapper
    public RestResponse<ErrorResponse> handleInvalidRefreshTokenException(InvalidRefreshTokenException exception) {
        Log.warnv("Invalid token-refresh attempt: {0}", exception.getMessage());
        return RestResponse.status(Response.Status.UNAUTHORIZED, new ErrorResponse("Invalid refresh-token")); // do not expose what is wrong about the token
    }


    @ServerExceptionMapper
    public RestResponse<ErrorResponse> handleAlreadyVerifiedException(AlreadyVerifiedException exception) {
        Log.warnv("User is already verified: {0}", exception.getMessage());
        return RestResponse.status(Response.Status.CONFLICT, new ErrorResponse("Already verified"));
    }

    @ServerExceptionMapper
    public RestResponse<ErrorResponse> handleWrongVerificationTokenException(WrongVerificationTokenException exception) {
        Log.warnv("Verification code does not match " + exception.getMessage());
        return RestResponse.status(Response.Status.BAD_REQUEST, new ErrorResponse("Verification code does not match"));
    }

    @ServerExceptionMapper
    public RestResponse<ErrorResponse> handleVerificationTokenExpiredException(VerificationTokenExpiredException exception) {
        Log.warnv("Verification code has expired " + exception.getMessage());
        return RestResponse.status(Response.Status.BAD_REQUEST, new ErrorResponse("Verification code has expired"));
    }

    /*@ServerExceptionMapper
    public RestResponse<ErrorResponse> handleOtherExceptions(Exception exception) {
        Log.error("Unexpected error: " + exception.getMessage(), exception);
        return RestResponse.status(Response.Status.INTERNAL_SERVER_ERROR, new ErrorResponse("An unexpected error occurred"));
    }*/
}

