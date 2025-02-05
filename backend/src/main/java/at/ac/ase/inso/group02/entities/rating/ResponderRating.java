package at.ac.ase.inso.group02.entities.rating;

import at.ac.ase.inso.group02.entities.Skill;
import at.ac.ase.inso.group02.entities.User;
import at.ac.ase.inso.group02.entities.exchange.ExchangeItem;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * represents a rating created *by* the user that created the skill-demand or -offering posting that has been exchanged
 * *for* the user that has initiated an exchange by reacting to the skill-offer/demand-posting
 */
@Entity
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class ResponderRating extends UserRating {

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "exchange_id", nullable = false, unique = true)
    @JsonBackReference(value = "exchange-responder-rating")
    private ExchangeItem responderExchange;

    @Override
    public User getByUser() {
        return getResponderExchange().getExchangedSkill().getByUser();
    }

    @Override
    public User getForUser() {
        return getResponderExchange().getInitiator();
    }

    @Override
    public Skill getForSkill() {
        Skill exchangedSkillCounterpart = getResponderExchange().getExchangedSkillCounterpart();
        return exchangedSkillCounterpart != null ? exchangedSkillCounterpart : getResponderExchange().getExchangedSkill();
    }

    @PreRemove
    private void preRemove() {
        getResponderExchange().setResponderRating(null);
    }
}
