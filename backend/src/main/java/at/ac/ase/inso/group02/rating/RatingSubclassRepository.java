package at.ac.ase.inso.group02.rating;

import at.ac.ase.inso.group02.entities.exchange.ExchangeItem;
import at.ac.ase.inso.group02.entities.rating.UserRating;

/**
 * generic repository for rating-subclasses (InitiatorRating or ResponderRating)
 *
 * @param <R> InitiatorRating or ResponderRating
 */
public interface RatingSubclassRepository<R extends UserRating> extends GenericRatingRepository<R> {

    /**
     * finds the rating of the subtype for the given exchange (of which only one can exist per exchange)
     *
     * @param exchangeItem the exchange to find the rating for
     * @return the rating of the subtype for the exchange, null if none exists
     */
    R findByExchange(ExchangeItem exchangeItem);
}
