package at.ac.ase.inso.group02.rating.impl;

import at.ac.ase.inso.group02.entities.Skill;
import at.ac.ase.inso.group02.entities.User;
import at.ac.ase.inso.group02.entities.exchange.ExchangeItem;
import at.ac.ase.inso.group02.entities.rating.InitiatorRating;
import at.ac.ase.inso.group02.rating.InitiatorRatingRepository;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class InitiatorRatingRepositoryImpl implements InitiatorRatingRepository {
    @Override
    public PanacheQuery<InitiatorRating> findRatingsForUser(User user) {
        return find("initiatorExchange.exchangedSkill.byUser = :user",
                Parameters.with("user", user));
    }

    @Override
    public PanacheQuery<InitiatorRating> findRatingsForSkill(Skill skill) {
        return find("initiatorExchange.exchangedSkill = :skill",
                Parameters.with("skill", skill));
    }

    @Override
    public InitiatorRating findByExchange(ExchangeItem exchangeItem) {
        return find("initiatorExchange = :exchange",
                Parameters.with("exchange", exchangeItem))
                .firstResult();
    }
}
