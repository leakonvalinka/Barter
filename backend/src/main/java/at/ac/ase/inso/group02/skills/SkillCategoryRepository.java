package at.ac.ase.inso.group02.skills;

import at.ac.ase.inso.group02.entities.SkillCategory;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

import java.util.stream.Stream;

/**
 * handles database operations of Skill-categories
 */
public interface SkillCategoryRepository extends PanacheRepository<SkillCategory> {

    /**
     * @param query an arbitrary text query
     * @return SkillCategories that somehow match the query
     * (e.g. a field value matches the query using SQL "LIKE" semantics)
     */
    Stream<SkillCategory> findByQueryMatch(String query);
}
