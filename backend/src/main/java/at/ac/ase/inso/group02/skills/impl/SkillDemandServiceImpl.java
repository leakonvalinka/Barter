package at.ac.ase.inso.group02.skills.impl;

import at.ac.ase.inso.group02.authentication.AuthenticationService;
import at.ac.ase.inso.group02.entities.SkillDemand;
import at.ac.ase.inso.group02.rating.RatingService;
import at.ac.ase.inso.group02.skills.GenericSkillRepository;
import at.ac.ase.inso.group02.skills.SkillCategoryRepository;
import at.ac.ase.inso.group02.skills.SkillDemandService;
import at.ac.ase.inso.group02.skills.dto.CreateSkillDemandDTO;
import at.ac.ase.inso.group02.skills.dto.SkillDemandDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Validator;
import lombok.NoArgsConstructor;

@ApplicationScoped
@NoArgsConstructor
public class SkillDemandServiceImpl extends SkillSubclassServiceImpl<SkillDemandDTO, CreateSkillDemandDTO, SkillDemand> implements SkillDemandService {

    @Inject
    public SkillDemandServiceImpl(AuthenticationService authenticationService, GenericSkillRepository<SkillDemand> skillRepository, RatingService ratingService, GenericSkillRepository<SkillDemand> skillRepository1, SkillCategoryRepository skillCategoryRepository, Validator validator) {
        super(authenticationService, skillRepository, ratingService, skillRepository1, skillCategoryRepository, validator);
    }

    @Override
    protected void updateSubtype(CreateSkillDemandDTO updateSkillDTO, SkillDemand skill) {
        skill.setUrgency(updateSkillDTO.getUrgency());
    }
}
