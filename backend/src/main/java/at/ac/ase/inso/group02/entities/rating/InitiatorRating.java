package at.ac.ase.inso.group02.entities.rating;

import at.ac.ase.inso.group02.entities.Skill;
import at.ac.ase.inso.group02.entities.User;
import at.ac.ase.inso.group02.entities.exchange.ExchangeItem;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * represents a rating created *by* the user that has initiated an exchange by reacting to the skill-offer/demand-posting
 * *for* the user that created the skill-demand or offering that has been exchanged
 */
@Entity
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class InitiatorRating extends UserRating {

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "exchange_id", nullable = false, unique = true)
    @JsonBackReference(value = "exchange-initiator-rating")
    private ExchangeItem initiatorExchange;

    @Override
    public User getByUser() {
        return getInitiatorExchange().getInitiator();
    }

    @Override
    public User getForUser() {
        return getInitiatorExchange().getExchangedSkill().getByUser();
    }

    @Override
    public Skill getForSkill() {
        return getInitiatorExchange().getExchangedSkill();
    }

    @PreRemove
    private void preRemove() {
        getInitiatorExchange().setInitiatorRating(null);
    }
}
