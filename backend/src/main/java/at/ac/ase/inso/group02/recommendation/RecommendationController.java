package at.ac.ase.inso.group02.recommendation;

import org.jboss.resteasy.reactive.ResponseStatus;

import at.ac.ase.inso.group02.skills.dto.SkillDemandDTO;
import at.ac.ase.inso.group02.skills.dto.SkillOfferDTO;
import at.ac.ase.inso.group02.util.pagination.PaginatedQueryDTO;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * Controller interface for handling skill recommendation endpoints.
 * Provides REST endpoints for retrieving skill recommendations based on user demands.
 */
@Path("/recommendation")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface RecommendationController {
    /**
     * Retrieves skill recommendations for the currently authenticated user based on their demand.
     *
     * @param demandDTO The skill demand to find recommendations for
     * @return A paginated list of skill offers that match the demand criteria
     */
    @POST
    @ResponseStatus(200)
    @RolesAllowed({"USER", "ADMIN"})
    PaginatedQueryDTO<SkillOfferDTO> getRecommendationsForCurrentUser(SkillDemandDTO demandDTO);
}
