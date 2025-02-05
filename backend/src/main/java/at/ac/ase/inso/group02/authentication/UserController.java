package at.ac.ase.inso.group02.authentication;

import at.ac.ase.inso.group02.authentication.dto.UserDetailDTO;
import at.ac.ase.inso.group02.authentication.dto.UserUpdateDTO;
import at.ac.ase.inso.group02.rating.dto.UserRatingDTO;
import at.ac.ase.inso.group02.rating.views.RatingViews;
import at.ac.ase.inso.group02.util.pagination.PaginatedQueryDTO;
import at.ac.ase.inso.group02.util.pagination.PaginationParamsDTO;
import at.ac.ase.inso.group02.views.Views;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.ResponseStatus;
import org.jboss.resteasy.reactive.RestPath;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface UserController {

    /**
     * Endpoint to retrieve user information for the currently authenticated user
     *
     * @return User information
     */
    @GET
    @RolesAllowed({"USER", "ADMIN"})
    @JsonView(Views.Private.class)
    UserDetailDTO getCurrentUser();

    /**
     * Endpoint to retrieve user information by Username
     *
     * @param username - for the user to be retrieved
     * @return User information
     */
    @GET
    @PermitAll
    @Path("/{username}")
    @JsonView(Views.Public.class)
    UserDetailDTO getUserByUsername(@RestPath String username);

    /**
     * Endpoint to update user information
     *
     * @param updateData the user update data
     * @return Updated user information
     */
    @PUT
    @ResponseStatus(200)
    @RolesAllowed({"USER", "ADMIN"})
    @JsonView(Views.Private.class)
    UserDetailDTO updateUser(@Valid UserUpdateDTO updateData);

    /**
     * Endpoint to delete a user by their username, which is taken from the token.
     */
    @DELETE
    @ResponseStatus(204)
    @RolesAllowed({"USER", "ADMIN"})
    void deleteUser();

    /**
     * Endpoint to retrieve ratings by Username
     *
     * @param username - of the user to retrieve ratings for
     * @return User rating information
     */
    @GET
    @ResponseStatus(200)
    @RolesAllowed({"USER", "ADMIN"})
    @Path("/{username}/ratings")
    @JsonView(RatingViews.IncludeByUser.class)
    PaginatedQueryDTO<UserRatingDTO> getRatingsForUsername(@RestPath String username, @BeanParam PaginationParamsDTO paginationParamsDTO);
}
