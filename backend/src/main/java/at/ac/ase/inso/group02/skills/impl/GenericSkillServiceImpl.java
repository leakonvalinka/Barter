package at.ac.ase.inso.group02.skills.impl;

import at.ac.ase.inso.group02.authentication.AuthenticationService;
import at.ac.ase.inso.group02.entities.Skill;
import at.ac.ase.inso.group02.exceptions.UnauthorizedModificationException;
import at.ac.ase.inso.group02.rating.RatingService;
import at.ac.ase.inso.group02.rating.dto.UserRatingDTO;
import at.ac.ase.inso.group02.skills.GenericSkillRepository;
import at.ac.ase.inso.group02.skills.GenericSkillService;
import at.ac.ase.inso.group02.skills.dto.SkillDTO;
import at.ac.ase.inso.group02.skills.dto.SkillQueryParamsDTO;
import at.ac.ase.inso.group02.skills.exception.SkillDoesNotExistException;
import at.ac.ase.inso.group02.util.MapperUtil;
import at.ac.ase.inso.group02.util.pagination.PaginatedQueryDTO;
import at.ac.ase.inso.group02.util.pagination.PaginationParamsDTO;
import at.ac.ase.inso.group02.util.pagination.PaginationUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.NotFoundException;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import java.util.Set;

@NoArgsConstructor
public class GenericSkillServiceImpl<D extends SkillDTO, S extends Skill> implements GenericSkillService<D> {

    private AuthenticationService authenticationService;

    private GenericSkillRepository<S> skillRepository;

    private RatingService ratingService;

    @Inject
    public GenericSkillServiceImpl(AuthenticationService authenticationService, GenericSkillRepository<S> skillRepository, RatingService ratingService) {
        this.authenticationService = authenticationService;
        this.skillRepository = skillRepository;
        this.ratingService = ratingService;
    }

    @Override
    @Transactional
    public D getSkillById(Long id) {
        return mapSkillWithUser(getSkillEntityById(id));
    }

    @Override
    @Transactional
    public PaginatedQueryDTO<UserRatingDTO> getSkillRatings(Long skillId, PaginationParamsDTO paginationParamsDTO) {
        Skill skill = getSkillEntityById(skillId);

        if (skill == null) throw new NotFoundException("A skill with id " + skillId + " does not exist");

        return ratingService.getSkillRatings(skill, paginationParamsDTO);
    }

    @Override
    @Transactional
    public boolean deleteSkillById(Long id) {
        S skill = getSkillEntityById(id);

        if (!authenticationService.getCurrentUser().equals(skill.getByUser())) {
            throw new UnauthorizedModificationException("You do not have permission to delete that skill");
        }

        skillRepository.delete(skill);
        return true;
    }

    @Override
    @Transactional
    public PaginatedQueryDTO<D> getSkillsFiltered(SkillQueryParamsDTO params) {


        Point referencePoint;

        if(params.getLat() != null && params.getLon() != null) {
            referencePoint = new GeometryFactory().createPoint(new Coordinate(params.getLat(), params.getLon()));
        } else {
            referencePoint = authenticationService.getCurrentUser().getLocation().getHomeLocation();
        }

        if(referencePoint == null) {
            throw new ConstraintViolationException("You don't have a home location set. You must manually set a reference point!", Set.of());
        }

        PanacheQuery<S> query = skillRepository.getSkillsFiltered(
                params.getCategory(),
                referencePoint,
                params.getRadius(),

                // TODO: also don't include skills by users that are blocked by the current user (?)
                params.isIncludeOwn() ? Set.of() : Set.of(authenticationService.getCurrentUser())
        );

        return PaginationUtil.getPaginatedQueryDTO(params, query, this::mapSkillWithUser);
    }

    public S getSkillEntityById(Long id) {
        S skill = skillRepository.findById(id);
        if (skill == null) {
            throw new SkillDoesNotExistException("Skill with id " + id + " does not exist");
        }
        return skill;
    }


    protected final D mapSkillWithUser(S skill) {
        D dto = MapperUtil.map(skill, new TypeReference<>() {
        });

        // we need to manually map the user, because Jackson's cyclic serialization prevention is just:
        // "I won't serialize one way of the cyclic references automatically"
        dto.setByUser(MapperUtil.map(skill.getByUser(), new TypeReference<>() {
        }));

        return dto;
    }
}
