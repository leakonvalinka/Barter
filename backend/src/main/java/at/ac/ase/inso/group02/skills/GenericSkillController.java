package at.ac.ase.inso.group02.skills;

import at.ac.ase.inso.group02.rating.dto.UserRatingDTO;
import at.ac.ase.inso.group02.rating.views.RatingViews;
import at.ac.ase.inso.group02.skills.dto.SkillDTO;
import at.ac.ase.inso.group02.skills.dto.SkillQueryParamsDTO;
import at.ac.ase.inso.group02.util.pagination.PaginatedQueryDTO;
import at.ac.ase.inso.group02.util.pagination.PaginationParamsDTO;
import at.ac.ase.inso.group02.views.Views;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.ResponseStatus;
import org.jboss.resteasy.reactive.RestPath;

/**
 * generic REST Skill controller for actions applicable to all skills
 * (all types of skills simultaneously or skill-demands/-offers separately)
 *
 * @param <D> the expected return-DTO type of the methods (e.g. SkillDTO for all types of skills simultaneously)
 *            (note: if all skill-types are handled simultaneously (D == SkillDTO),
 *            the "type" field is explicitly set to "offer" or "demand" for each returned skill)
 */
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface GenericSkillController<D extends SkillDTO> {

    /**
     * searches for skills and filters by the given constraints
     * default: 5km radius from the location of the currently logged-in user
     *
     * @param params constraints for the search
     * @return paginated skill results
     */
    @GET
    @ResponseStatus(200)
    @RolesAllowed({"USER", "ADMIN"})
    @JsonView(Views.ExplicitlyTypedBrief.class)
    PaginatedQueryDTO<D> getSkills(@BeanParam SkillQueryParamsDTO params);

    /**
     * returns a single skill with the given id
     *
     * @param id id of a skill
     * @return the skill with the given id
     */
    @GET
    @ResponseStatus(200)
    @RolesAllowed({"USER", "ADMIN"})
    @Path("/{id}")
    @JsonView(Views.ExplicitlyTypedFull.class)
    D getSkill(@RestPath Long id);

    /**
     * returns the ratings for a given skill-id
     * (this includes only ratings *for* the user that created the skill posting)
     *
     * @param id - of the skill to find ratings for
     * @param paginationParamsDTO pagination parameters
     * @return paginated rating results
     */
    @GET
    @ResponseStatus(200)
    @RolesAllowed({"USER", "ADMIN"})
    @Path("/{id}/ratings")
    @JsonView(RatingViews.IncludeByUser.class)
    PaginatedQueryDTO<UserRatingDTO> getSkillRatings(@RestPath Long id, @BeanParam PaginationParamsDTO paginationParamsDTO);

    /**
     * deletes a skill by the id
     *
     * @param id id of the skill to be deleted
     */
    @DELETE
    @ResponseStatus(204)
    @RolesAllowed({"USER", "ADMIN"})
    @Path("/{id}")
    void deleteSkill(@RestPath Long id);
}
