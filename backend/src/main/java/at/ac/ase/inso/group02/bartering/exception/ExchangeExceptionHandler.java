package at.ac.ase.inso.group02.bartering.exception;

import at.ac.ase.inso.group02.exceptions.ErrorResponse;
import io.quarkus.logging.Log;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

class ExchangeExceptionHandler {

    @ServerExceptionMapper
    public RestResponse<ErrorResponse> handleNotPartOfExchangeException(NotPartOfExchangeException exception) {
        Log.warn(exception.getMessage());
        return RestResponse.status(Response.Status.FORBIDDEN, new ErrorResponse(exception.getMessage()));
    }

    @ServerExceptionMapper
    public RestResponse<ErrorResponse> handleExchangeNotRatableException(ExchangeNotRatableException exception) {
        Log.warn(exception.getMessage());
        return RestResponse.status(Response.Status.CONFLICT, new ErrorResponse(exception.getMessage()));
    }

    @ServerExceptionMapper
    public RestResponse<ErrorResponse> handleIllegalExchangeException(IllegalExchangeException exception) {
        Log.warn(exception.getMessage());
        return RestResponse.status(Response.Status.CONFLICT, new ErrorResponse(exception.getMessage()));
    }

    @ServerExceptionMapper
    public RestResponse<ErrorResponse> handleIllegalExchangeModificationException(IllegalExchangeModificationException exception) {
        Log.warn(exception.getMessage());
        return RestResponse.status(Response.Status.CONFLICT, new ErrorResponse(exception.getMessage()));
    }
}

