package at.ac.ase.inso.group02.rating.exception;

import at.ac.ase.inso.group02.exceptions.ErrorResponse;
import io.quarkus.logging.Log;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

class RatingExceptionHandler {

    @ServerExceptionMapper
    public RestResponse<ErrorResponse> handleRatingAlreadyExistsException(RatingAlreadyExistsException exception) {
        Log.warn(exception.getMessage());
        return RestResponse.status(Response.Status.CONFLICT, new ErrorResponse(exception.getMessage()));
    }
}

