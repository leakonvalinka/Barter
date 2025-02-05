package at.ac.ase.inso.group02.rating;

import at.ac.ase.inso.group02.entities.Skill;
import at.ac.ase.inso.group02.entities.User;
import at.ac.ase.inso.group02.entities.rating.UserRating;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

/**
 * generic repository for any rating (UserRating, InitiatorRating or ResponderRating)
 *
 * @param <R> UserRating, InitiatorRating or ResponderRating
 */
public interface GenericRatingRepository<R extends UserRating> extends PanacheRepository<R> {

    /**
     * finds ratings of the respective type that are designated for a given user
     *
     * @param user a user to find ratings for (i.e. the recipient, not the author)
     * @return ratings-query for the user
     */
    PanacheQuery<? extends R> findRatingsForUser(User user);

    /**
     * finds ratings of the respective type that are given for an exchanged service
     * @param skill the skill/service to fetch ratings for
     * @return ratings-query for the skill
     */
    PanacheQuery<? extends R> findRatingsForSkill(Skill skill);
}
