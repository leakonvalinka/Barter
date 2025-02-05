package at.ac.ase.inso.group02.skills.impl;

import at.ac.ase.inso.group02.rating.dto.UserRatingDTO;
import at.ac.ase.inso.group02.rating.views.RatingViews;
import at.ac.ase.inso.group02.skills.SkillController;
import at.ac.ase.inso.group02.skills.SkillService;
import at.ac.ase.inso.group02.skills.dto.SkillDTO;
import at.ac.ase.inso.group02.skills.dto.SkillQueryParamsDTO;
import at.ac.ase.inso.group02.util.pagination.PaginatedQueryDTO;
import at.ac.ase.inso.group02.util.pagination.PaginationParamsDTO;
import at.ac.ase.inso.group02.views.Views;
import com.fasterxml.jackson.annotation.JsonView;

public class SkillControllerImpl extends GenericSkillControllerImpl<SkillDTO> implements SkillController {
    public SkillControllerImpl(SkillService skillService) {
        super(skillService);
    }

    @Override
    @JsonView(Views.ExplicitlyTypedBrief.class)
    public PaginatedQueryDTO<SkillDTO> getSkills(SkillQueryParamsDTO params) {
        return super.getSkills(params);
    }

    @Override
    @JsonView(Views.ExplicitlyTypedFull.class)
    public SkillDTO getSkill(Long id) {
        return super.getSkill(id);
    }

    @Override
    @JsonView(RatingViews.IncludeByUser.class)
    public PaginatedQueryDTO<UserRatingDTO> getSkillRatings(Long id, PaginationParamsDTO paginationParamsDTO) {
        return super.getSkillRatings(id, paginationParamsDTO);
    }
}
