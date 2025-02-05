package at.ac.ase.inso.group02.skills.impl;

import at.ac.ase.inso.group02.rating.dto.UserRatingDTO;
import at.ac.ase.inso.group02.rating.views.RatingViews;
import at.ac.ase.inso.group02.skills.SkillDemandController;
import at.ac.ase.inso.group02.skills.SkillDemandService;
import at.ac.ase.inso.group02.skills.dto.CreateSkillDemandDTO;
import at.ac.ase.inso.group02.skills.dto.SkillDemandDTO;
import at.ac.ase.inso.group02.skills.dto.SkillQueryParamsDTO;
import at.ac.ase.inso.group02.util.pagination.PaginatedQueryDTO;
import at.ac.ase.inso.group02.util.pagination.PaginationParamsDTO;
import at.ac.ase.inso.group02.views.Views;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.inject.Inject;
import jakarta.validation.Valid;

public class SkillDemandControllerImpl extends GenericSkillSubtypeControllerImpl<SkillDemandDTO, CreateSkillDemandDTO> implements SkillDemandController {

    @Inject
    public SkillDemandControllerImpl(SkillDemandService skillService) {
        super(skillService);
    }

    /*
        need to override because @JsonView annotation is not inherited, but needed on this level here
        (base methods need @JsonView too for Swagger UI)
        */
    @Override
    @JsonView(Views.Brief.class)
    public SkillDemandDTO createSkill(@Valid CreateSkillDemandDTO createSkillDTO) {
        return super.createSkill(createSkillDTO);
    }

    @Override
    @JsonView(Views.Brief.class)
    public SkillDemandDTO updateSkill(Long id, @Valid CreateSkillDemandDTO updateSkillDTO) {
        return super.updateSkill(id, updateSkillDTO);
    }

    @Override
    @JsonView(Views.Full.class)
    public SkillDemandDTO getSkill(Long id) {
        return super.getSkill(id);
    }

    @Override
    @JsonView(RatingViews.IncludeByUser.class)
    public PaginatedQueryDTO<UserRatingDTO> getSkillRatings(Long id, PaginationParamsDTO paginationParamsDTO) {
        return super.getSkillRatings(id, paginationParamsDTO);
    }

    @Override
    @JsonView(Views.Brief.class)
    public PaginatedQueryDTO<SkillDemandDTO> getSkills(SkillQueryParamsDTO params) {
        return super.getSkills(params);
    }
}
