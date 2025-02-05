package at.ac.ase.inso.group02.skills;

import at.ac.ase.inso.group02.skills.dto.SkillCategoryDTO;

import java.util.Set;

/**
 * service for SkillCategory operations
 */
public interface SkillCategoryService {
    /**
     * @param filterQuery a query to match values of the skill-category. if not null, will restrict
     *                    the result set to only matches
     * @return a set of all known Skill-categories, possibly restricted by matches with filterQuery
     */
    Set<SkillCategoryDTO> findByQuery(String filterQuery);

    /**
     * @param id id of a skill-category to look up
     * @return information about the skill-category with the given id
     * @throws at.ac.ase.inso.group02.skills.exception.SkillCategoryDoesNotExistException if there is no skill-category with the given id
     */
    SkillCategoryDTO findById(Long id);
}
