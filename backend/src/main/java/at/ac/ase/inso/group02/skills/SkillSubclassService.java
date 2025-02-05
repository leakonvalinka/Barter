package at.ac.ase.inso.group02.skills;

import at.ac.ase.inso.group02.exceptions.UnauthorizedModificationException;
import at.ac.ase.inso.group02.skills.dto.CreateSkillDTO;
import at.ac.ase.inso.group02.skills.dto.SkillDTO;
import at.ac.ase.inso.group02.skills.exception.SkillCategoryDoesNotExistException;
import at.ac.ase.inso.group02.skills.exception.SkillDoesNotExistException;
import jakarta.validation.ConstraintViolationException;

/**
 * service for operations applicable to a single concrete type of skill (i.e. skill-offer or skill-demand)
 * (i.e. D and C are true subclasses of SkillDTO and CreateSkillDTO respectively)
 */
public interface SkillSubclassService<D extends SkillDTO, C extends CreateSkillDTO> extends GenericSkillService<D> {

    /**
     * creates a skill and save it
     *
     * @param createSkillDTO data of the skill to create
     * @return data representing the saved skill
     * @throws ConstraintViolationException       if the input data is invalid
     * @throws SkillCategoryDoesNotExistException if the given skill-category id does not
     *                                            correspond to an existing skill-category
     */
    D createSkill(C createSkillDTO);

    /**
     * updates a skill that is already present
     *
     * @param id             id of the skill to update
     * @param updateSkillDTO (full) data of the skill to update
     * @return data representing the updated skill
     * @throws ConstraintViolationException       if the input data is invalid
     * @throws UnauthorizedModificationException  if the call is an attempt to edit a skill
     *                                            that was not created by the currently authenticated user
     * @throws SkillDoesNotExistException         if there is no skill of the requested type with the given id
     * @throws SkillCategoryDoesNotExistException if the given skill-category id does not
     *                                            correspond to an existing skill-category
     */
    D updateSkill(Long id, C updateSkillDTO);
}
