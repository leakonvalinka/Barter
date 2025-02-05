package at.ac.ase.inso.group02.exceptions;

import io.quarkus.logging.Log;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * provides exception handles for exceptions that may occur throughout the entire project
 */
public class GlobalExceptionHandler {

    @ServerExceptionMapper
    public RestResponse<ErrorResponse> handleUnauthenticatedException(UnauthenticatedException exception) {
        Log.warn("Improperly authenticated access attempt: " + exception.getMessage());
        return RestResponse.status(Response.Status.UNAUTHORIZED, new ErrorResponse("Unauthorized"));
    }

    @ServerExceptionMapper
    public RestResponse<ErrorResponse> handleUnauthorizedModificationException(UnauthorizedModificationException exception) {
        Log.warn("Unauthorized access attempt: " + exception.getMessage());
        return RestResponse.status(Response.Status.FORBIDDEN, new ErrorResponse("Unauthorized: " + exception.getMessage()));
    }

    @ServerExceptionMapper
    public RestResponse<ErrorResponse> handleUnauthorizedCreationException(UnauthorizedCreationException exception) {
        Log.warn("Unauthorized creation attempt: " + exception.getMessage());
        return RestResponse.status(Response.Status.FORBIDDEN, new ErrorResponse("Forbidden: " + exception.getMessage()));
    }

    @ServerExceptionMapper
    public RestResponse<ErrorResponse> handleNotFoundException(NotFoundException exception) {
        Log.warn(exception.getMessage());
        return RestResponse.status(Response.Status.NOT_FOUND, new ErrorResponse(exception.getMessage()));
    }

    @ServerExceptionMapper
    public RestResponse<ErrorResponse> handleConstraintViolationException(ConstraintViolationException exception) {
        Log.warn("Validation error occurred: " + exception.getMessage());
        // Collect individual violation messages
        Map<String, String> violations = exception.getConstraintViolations().stream()
                .collect(Collectors.toMap(this::getFieldName, ConstraintViolation::getMessage, (existing, newValue) -> {
                    existing += ", " + newValue;
                    return existing;
                }));
        return RestResponse.status(Response.Status.BAD_REQUEST, new ErrorResponse("Validation error!", violations));
    }

    private String getFieldName(ConstraintViolation<?> violation) {
        Path propertyPath = violation.getPropertyPath();
        return StreamSupport.stream(propertyPath.spliterator(), false)
                .skip(2)
                .map(Path.Node::toString)
                .collect(Collectors.joining("."));
    }
}
