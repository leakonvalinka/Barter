package at.ac.ase.inso.group02.rating.impl;

import at.ac.ase.inso.group02.entities.Skill;
import at.ac.ase.inso.group02.entities.User;
import at.ac.ase.inso.group02.entities.rating.UserRating;
import at.ac.ase.inso.group02.rating.RatingRepository;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.AllArgsConstructor;

@ApplicationScoped
@AllArgsConstructor
public class RatingRepositoryImpl implements RatingRepository {
    @Override
    public PanacheQuery<? extends UserRating> findRatingsForUser(User user) {

        // custom query because PanacheQuery does not support .union() :/
        String query = "SELECT u FROM UserRating u " +
                "WHERE u IN (SELECT i FROM InitiatorRating i WHERE i.initiatorExchange.exchangedSkill.byUser = :user) " +
                "OR u IN (SELECT r FROM ResponderRating r WHERE r.responderExchange.initiator = :user)";

        return find(query, Parameters.with("user", user));
    }

    @Override
    public PanacheQuery<? extends UserRating> findRatingsForSkill(Skill skill) {
        String query = "SELECT u FROM UserRating u " +
                "WHERE u IN (SELECT i FROM InitiatorRating i WHERE i.initiatorExchange.exchangedSkill = :skill) " +
                "OR u IN (SELECT r FROM ResponderRating r WHERE r.responderExchange.exchangedSkill = :skill)";

        return find(query, Parameters.with("skill", skill));
    }
}
