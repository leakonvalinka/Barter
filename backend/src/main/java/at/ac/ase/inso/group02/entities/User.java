package at.ac.ase.inso.group02.entities;

import at.ac.ase.inso.group02.authentication.validation.NotEmail;
import at.ac.ase.inso.group02.entities.admin.SkillReport;
import at.ac.ase.inso.group02.entities.admin.UserBan;
import at.ac.ase.inso.group02.entities.admin.UserReport;
import at.ac.ase.inso.group02.entities.auth.RefreshToken;
import at.ac.ase.inso.group02.entities.auth.VerificationToken;
import at.ac.ase.inso.group02.entities.exchange.ExchangeChat;
import at.ac.ase.inso.group02.entities.exchange.ExchangeItem;
import at.ac.ase.inso.group02.entities.messaging.MessageUnreadState;
import at.ac.ase.inso.group02.entities.messaging.WSTicket;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import io.quarkus.security.jpa.Password;
import io.quarkus.security.jpa.PasswordType;
import io.quarkus.security.jpa.Roles;
import io.quarkus.security.jpa.Username;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;


/**
 * The central user object, representing registered users
 */
@Entity
@Table(name = "application_user")
@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
//@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class, property="@UUID")
public class User {
    @Id
    @GeneratedValue
    @Setter(AccessLevel.PROTECTED)
    private Long id;

    @Column(nullable = false, unique = true)
    @Email
    private String email;

    // by default, passwords-hashes are computed with bcrypt under the Modular Crypt Format (MCF), check the UserService implementation
    // see: https://quarkus.io/guides/security-jpa#password-storage-and-hashing
    @Password(value = PasswordType.MCF)
    private String password;

    @Column(nullable = false, unique = true)
    @Username
    @NotEmail
    private String username;

    @ManyToMany(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    @Roles
    @JsonBackReference(value = "roles")
    public Set<UserRole> roles = new HashSet<>();

    @Column()
    private String displayName;

    @Embedded
    private UserLocation location;

    @Column(length = 1000)
    private String bio;

    @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    @JsonIdentityReference(alwaysAsId = true)
    private Image profilePicture;

    @Column(nullable = false)
    @Builder.Default
    private UserState state = UserState.NEEDS_EMAIL_CONFIRM;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JsonBackReference(value = "refreshTokens")
    private Set<RefreshToken> refreshTokens = new HashSet<>();

    @Embedded
    private VerificationToken verificationToken;

    @OneToMany(mappedBy = "byUser", cascade = CascadeType.REMOVE, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    private Set<Skill> skills = new HashSet<>();

    @OneToMany(mappedBy = "byUser", cascade = CascadeType.REMOVE, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    private Set<SkillOffer> skillOffers = new HashSet<>();

    @OneToMany(mappedBy = "byUser", cascade = CascadeType.REMOVE, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    private Set<SkillDemand> skillDemands = new HashSet<>();

    @OneToMany(mappedBy = "initiator", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    @JsonManagedReference(value = "exchange-chat-initiator")
    private Set<ExchangeChat> initiatedExchangeChats = new HashSet<>();

    @OneToMany(mappedBy = "initiator", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    @JsonManagedReference(value = "exchange-initiator")
    private Set<ExchangeItem> initiatedExchangeItems = new HashSet<>();

    @Min(0)
    @Max(10)
    @Builder.Default
    private Double averageRatingHalfStars = null;

    @Min(0)
    @Builder.Default
    private Long numberOfRatings = 0L;

    @OneToMany(mappedBy = "forUser", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonManagedReference(value = "ws-tickets")
    private Set<WSTicket> wsTickets = new HashSet<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @Builder.Default
    @JsonIgnore
    private Set<MessageUnreadState> unseenMessages = new HashSet<>();

    /**
     * Reports that were filed against this user.
     * This creates a bidirectional relationship with UserReport where this user is the reported user.
     * When this user is deleted, all associated reports will also be deleted (orphanRemoval).
     */
    @OneToMany(mappedBy = "reportedUser", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference(value = "reported-user")
    @Builder.Default  // Fix for builder warning
    private Set<UserReport> receivedReports = new HashSet<>();

    /**
     * Reports that this user has filed against other users.
     * This creates a bidirectional relationship with UserReport where this user is the reporting user.
     * When this user is deleted, all their submitted reports will also be deleted.
     */
    @OneToMany(mappedBy = "reportingUser", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference(value = "reporting-user")
    @Builder.Default  // Fix for builder warning
    private Set<UserReport> submittedReports = new HashSet<>();

    /**
     * Ban records associated with this user.
     * This creates a bidirectional relationship with UserBan.
     * When this user is deleted, all their ban records will also be deleted.
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference(value = "banned-user")
    @Builder.Default  // Fix for builder warning
    private Set<UserBan> bans = new HashSet<>();

    /**
     * Skill reports that this user has submitted.
     * This creates a bidirectional relationship with SkillReport where this user is the reporting user.
     * When this user is deleted, all their submitted skill reports will also be deleted.
     */
    @OneToMany(mappedBy = "reportingUser", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference(value = "reporting-user")
    @Builder.Default  // Fix for builder warning
    private Set<SkillReport> submittedSkillReports = new HashSet<>();

    @Builder.Default
    @Column(nullable = true)
    private Boolean firstLogin = true;

    /*
    from https://vladmihalcea.com/the-best-way-to-implement-equals-hashcode-and-tostring-with-jpa-and-hibernate/
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof User other))
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
