package at.ac.ase.inso.group02.skills.impl;

import at.ac.ase.inso.group02.skills.GenericSkillSubtypeController;
import at.ac.ase.inso.group02.skills.SkillSubclassService;
import at.ac.ase.inso.group02.skills.dto.CreateSkillDTO;
import at.ac.ase.inso.group02.skills.dto.SkillDTO;
import at.ac.ase.inso.group02.views.Views;
import com.fasterxml.jackson.annotation.JsonView;
import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import jakarta.validation.Valid;

public abstract class GenericSkillSubtypeControllerImpl<D extends SkillDTO, C extends CreateSkillDTO> extends GenericSkillControllerImpl<D> implements GenericSkillSubtypeController<D, C> {

    SkillSubclassService<D, C> skillService;

    @Inject
    public GenericSkillSubtypeControllerImpl(SkillSubclassService<D, C> skillService) {
        super(skillService);
        this.skillService = skillService;
    }

    @Override
    @JsonView(Views.Brief.class)
    public D createSkill(@Valid C createSkillDTO) {
        Log.infov("Creating skill: {0}", createSkillDTO);
        return skillService.createSkill(createSkillDTO);
    }

    @Override
    @JsonView(Views.Brief.class)
    public D updateSkill(Long id, @Valid C updateSkillDTO) {
        Log.infov("Updating skill {0} with values: {1}", id, updateSkillDTO);
        return skillService.updateSkill(id, updateSkillDTO);
    }
}
