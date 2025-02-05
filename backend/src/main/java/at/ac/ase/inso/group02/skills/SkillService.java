package at.ac.ase.inso.group02.skills;

import at.ac.ase.inso.group02.entities.Skill;
import at.ac.ase.inso.group02.skills.dto.SkillDTO;

/**
 * service for operations on all types of skills (offers and demands simultaneously)
 */
public interface SkillService extends GenericSkillService<SkillDTO> {
    Skill getSkillEntityById(Long id);
}
