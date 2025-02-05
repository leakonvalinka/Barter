package at.ac.ase.inso.group02.skills;

import at.ac.ase.inso.group02.skills.dto.SkillCategoryDTO;
import at.ac.ase.inso.group02.views.Views;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.ResponseStatus;
import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.RestQuery;

import java.util.Set;

/**
 * REST endpoint controller for skill-category information
 */
@Path("/skills/categories")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface SkillCategoryController {

    /**
     * lists all known skill-categories
     *
     * @return a set of all known skill-categories
     */
    @GET
    @ResponseStatus(200)
    @RolesAllowed({"USER", "ADMIN"})
    @JsonView(Views.Full.class)
    Set<SkillCategoryDTO> getCategories(@RestQuery String q);

    /**
     * lists information about a specific skill-category by id
     *
     * @param id id of the skill-category to look up
     * @return information about the skill-category with given id
     */
    @GET
    @ResponseStatus(200)
    @RolesAllowed({"USER", "ADMIN"})
    @Path("/{id}")
    @JsonView(Views.Full.class)
    SkillCategoryDTO getCategoryById(@RestPath Long id);
}
