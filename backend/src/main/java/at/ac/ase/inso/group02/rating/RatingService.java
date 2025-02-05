package at.ac.ase.inso.group02.rating;

import at.ac.ase.inso.group02.bartering.exception.ExchangeNotRatableException;
import at.ac.ase.inso.group02.entities.Skill;
import at.ac.ase.inso.group02.exceptions.UnauthorizedCreationException;
import at.ac.ase.inso.group02.exceptions.UnauthorizedModificationException;
import at.ac.ase.inso.group02.rating.dto.CreateRatingDTO;
import at.ac.ase.inso.group02.rating.dto.UserRatingDTO;
import at.ac.ase.inso.group02.rating.exception.RatingAlreadyExistsException;
import at.ac.ase.inso.group02.util.pagination.PaginatedQueryDTO;
import at.ac.ase.inso.group02.util.pagination.PaginationParamsDTO;
import jakarta.ws.rs.NotFoundException;

/**
 * Service for User-To-User Ratings
 */
public interface RatingService {

    /**
     * retrieves user-ratings for a user by username (written by any other user).
     *
     * @param username - of the user to fetch ratings for
     * @return paginated ratings of the user
     * @throws NotFoundException if there is no user with the given username
     */
    PaginatedQueryDTO<UserRatingDTO> getUserRatings(String username, PaginationParamsDTO paginationParamsDTO);

    /**
     * retrieves user-ratings for a skill (written by users that react to that skill posting, not its author)
     *
     * @param skill               - the skill to find ratings for
     * @param paginationParamsDTO pagination parameters
     * @return paginated ratings of the skill
     */
    PaginatedQueryDTO<UserRatingDTO> getSkillRatings(Skill skill, PaginationParamsDTO paginationParamsDTO);

    /**
     * creates a ratings for the exchange with given id,
     * from the authenticated user to the respective other party of the exchange
     *
     * @param exchangeID - of the exchange to rate
     * @param rating     - details of the rating created by the user
     * @return data of the created rating
     * @throws NotFoundException             if there is no exchange with the given id
     * @throws UnauthorizedCreationException if the current user was not part of the exchange and thus cannot leave a rating
     * @throws ExchangeNotRatableException   if the given exchange exists, but cannot yet accept ratings
     * @throws RatingAlreadyExistsException  is there already is a rating by the user for the exchange
     *                                       (in this case, it should be edited instead of created)
     */
    UserRatingDTO createRatingForExchange(Long exchangeID, CreateRatingDTO rating);


    /**
     * updates an existing rating
     *
     * @param ratingID  - of the rating to update
     * @param ratingDTO - details of the rating to update created by the user
     * @return data of the created rating
     * @throws NotFoundException                 if there is no rating with the given id
     * @throws UnauthorizedModificationException if the current user was not the author of the rating
     * @throws IllegalStateException             if the input data cannot be applied to the rating entity
     */
    UserRatingDTO updateRating(Long ratingID, CreateRatingDTO ratingDTO);


    /**
     * deletes an existing rating
     *
     * @param ratingID - of the rating to delete
     * @return true if the rating was deleted
     * @throws NotFoundException                 if there is no rating with the given id
     * @throws UnauthorizedModificationException if the current user was not the author of the rating
     */
    boolean deleteRating(Long ratingID);
}
