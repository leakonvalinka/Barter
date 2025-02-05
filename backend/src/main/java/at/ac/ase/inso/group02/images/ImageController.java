package at.ac.ase.inso.group02.images;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.RestPath;

import java.util.UUID;

/**
 * image REST endpoint
 */
@Path("/images")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface ImageController {

    /**
     * retrieves binary image data for an image based on id
     *
     * @param id image-id
     * @return binary image data
     */
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    byte[] getImage(@RestPath UUID id);
}
