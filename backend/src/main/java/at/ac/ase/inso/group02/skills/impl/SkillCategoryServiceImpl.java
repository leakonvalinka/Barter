package at.ac.ase.inso.group02.skills.impl;

import at.ac.ase.inso.group02.entities.SkillCategory;
import at.ac.ase.inso.group02.skills.SkillCategoryRepository;
import at.ac.ase.inso.group02.skills.SkillCategoryService;
import at.ac.ase.inso.group02.skills.dto.SkillCategoryDTO;
import at.ac.ase.inso.group02.skills.exception.SkillCategoryDoesNotExistException;
import at.ac.ase.inso.group02.util.MapperUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
@AllArgsConstructor
public class SkillCategoryServiceImpl implements SkillCategoryService {

    SkillCategoryRepository skillCategoryRepository;

    @Override
    @Transactional
    public Set<SkillCategoryDTO> findByQuery(String filterQuery) {
        return (filterQuery == null
                ? skillCategoryRepository.findAll().stream()
                : skillCategoryRepository.findByQueryMatch(filterQuery))
                .map(cat -> MapperUtil.map(cat, SkillCategoryDTO.class))
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional
    public SkillCategoryDTO findById(Long id) {
        SkillCategory skillCategory = skillCategoryRepository.findById(id);

        if (skillCategory == null) {
            throw new SkillCategoryDoesNotExistException("Skill category with id " + id + " not found");
        }

        return MapperUtil.map(skillCategory, SkillCategoryDTO.class);
    }
}
