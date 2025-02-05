package at.ac.ase.inso.group02.skills.impl;

import at.ac.ase.inso.group02.entities.Skill;
import at.ac.ase.inso.group02.entities.SkillDemand;
import at.ac.ase.inso.group02.entities.SkillOffer;
import at.ac.ase.inso.group02.entities.User;
import at.ac.ase.inso.group02.skills.GenericSkillRepository;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.logging.Log;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;
import org.locationtech.jts.geom.Point;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public abstract class GenericSkillRepositoryImpl<S extends Skill> implements GenericSkillRepository<S> {
    @Override
    public PanacheQuery<S> getSkillsFiltered(Collection<Long> categoryIDs, Point referencePoint, Double radius, Set<User> ignoreUsers) {
        List<String> queryStringConstraints = new ArrayList<>();
        Parameters queryParams = new Parameters();

        if (categoryIDs != null && !categoryIDs.isEmpty()) {
            queryStringConstraints.add("category.id IN :categoryIDs");
            queryParams = queryParams.and("categoryIDs", categoryIDs);
        }

        Log.info(ignoreUsers);
        if (ignoreUsers != null && !ignoreUsers.isEmpty()) {
            queryStringConstraints.add("byUser NOT IN :ignoreUsers");
            queryParams = queryParams.and("ignoreUsers", ignoreUsers);
        }

        queryStringConstraints.add("""
                distance(
                    byUser.location.homeLocation, 
                    ST_SetSRID(:point, 4326)
                ) 
                < :radius
                """);

        queryParams = queryParams.and("point", referencePoint).and("radius", radius);

        return find(String.join(" and ", queryStringConstraints), queryParams);
    }

    @ApplicationScoped
    public static class SkillDemandRepositoryImpl extends GenericSkillRepositoryImpl<SkillDemand> {
    }

    @ApplicationScoped
    public static class SkillOfferRepositoryImpl extends GenericSkillRepositoryImpl<SkillOffer> {
    }

    @ApplicationScoped
    public static class SkillRepositoryImpl extends GenericSkillRepositoryImpl<Skill> {
    }
}

