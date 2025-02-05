package at.ac.ase.inso.group02.admin.exception;

import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import at.ac.ase.inso.group02.exceptions.ErrorResponse;
import io.quarkus.logging.Log;
import jakarta.ws.rs.core.Response;

public class AdminExceptionHandler {
	@ServerExceptionMapper
    public RestResponse<ErrorResponse> handleUserIsBannedException(UserIsBannedException exception) {
        Log.warnv("The user is banned: " + exception.getMessage());
        return RestResponse.status(Response.Status.FORBIDDEN, new ErrorResponse("The user is banned"));
    }
}
