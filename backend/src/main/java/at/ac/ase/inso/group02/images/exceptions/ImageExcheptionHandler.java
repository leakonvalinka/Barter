package at.ac.ase.inso.group02.images.exceptions;

import at.ac.ase.inso.group02.exceptions.ErrorResponse;
import io.quarkus.logging.Log;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

public class ImageExcheptionHandler {

    @ServerExceptionMapper
    public RestResponse<ErrorResponse> handleImageDataRetrievalError(ImageDataRetrievalError exception) {
        Log.warn("Not found: " + exception.getMessage());
        return RestResponse.status(Response.Status.NOT_FOUND, new ErrorResponse("Could not retrieve image data"));
    }
}
