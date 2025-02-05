package at.ac.ase.inso.group02.recommendation.impl;

import at.ac.ase.inso.group02.recommendation.RecommendationController;
import at.ac.ase.inso.group02.recommendation.RecommendationService;
import at.ac.ase.inso.group02.skills.dto.SkillDemandDTO;
import at.ac.ase.inso.group02.skills.dto.SkillOfferDTO;
import at.ac.ase.inso.group02.util.pagination.PaginatedQueryDTO;
import io.quarkus.logging.Log;
import lombok.AllArgsConstructor;

/**
 * Implementation of the RecommendationController interface.
 * Handles HTTP requests for skill recommendations.
 */
@AllArgsConstructor
public class RecommendationControllerImpl implements RecommendationController {
    private final RecommendationService recommendationService;

    /**
     * {@inheritDoc}
     */
    @Override
    public PaginatedQueryDTO<SkillOfferDTO> getRecommendationsForCurrentUser(SkillDemandDTO demandDTO) {
        Log.infov("Finding recommendations for demand {0}" ,demandDTO.getId());
        return recommendationService.getRecommendations(demandDTO);
    }
}
