package at.ac.ase.inso.group02.skills.impl;

import at.ac.ase.inso.group02.authentication.AuthenticationService;
import at.ac.ase.inso.group02.entities.Skill;
import at.ac.ase.inso.group02.entities.SkillCategory;
import at.ac.ase.inso.group02.entities.User;
import at.ac.ase.inso.group02.exceptions.UnauthorizedModificationException;
import at.ac.ase.inso.group02.rating.RatingService;
import at.ac.ase.inso.group02.skills.GenericSkillRepository;
import at.ac.ase.inso.group02.skills.SkillCategoryRepository;
import at.ac.ase.inso.group02.skills.SkillSubclassService;
import at.ac.ase.inso.group02.skills.dto.CreateSkillDTO;
import at.ac.ase.inso.group02.skills.dto.SkillDTO;
import at.ac.ase.inso.group02.skills.exception.SkillCategoryDoesNotExistException;
import at.ac.ase.inso.group02.util.MapperUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.NoArgsConstructor;

import java.util.Set;

@NoArgsConstructor
public abstract class SkillSubclassServiceImpl<D extends SkillDTO, C extends CreateSkillDTO, S extends Skill> extends GenericSkillServiceImpl<D, S> implements SkillSubclassService<D, C> {

    private AuthenticationService authenticationService;
    private GenericSkillRepository<S> skillRepository;
    private SkillCategoryRepository skillCategoryRepository;
    private Validator validator;

    @Inject
    public SkillSubclassServiceImpl(AuthenticationService authenticationService, GenericSkillRepository<S> skillRepository, RatingService ratingService, GenericSkillRepository<S> skillRepository1, SkillCategoryRepository skillCategoryRepository, Validator validator) {
        super(authenticationService, skillRepository, ratingService);
        this.authenticationService = authenticationService;
        this.skillRepository = skillRepository1;
        this.skillCategoryRepository = skillCategoryRepository;
        this.validator = validator;
    }

    @Override
    @Transactional
    public D createSkill(C createSkillDTO) {

        // validate DTO constraints (if not already done in the Controller
        Set<ConstraintViolation<CreateSkillDTO>> violations = validator.validate(createSkillDTO);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        S skill = MapperUtil.map(createSkillDTO, new TypeReference<>() {
        });

        // set currently authenticated user
        User currentUser = authenticationService.getCurrentUser();
        skill.setByUser(currentUser);

        // fetch skill-category
        SkillCategory skillCategory = getSkillCategory(createSkillDTO.getCategory().getId());
        skill.setCategory(skillCategory);

        // save the skill
        skillRepository.persistAndFlush(skill);

        return mapSkillWithUser(skill);
    }


    @Override
    @Transactional
    public D updateSkill(Long id, C updateSkillDTO) {

        // validate DTO constraints (if not already done in the Controller
        Set<ConstraintViolation<C>> violations = validator.validate(updateSkillDTO);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        S skill = getSkillEntityById(id);

        User currentUser = authenticationService.getCurrentUser();
        if (!currentUser.equals(skill.getByUser())) {
            throw new UnauthorizedModificationException("You do not have permission to edit that skill");
        }

        // fetch skill-category
        SkillCategory skillCategory = getSkillCategory(updateSkillDTO.getCategory().getId());

        skill.setCategory(skillCategory);

        // update remaining updatable fields
        skill.setTitle(updateSkillDTO.getTitle());
        skill.setDescription(updateSkillDTO.getDescription());

        updateSubtype(updateSkillDTO, skill);

        // save the skill
        skillRepository.persistAndFlush(skill);
        return mapSkillWithUser(skill);
    }

    protected void updateSubtype(C updateSkillDTO, S skill) {
        throw new UnsupportedOperationException("Subclasses must implement this method");
    }

    private SkillCategory getSkillCategory(Long id) {
        SkillCategory skillCategory = skillCategoryRepository.findById(id);
        if (skillCategory == null) {
            throw new SkillCategoryDoesNotExistException("Invalid Skill Category");
        }
        return skillCategory;
    }
}
