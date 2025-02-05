package at.ac.ase.inso.group02.entities.auth;

import at.ac.ase.inso.group02.entities.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class RefreshToken {

    // instead of the token-string, store the unique UUID contained within the token
    @Id
    @Setter(AccessLevel.PROTECTED)
//    @org.hibernate.annotations.Type(type="org.hibernate.type.PostgresUUIDType")
    private UUID uuid;

    @ManyToOne(optional = false)
    private User user;

    @Column(nullable = false, columnDefinition = "TIMESTAMP(0)", updatable = false)
    private Instant expiration;
}
