package at.ac.ase.inso.group02.skills.impl;

import at.ac.ase.inso.group02.authentication.AuthenticationService;
import at.ac.ase.inso.group02.entities.SkillOffer;
import at.ac.ase.inso.group02.rating.RatingService;
import at.ac.ase.inso.group02.skills.GenericSkillRepository;
import at.ac.ase.inso.group02.skills.SkillCategoryRepository;
import at.ac.ase.inso.group02.skills.SkillOfferService;
import at.ac.ase.inso.group02.skills.dto.CreateSkillOfferDTO;
import at.ac.ase.inso.group02.skills.dto.SkillOfferDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Validator;
import lombok.NoArgsConstructor;

@ApplicationScoped
@NoArgsConstructor
public class SkillOfferServiceImpl extends SkillSubclassServiceImpl<SkillOfferDTO, CreateSkillOfferDTO, SkillOffer> implements SkillOfferService {

    @Inject
    public SkillOfferServiceImpl(AuthenticationService authenticationService, GenericSkillRepository<SkillOffer> skillRepository, RatingService ratingService, GenericSkillRepository<SkillOffer> skillRepository1, SkillCategoryRepository skillCategoryRepository, Validator validator) {
        super(authenticationService, skillRepository, ratingService, skillRepository1, skillCategoryRepository, validator);
    }

    @Override
    protected void updateSubtype(CreateSkillOfferDTO updateSkillDTO, SkillOffer skill) {
        skill.setSchedule(updateSkillDTO.getSchedule());
    }
}
