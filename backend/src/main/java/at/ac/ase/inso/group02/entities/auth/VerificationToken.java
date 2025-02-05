package at.ac.ase.inso.group02.entities.auth;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.time.Instant;

@Embeddable
@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class VerificationToken {
    private String code;
    private Instant expiration;
}
