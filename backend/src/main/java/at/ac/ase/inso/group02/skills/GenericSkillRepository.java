package at.ac.ase.inso.group02.skills;

import at.ac.ase.inso.group02.entities.Skill;
import at.ac.ase.inso.group02.entities.User;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import org.locationtech.jts.geom.Point;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * generic repository for skills and subtypes
 *
 * @param <S>
 */
public interface GenericSkillRepository<S extends Skill> extends PanacheRepository<S> {

    /**
     * Retrieve skills near a point of reference and filter by different parameters
     *
     * @param categoryIDs    if not-null and not-empty, results are restricted to categories with one of the given IDs
     * @param referencePoint (rough) point of reference for the search
     * @param radius         restricts the results to be offered within a given distance from the referencePoint
     * @param ignoreUsers    restricts the results only to posts created by users NOT in ignoreUsers
     * @return a query object for the filtered skills (can be used for pagination)
     */
    PanacheQuery<S> getSkillsFiltered(Collection<Long> categoryIDs, Point referencePoint, Double radius, Set<User> ignoreUsers);

    default List<S> findByUser(User user) {
        return list("byUser", user);
    }
}
