package at.ac.ase.inso.group02.skills.impl;

import at.ac.ase.inso.group02.rating.dto.UserRatingDTO;
import at.ac.ase.inso.group02.rating.views.RatingViews;
import at.ac.ase.inso.group02.skills.GenericSkillController;
import at.ac.ase.inso.group02.skills.GenericSkillService;
import at.ac.ase.inso.group02.skills.dto.SkillDTO;
import at.ac.ase.inso.group02.skills.dto.SkillQueryParamsDTO;
import at.ac.ase.inso.group02.util.pagination.PaginatedQueryDTO;
import at.ac.ase.inso.group02.util.pagination.PaginationParamsDTO;
import at.ac.ase.inso.group02.views.Views;
import com.fasterxml.jackson.annotation.JsonView;
import io.quarkus.logging.Log;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class GenericSkillControllerImpl<D extends SkillDTO> implements GenericSkillController<D> {

    GenericSkillService<D> skillService;

    @Override
    @JsonView(Views.ExplicitlyTypedBrief.class)
    public PaginatedQueryDTO<D> getSkills(SkillQueryParamsDTO params) {
        Log.infov("Fetching skills with params: {0}", params);
        return skillService.getSkillsFiltered(params);
    }

    @Override
    @JsonView(Views.ExplicitlyTypedFull.class)
    public D getSkill(Long id) {
        Log.infov("Fetching skill {0}", id);
        return skillService.getSkillById(id);
    }

    @Override
    @JsonView(RatingViews.IncludeByUser.class)
    public PaginatedQueryDTO<UserRatingDTO> getSkillRatings(Long id, PaginationParamsDTO paginationParamsDTO) {
        Log.infov("Fetching ratings for skill {0} and pagination params: {1}", id, paginationParamsDTO);
        return skillService.getSkillRatings(id, paginationParamsDTO);
    }

    @Override
    public void deleteSkill(Long id) {
        Log.infov("Deleting skill {0}", id);
        skillService.deleteSkillById(id);
    }
}
