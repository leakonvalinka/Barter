package at.ac.ase.inso.group02.rating.impl;

import at.ac.ase.inso.group02.authentication.AuthenticationService;
import at.ac.ase.inso.group02.authentication.UserRepository;
import at.ac.ase.inso.group02.bartering.ExchangeRepository;
import at.ac.ase.inso.group02.bartering.exception.ExchangeNotRatableException;
import at.ac.ase.inso.group02.entities.Skill;
import at.ac.ase.inso.group02.entities.User;
import at.ac.ase.inso.group02.entities.exchange.ExchangeItem;
import at.ac.ase.inso.group02.entities.rating.InitiatorRating;
import at.ac.ase.inso.group02.entities.rating.ResponderRating;
import at.ac.ase.inso.group02.entities.rating.UserRating;
import at.ac.ase.inso.group02.exceptions.UnauthorizedCreationException;
import at.ac.ase.inso.group02.exceptions.UnauthorizedModificationException;
import at.ac.ase.inso.group02.rating.*;
import at.ac.ase.inso.group02.rating.dto.CreateRatingDTO;
import at.ac.ase.inso.group02.rating.dto.UserRatingDTO;
import at.ac.ase.inso.group02.rating.exception.RatingAlreadyExistsException;
import at.ac.ase.inso.group02.skills.GenericSkillRepository;
import at.ac.ase.inso.group02.util.MapperUtil;
import at.ac.ase.inso.group02.util.pagination.PaginatedQueryDTO;
import at.ac.ase.inso.group02.util.pagination.PaginationParamsDTO;
import at.ac.ase.inso.group02.util.pagination.PaginationUtil;
import com.fasterxml.jackson.databind.JsonMappingException;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import lombok.AllArgsConstructor;

import java.util.IntSummaryStatistics;

@ApplicationScoped
@AllArgsConstructor
public class RatingServiceImpl implements RatingService {

    private UserRepository userRepository;

    private RatingRepository ratingRepository;
    private InitiatorRatingRepository initiatorRatingRepository;
    private ResponderRatingRepository responderRatingRepository;

    private ExchangeRepository exchangeRepository;

    private GenericSkillRepository<Skill> skillRepository;

    private AuthenticationService authenticationService;

    @Override
    @Transactional
    public PaginatedQueryDTO<UserRatingDTO> getUserRatings(String username, PaginationParamsDTO paginationParamsDTO) {
        User user = userRepository.findByUsername(username);

        if (user == null) {
            throw new NotFoundException("No user found with username " + username);
        }

        return PaginationUtil.getPaginatedQueryDTO(
                paginationParamsDTO,
                ratingRepository.findRatingsForUser(user),
                r -> MapperUtil.map(r, UserRatingDTO.class)
        );
    }

    @Override
    @Transactional
    public PaginatedQueryDTO<UserRatingDTO> getSkillRatings(Skill skill, PaginationParamsDTO paginationParamsDTO) {
        return PaginationUtil.getPaginatedQueryDTO(
                paginationParamsDTO,
                initiatorRatingRepository.findRatingsForSkill(skill),
                r -> MapperUtil.map(r, UserRatingDTO.class)
        );
    }

    @Override
    @Transactional
    public UserRatingDTO createRatingForExchange(Long exchangeID, CreateRatingDTO rating) {
        User user = authenticationService.getCurrentUser();
        ExchangeItem exchangeItem = exchangeRepository.findById(exchangeID);

        if (exchangeItem == null) {
            throw new NotFoundException("No exchange found with id " + exchangeID);
        }

        if (!exchangeItem.isRatable()){
            throw new ExchangeNotRatableException("Exchange is not ratable");
        }

        UserRating userRating;
        if (user.equals(exchangeItem.getInitiator())) {
            verifyRatingDoesNotExist(exchangeItem, initiatorRatingRepository);
            InitiatorRating initiatorRating = MapperUtil.map(rating, InitiatorRating.class);
            initiatorRating.setInitiatorExchange(exchangeItem);
            userRating = initiatorRating;

        } else if (user.equals(exchangeItem.getExchangedSkill().getByUser())) {
            verifyRatingDoesNotExist(exchangeItem, responderRatingRepository);
            ResponderRating responderRating = MapperUtil.map(rating, ResponderRating.class);
            responderRating.setResponderExchange(exchangeItem);
            userRating = responderRating;

        } else {
            throw new UnauthorizedCreationException("You are not allowed to create a rating for the exchange, since you were not part of it");
        }

        ratingRepository.persistAndFlush(userRating);
        updateRatingOfUser(userRating.getForUser());
        updateRatingOfSkill(exchangeItem.getExchangedSkill());
        return MapperUtil.map(userRating, UserRatingDTO.class);
    }

    @Override
    @Transactional
    public UserRatingDTO updateRating(Long ratingID, CreateRatingDTO ratingDTO) {
        UserRating userRating = ratingRepository.findById(ratingID);

        if (userRating == null) {
            throw new NotFoundException("No such rating found with id " + ratingID);
        }

        if (!authenticationService.getCurrentUser().equals(userRating.getByUser())) {
            throw new UnauthorizedModificationException("You are not allowed to update this rating");
        }

        try {
            MapperUtil.updateEntity(userRating, ratingDTO);
        } catch (JsonMappingException e) {
            throw new IllegalStateException("Malformed input. If you see this error, please contact the administrator");
        }

        ratingRepository.persistAndFlush(userRating);
        updateRatingOfUser(userRating.getForUser());
        updateRatingOfSkill(userRating.getForSkill());
        return MapperUtil.map(userRating, UserRatingDTO.class);
    }

    @Override
    @Transactional
    public boolean deleteRating(Long ratingID) {
        UserRating userRating = ratingRepository.findById(ratingID);

        if (userRating == null) {
            throw new NotFoundException("No such rating found with id " + ratingID);
        }

        if (!authenticationService.getCurrentUser().equals(userRating.getByUser())) {
            throw new UnauthorizedModificationException("You are not allowed to delete this rating");
        }

        ratingRepository.delete(userRating);
        ratingRepository.flush();
        updateRatingOfUser(userRating.getForUser());
        updateRatingOfSkill(userRating.getForSkill());
        return true;
    }

    private void verifyRatingDoesNotExist(ExchangeItem exchangeItem, RatingSubclassRepository<? extends UserRating> repository) {
        if (repository.findByExchange(exchangeItem) != null) {
            throw new RatingAlreadyExistsException("You already left a rating for this exchange!");
        }
    }

    private void updateRatingOfUser(User user) {
        IntSummaryStatistics stats = ratingRepository.findRatingsForUser(user)
                .stream()
                .map(UserRating::getRatingHalfStars)
                .mapToInt(i -> i)
                .summaryStatistics();

        user.setAverageRatingHalfStars(stats.getAverage());
        user.setNumberOfRatings(stats.getCount());
        userRepository.persistUser(user);
    }

    private void updateRatingOfSkill(Skill skill) {
        IntSummaryStatistics stats = initiatorRatingRepository.findRatingsForSkill(skill)
                .stream()
                .map(UserRating::getRatingHalfStars)
                .mapToInt(i -> i)
                .summaryStatistics();

        skill.setAverageRatingHalfStars(stats.getAverage());
        skill.setNumberOfRatings(stats.getCount());
        skillRepository.persistAndFlush(skill);
    }
}
