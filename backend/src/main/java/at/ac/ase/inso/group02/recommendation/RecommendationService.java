package at.ac.ase.inso.group02.recommendation;

import at.ac.ase.inso.group02.skills.dto.SkillDemandDTO;
import at.ac.ase.inso.group02.skills.dto.SkillOfferDTO;
import at.ac.ase.inso.group02.util.pagination.PaginatedQueryDTO;

/**
 * Service interface for skill recommendation business logic.
 * Handles the filtering and matching of skill offers based on user demands.
 */
public interface RecommendationService {
    /**
     * Retrieves filtered and ranked skill recommendations based on a user's demand.
     *
     * @param demandDTO The skill demand to find recommendations for
     * @return A paginated list of matching skill offers
     */
    PaginatedQueryDTO<SkillOfferDTO> getRecommendations(SkillDemandDTO demandDTO);
}
