package at.ac.ase.inso.group02.skills;

import at.ac.ase.inso.group02.exceptions.UnauthorizedModificationException;
import at.ac.ase.inso.group02.rating.dto.UserRatingDTO;
import at.ac.ase.inso.group02.skills.dto.SkillDTO;
import at.ac.ase.inso.group02.skills.dto.SkillQueryParamsDTO;
import at.ac.ase.inso.group02.skills.exception.SkillDoesNotExistException;
import at.ac.ase.inso.group02.util.pagination.PaginatedQueryDTO;
import at.ac.ase.inso.group02.util.pagination.PaginationParamsDTO;

/**
 * generic skill service for operations applicable to all skills
 * (all types of skills simultaneously or skill-demands/-offers separately)
 *
 * @param <D> the expected return-DTO type of the methods (e.g. SkillDTO for all types of skills simultaneously)
 */
public interface GenericSkillService<D extends SkillDTO> {

    /**
     * get a skill by its id
     *
     * @param id id of a skill
     * @return a DTO describing the skill
     * @throws SkillDoesNotExistException if no skill with the id exists or it is not the correct type
     *                                    (e.g. offer with id=2 queried but only a demand with id=2 exists)
     */
    D getSkillById(Long id);

    /**
     * retrieves user-ratings for a skill by its id (written by users that react to that skill posting, not its author)
     *
     * @param skillId             - of the skill to find ratings for
     * @param paginationParamsDTO pagination parameters
     * @return paginated ratings of the skill
     * @throws SkillDoesNotExistException if there is no skill with the given id or it is not the correct type
     *                                    (as with getSkillById)
     */
    PaginatedQueryDTO<UserRatingDTO> getSkillRatings(Long skillId, PaginationParamsDTO paginationParamsDTO);

    /**
     * deletes a skill by id
     *
     * @param id id of a skill to delete
     * @return true if the respective skill was found and deleted, false otherwise
     * @throws UnauthorizedModificationException if the call is an attempt to delete a skill
     *                                           that was not created by the currently authenticated user
     * @throws SkillDoesNotExistException        if no skill with the id exists or it is not the correct type
     */
    boolean deleteSkillById(Long id);

    /**
     * searches for skills and filters by the given constraints
     *
     * @param params constraints for the search, include spatial constraints (point of reference and radius)
     *               and ids of skill-categories. If no skill-category ids are provided, no category-constraint is applied
     * @return paginated skill results
     */
    PaginatedQueryDTO<D> getSkillsFiltered(SkillQueryParamsDTO params);
}
