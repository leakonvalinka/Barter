package at.ac.ase.inso.group02.rating;

import at.ac.ase.inso.group02.rating.dto.CreateRatingDTO;
import at.ac.ase.inso.group02.rating.dto.UserRatingDTO;
import at.ac.ase.inso.group02.rating.views.RatingViews;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.ResponseStatus;
import org.jboss.resteasy.reactive.RestPath;

/**
 * controller for rating-specific actions
 * (only for existing ratings; to create ratings see /exchange/id/rate in ExchangeController)
 */
@Path("/rating")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface RatingController {
    /**
     * Endpoint to update an existing rating (must be created by the same user trying to modify)
     *
     * @param ratingID  - if of the rating
     * @param ratingDTO - new data of the rating
     * @return the updated User rating information
     */
    @PUT
    @ResponseStatus(200)
    @RolesAllowed({"USER", "ADMIN"})
    @Path("/{ratingID}")
    @JsonView(RatingViews.IncludeForUser.class)
    UserRatingDTO updateRating(@RestPath Long ratingID, @Valid CreateRatingDTO ratingDTO);

    /**
     * Endpoint to remove an existing rating
     *
     * @param ratingID - id of the rating to delete
     */
    @DELETE
    @ResponseStatus(204)
    @RolesAllowed({"USER", "ADMIN"})
    @Path("/{ratingID}")
    void deleteRating(@RestPath Long ratingID);
}
