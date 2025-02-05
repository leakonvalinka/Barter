package at.ac.ase.inso.group02.skills.impl;

import at.ac.ase.inso.group02.skills.SkillCategoryController;
import at.ac.ase.inso.group02.skills.SkillCategoryService;
import at.ac.ase.inso.group02.skills.dto.SkillCategoryDTO;
import at.ac.ase.inso.group02.views.Views;
import com.fasterxml.jackson.annotation.JsonView;
import io.quarkus.logging.Log;
import lombok.AllArgsConstructor;

import java.util.Set;

@AllArgsConstructor
public class SkillCategoryControllerImpl implements SkillCategoryController {

    SkillCategoryService skillCategoryService;

    @Override
    @JsonView(Views.Full.class)
    public Set<SkillCategoryDTO> getCategories(String q) {
        Log.infov("Fetching skill categories with query: {0}", q);
        return skillCategoryService.findByQuery(q);
    }

    @Override
    @JsonView(Views.Full.class)
    public SkillCategoryDTO getCategoryById(Long id) {
        Log.infov("Fetching skill category {0}", id);
        return skillCategoryService.findById(id);
    }
}
