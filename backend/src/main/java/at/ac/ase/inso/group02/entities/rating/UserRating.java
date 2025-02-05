package at.ac.ase.inso.group02.entities.rating;

import at.ac.ase.inso.group02.entities.Skill;
import at.ac.ase.inso.group02.entities.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * represents ratings by users for users
 */
@Entity
//@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "type", "exchange_id" }) })
@Inheritance(strategy = InheritanceType.JOINED)
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public abstract class UserRating {
    @Id
    @GeneratedValue
    @Setter(AccessLevel.PROTECTED)
    private Long id;

    @Min(0)
    @Max(10)
    @Column
    // the given rating in half-stars (i.e. ratingHalfStars=10 ... 5-star rating)
    private short ratingHalfStars;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private String title;

    @Column
    private String description;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof User other))
            return false;

        return id != null &&
                id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @JsonProperty("byUser")
    public abstract User getByUser();

    @JsonProperty("forUser")
    public abstract User getForUser();

    @JsonProperty("forSkill")
    public abstract Skill getForSkill();
}
