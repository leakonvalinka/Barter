package at.ac.ase.inso.group02.recommendation.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Objects;

import at.ac.ase.inso.group02.authentication.UserService;
import at.ac.ase.inso.group02.authentication.dto.UserDetailDTO;
import at.ac.ase.inso.group02.recommendation.RecommendationService;
import at.ac.ase.inso.group02.skills.SkillOfferService;
import at.ac.ase.inso.group02.skills.dto.SkillDemandDTO;
import at.ac.ase.inso.group02.skills.dto.SkillOfferDTO;
import at.ac.ase.inso.group02.skills.dto.SkillQueryParamsDTO;
import at.ac.ase.inso.group02.util.pagination.PaginatedQueryDTO;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Implementation of the RecommendationService interface.
 * Provides recommendation logic based on geographic proximity, skill categories,
 * provider ratings, and title keyword matching.
 */
@ApplicationScoped
public class RecommendationServiceImpl implements RecommendationService {
    private static final double MINIMUM_RATING = 7.5;
    private static final double DEFAULT_RADIUS = 2500.0;

    private final SkillOfferService skillService;
    private final UserService userService;
    
    @Inject
    public RecommendationServiceImpl(SkillOfferService skillService, UserService userService) {
        this.skillService = Objects.requireNonNull(skillService, "skillService cannot be null");
        this.userService = Objects.requireNonNull(userService, "userService cannot be null");
    }

    /**
     * {@inheritDoc}
     * Filters recommendations based on:
     * 1. Geographic proximity (within 2.5km radius)
     * 2. Skill category matching
     * 3. Provider rating (minimum 3.75 stars)
     * 4. Title keyword matching between demand and offer
     *
     * @throws IllegalArgumentException if demandDTO is null or has invalid data
     * @throws IllegalStateException if user data is not available or incomplete
     */
    @Override
    public PaginatedQueryDTO<SkillOfferDTO> getRecommendations(SkillDemandDTO demandDTO) {
        validateDemandDTO(demandDTO);
        UserDetailDTO user = validateAndGetUser();
        SkillQueryParamsDTO params = buildQueryParams(demandDTO, user);
        
        PaginatedQueryDTO<SkillOfferDTO> skills = this.skillService.getSkillsFiltered(params);
        if (skills == null || skills.getItems() == null || skills.getItems().isEmpty()) {
            Log.info("No recommended skills could be found.");
            return createEmptyResponse();
        }
        Log.info("Found " + skills.getTotal() + " skills.");
        
        List<SkillOfferDTO> filteredItems = filterSkillOffers(skills.getItems(), demandDTO);
        
        return buildPaginatedResponse(skills, filteredItems);
    }

    private void validateDemandDTO(SkillDemandDTO demandDTO) {
        if (demandDTO == null) {
            throw new IllegalArgumentException("demandDTO cannot be null");
        }
        if (demandDTO.getTitle() == null || demandDTO.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("demand title cannot be null or empty");
        }
        if (demandDTO.getCategory() == null || demandDTO.getCategory().getId() == null) {
            throw new IllegalArgumentException("demand category and category ID cannot be null");
        }
    }

    private UserDetailDTO validateAndGetUser() {
        UserDetailDTO user = this.userService.getCurrentUser();
        if (user == null) {
            throw new IllegalStateException("Current user not found");
        }
        if (user.getLocation() == null) {
            throw new IllegalStateException("User location not set");
        }
        if (user.getLocation().getHomeLocation() == null) {
            throw new IllegalStateException("User home location not set");
        }
        return user;
    }

    private SkillQueryParamsDTO buildQueryParams(SkillDemandDTO demandDTO, UserDetailDTO user) {
        SkillQueryParamsDTO params = new SkillQueryParamsDTO();
        Set<Long> categoryIds = Set.of(demandDTO.getCategory().getId());
        params.setCategory(categoryIds);
        params.setRadius(DEFAULT_RADIUS);
        params.setIncludeOwn(false);
        params.setLat(user.getLocation().getHomeLocation().getX());
        params.setLon(user.getLocation().getHomeLocation().getY());
        return params;
    }

    private List<SkillOfferDTO> filterSkillOffers(Collection<SkillOfferDTO> items, SkillDemandDTO demandDTO) {
        List<SkillOfferDTO> filteredItems = new ArrayList<>(items);
        
        // Filter by ratings
        filteredItems.removeIf(skillOffer -> 
            skillOffer == null || 
            (skillOffer.getAverageRatingHalfStars() != null && 
             skillOffer.getAverageRatingHalfStars() != 0 && 
             skillOffer.getAverageRatingHalfStars() < MINIMUM_RATING)
        );
        Log.info("" + filteredItems.size() + " skills remaining after rating filter.");
        
        // Filter by matching words
        filteredItems.removeIf(skillOffer -> 
            skillOffer == null || 
            skillOffer.getTitle() == null || 
            !hasMatchingWords(skillOffer, demandDTO)
        );
        Log.info("" + filteredItems.size() + " skills remaining after title matching filter.");
        
        return filteredItems;
    }

    private boolean hasMatchingWords(SkillOfferDTO skillOffer, SkillDemandDTO demandDTO) {
        if (skillOffer == null || skillOffer.getTitle() == null || demandDTO == null || demandDTO.getTitle() == null) {
            return false;
        }
        String[] demandWords = demandDTO.getTitle().toLowerCase().split("\\s+");
        String offerTitle = skillOffer.getTitle().toLowerCase();
        return Arrays.stream(demandWords)
                    .filter(Objects::nonNull)
                    .anyMatch(word -> word != null && !word.trim().isEmpty() && offerTitle.contains(word));
    }

    private PaginatedQueryDTO<SkillOfferDTO> buildPaginatedResponse(
            PaginatedQueryDTO<SkillOfferDTO> originalQuery,
            List<SkillOfferDTO> filteredItems) {
        if (originalQuery == null || filteredItems == null) {
            return createEmptyResponse();
        }
        return PaginatedQueryDTO.<SkillOfferDTO>builder()
                .page(originalQuery.getPage())
                .pageSize(originalQuery.getPageSize())
                .total(filteredItems.size())
                .hasMore(filteredItems.size() > (originalQuery.getPage() + 1) * originalQuery.getPageSize())
                .items(filteredItems)
                .build();
    }

    private PaginatedQueryDTO<SkillOfferDTO> createEmptyResponse() {
        return PaginatedQueryDTO.<SkillOfferDTO>builder()
                .page(0)
                .pageSize(10)
                .total(0)
                .hasMore(false)
                .items(new ArrayList<>())
                .build();
    }
}