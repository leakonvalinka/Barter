package at.ac.ase.inso.group02.messaging.exceptions;

import at.ac.ase.inso.group02.exceptions.ErrorResponse;
import io.quarkus.logging.Log;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

public class MessagingExceptionHandler {
    @ServerExceptionMapper
    public RestResponse<ErrorResponse> handleIllegalWSTicketException(IllegalWSTicketException exception) {
        Log.warn(exception.getMessage());
        return RestResponse.status(Response.Status.UNAUTHORIZED, new ErrorResponse(exception.getMessage()));
    }

    @ServerExceptionMapper
    public RestResponse<ErrorResponse> handleWorkerStartException(WorkerStartException exception) {
        Log.warn(exception.getMessage());
        return RestResponse.status(Response.Status.SERVICE_UNAVAILABLE, new ErrorResponse(exception.getMessage()));
    }

    @ServerExceptionMapper
    public RestResponse<ErrorResponse> handleWorkerStopException(WorkerStopException exception) {
        Log.warn(exception.getMessage());
        return RestResponse.status(Response.Status.SERVICE_UNAVAILABLE, new ErrorResponse(exception.getMessage()));
    }

    @ServerExceptionMapper
    public RestResponse<ErrorResponse> handleChatMessagePublishException(ChatMessagePublishException exception) {
        Log.warn(exception.getMessage());
        return RestResponse.status(Response.Status.SERVICE_UNAVAILABLE, new ErrorResponse(exception.getMessage()));
    }
}
