package at.ac.ase.inso.group02.entities.exchange;

import at.ac.ase.inso.group02.entities.Skill;
import at.ac.ase.inso.group02.entities.User;
import at.ac.ase.inso.group02.entities.rating.InitiatorRating;
import at.ac.ase.inso.group02.entities.rating.ResponderRating;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Represents a bartering-exchange for a single skill.
 * <p>
 * An exchange has an initiator (mapped by field initiator),
 * who is the user that saw a skill posting and started a conversation with the author of that posting
 * <p>
 * and a responder (exchangedSkill.byUser) who is the user that created the skill posting (demand or offer)
 * and responds to the initiator
 */
@Entity
@Table(
        name = "exchange_item",
        uniqueConstraints = @UniqueConstraint(
                name = "unique_exchange_combination",
                columnNames = {"skill_id", "initiator_user_id", "exchange_chat_id"}
        )
)
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder(toBuilder = true)
@Getter
@Setter
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class ExchangeItem {
    @Id
    @GeneratedValue
    @Setter(AccessLevel.PROTECTED)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "skill_id")
    @JsonBackReference(value = "exchange-skill")
    private Skill exchangedSkill;

    // exchangedSkillCounterpart should essentially represent the counterpart to exchangedSkill if it exists
    // i.e. exchangedSkillCounterpart is the OFFER corresponding to the exchangedSkill DEMAND or vice versa
    // these matches are to be found by the Recommendation/Matching-Service
    // This counterpart is optional!!
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "skill_counterpart_id")
    @JsonBackReference(value = "exchange-skill-counterpart")
    private Skill exchangedSkillCounterpart;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "initiator_user_id")
    @JsonBackReference(value = "exchange-initiator")
    // this user initiates the exchange, another (exchangedSkill.byUser) either offers or demands the exchanged service
    private User initiator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exchange_chat_id")
    @JsonBackReference(value = "exchange-chat")
    private ExchangeChat exchangeChat;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime firstExchangeAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime lastExchangeAt;

    @Min(0)
    @Column
    private int numberOfExchanges;

    @Builder.Default
    private boolean ratable = false;

    @Builder.Default
    private boolean initiatorMarkedComplete = false;

    @OneToOne(mappedBy = "initiatorExchange", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference(value = "exchange-initiator-rating")
    private InitiatorRating initiatorRating;

    @Builder.Default
    private boolean responderMarkedComplete = false;

    @OneToOne(mappedBy = "responderExchange", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference(value = "exchange-responder-rating")
    private ResponderRating responderRating;


    /*
    from https://vladmihalcea.com/the-best-way-to-implement-equals-hashcode-and-tostring-with-jpa-and-hibernate/
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof ExchangeItem other))
            return false;

        return id != null &&
                id.equals(other.getId());
    }

    /*
    from https://vladmihalcea.com/the-best-way-to-implement-equals-hashcode-and-tostring-with-jpa-and-hibernate/
     */
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
