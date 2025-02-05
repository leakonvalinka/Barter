package at.ac.ase.inso.group02.skills.impl;

import at.ac.ase.inso.group02.entities.SkillCategory;
import at.ac.ase.inso.group02.skills.SkillCategoryRepository;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.stream.Stream;

@ApplicationScoped
public class SkillCategoryRepositoryImpl implements SkillCategoryRepository {
    @Override
    public Stream<SkillCategory> findByQueryMatch(String query) {
        String sanitizedQuery = "%" + query.toLowerCase() + "%";

        return find("LOWER(name) LIKE :query OR LOWER(description) LIKE :query",
                Parameters.with("query", sanitizedQuery)).stream();
    }
}
