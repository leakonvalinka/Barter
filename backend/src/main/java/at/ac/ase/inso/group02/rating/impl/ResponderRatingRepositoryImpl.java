package at.ac.ase.inso.group02.rating.impl;

import at.ac.ase.inso.group02.entities.Skill;
import at.ac.ase.inso.group02.entities.User;
import at.ac.ase.inso.group02.entities.exchange.ExchangeItem;
import at.ac.ase.inso.group02.entities.rating.ResponderRating;
import at.ac.ase.inso.group02.rating.ResponderRatingRepository;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ResponderRatingRepositoryImpl implements ResponderRatingRepository {
    @Override
    public PanacheQuery<ResponderRating> findRatingsForUser(User user) {
        return find("responderExchange.initiator = :user",
                Parameters.with("user", user));
    }

    @Override
    public PanacheQuery<ResponderRating> findRatingsForSkill(Skill skill) {
        return find("responderExchange.exchangedSkill = :skill",
                Parameters.with("skill", skill));
    }

    @Override
    public ResponderRating findByExchange(ExchangeItem exchangeItem) {
        return find("responderExchange = :exchange",
                Parameters.with("exchange", exchangeItem))
                .firstResult();
    }
}
