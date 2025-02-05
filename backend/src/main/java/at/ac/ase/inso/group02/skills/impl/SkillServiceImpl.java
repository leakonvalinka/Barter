package at.ac.ase.inso.group02.skills.impl;

import at.ac.ase.inso.group02.authentication.AuthenticationService;
import at.ac.ase.inso.group02.entities.Skill;
import at.ac.ase.inso.group02.rating.RatingService;
import at.ac.ase.inso.group02.skills.GenericSkillRepository;
import at.ac.ase.inso.group02.skills.SkillService;
import at.ac.ase.inso.group02.skills.dto.SkillDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class SkillServiceImpl extends GenericSkillServiceImpl<SkillDTO, Skill> implements SkillService {

    @Inject
    public SkillServiceImpl(AuthenticationService authenticationService, GenericSkillRepository<Skill> skillRepository, RatingService ratingService) {
        super(authenticationService, skillRepository, ratingService);
    }
}
