package at.ac.ase.inso.group02.skills.exceptionhandler;

import at.ac.ase.inso.group02.exceptions.ErrorResponse;
import at.ac.ase.inso.group02.skills.exception.SkillCategoryDoesNotExistException;
import at.ac.ase.inso.group02.skills.exception.SkillDoesNotExistException;
import io.quarkus.logging.Log;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

class SkillExceptionHandler {

    @ServerExceptionMapper
    public RestResponse<ErrorResponse> handleSkillCategoryDoesNotExistException(SkillCategoryDoesNotExistException exception) {
        Log.warn(exception.getMessage());
        return RestResponse.status(Response.Status.NOT_FOUND, new ErrorResponse(exception.getMessage()));
    }

    @ServerExceptionMapper
    public RestResponse<ErrorResponse> handleSkillDoesNotExistException(SkillDoesNotExistException exception) {
        Log.warn(exception.getMessage());
        return RestResponse.status(Response.Status.NOT_FOUND, new ErrorResponse(exception.getMessage()));
    }
}

