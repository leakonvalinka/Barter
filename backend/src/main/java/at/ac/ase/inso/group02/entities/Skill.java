package at.ac.ase.inso.group02.entities;

import at.ac.ase.inso.group02.entities.admin.SkillReport;
import at.ac.ase.inso.group02.entities.exchange.ExchangeItem;
import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id.DEDUCTION;

@Entity
@Table(name = "skill")
// use single table because overlap of demand and offer is small
// and IDs among offers and demands are unique
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.STRING)
@JsonTypeInfo(use = DEDUCTION)
@JsonSubTypes({
        @JsonSubTypes.Type(value = SkillDemand.class, name = "SkillDemand"),
        @JsonSubTypes.Type(value = SkillOffer.class, name = "SkillOffer")
})
@JsonIgnoreProperties(ignoreUnknown = true)
@SuperBuilder(toBuilder = true)
@Getter
@Setter
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public abstract class Skill {
    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private SkillCategory category;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    private User byUser;

    @OneToMany(mappedBy = "exchangedSkill", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    @JsonManagedReference(value = "exchange-skill")
    private Set<ExchangeItem> exchangeItems;

    @OneToMany(mappedBy = "exchangedSkillCounterpart", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    @JsonManagedReference(value = "exchange-skill-counterpart")
    private Set<ExchangeItem> exchangesCounterpart;

    /**
     * Reports that were filed against this skill.
     * This creates a bidirectional relationship with SkillReport.
     * When this skill is deleted, all associated reports will also be deleted.
     * @JsonIgnore is used to prevent infinite recursion during JSON serialization.
     */
    @OneToMany(mappedBy = "reportedSkill", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    @JsonManagedReference(value = "reported-skill")
    @Builder.Default  // Fix for builder warning since we're using SuperBuilder
    private Set<SkillReport> reports = new HashSet<>();

    // these rating stats only include Initiator-Ratings, i.e. the ratings by the users that react to this skill posting
    @Min(0)
    @Max(10)
    @Builder.Default
    private Double averageRatingHalfStars = null;

    @Min(0)
    @Builder.Default
    private Long numberOfRatings = 0L;
}
