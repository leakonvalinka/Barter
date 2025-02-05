package at.ac.ase.inso.group02.skills.impl;

import at.ac.ase.inso.group02.rating.dto.UserRatingDTO;
import at.ac.ase.inso.group02.rating.views.RatingViews;
import at.ac.ase.inso.group02.skills.SkillOfferController;
import at.ac.ase.inso.group02.skills.SkillOfferService;
import at.ac.ase.inso.group02.skills.dto.CreateSkillOfferDTO;
import at.ac.ase.inso.group02.skills.dto.SkillOfferDTO;
import at.ac.ase.inso.group02.skills.dto.SkillQueryParamsDTO;
import at.ac.ase.inso.group02.util.pagination.PaginatedQueryDTO;
import at.ac.ase.inso.group02.util.pagination.PaginationParamsDTO;
import at.ac.ase.inso.group02.views.Views;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.inject.Inject;
import jakarta.validation.Valid;

public class SkillOfferControllerImpl extends GenericSkillSubtypeControllerImpl<SkillOfferDTO, CreateSkillOfferDTO> implements SkillOfferController {

    @Inject
    public SkillOfferControllerImpl(SkillOfferService skillService) {
        super(skillService);
    }

    /*
    need to override because @JsonView annotation is not inherited, but needed on this level here
    (base methods need @JsonView too for Swagger UI)
    */
    @Override
    @JsonView(Views.Brief.class)
    public SkillOfferDTO createSkill(@Valid CreateSkillOfferDTO createSkillDTO) {
        return super.createSkill(createSkillDTO);
    }

    @Override
    @JsonView(Views.Brief.class)
    public SkillOfferDTO updateSkill(Long id, @Valid CreateSkillOfferDTO updateSkillDTO) {
        return super.updateSkill(id, updateSkillDTO);
    }

    @Override
    @JsonView(Views.Full.class)
    public SkillOfferDTO getSkill(Long id) {
        return super.getSkill(id);
    }


    @Override
    @JsonView(RatingViews.IncludeByUser.class)
    public PaginatedQueryDTO<UserRatingDTO> getSkillRatings(Long id, PaginationParamsDTO paginationParamsDTO) {
        return super.getSkillRatings(id, paginationParamsDTO);
    }

    @Override
    @JsonView(Views.Brief.class)
    public PaginatedQueryDTO<SkillOfferDTO> getSkills(SkillQueryParamsDTO params) {
        return super.getSkills(params);
    }
}
