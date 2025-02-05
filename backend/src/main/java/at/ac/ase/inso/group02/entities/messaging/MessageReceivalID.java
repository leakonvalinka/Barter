package at.ac.ase.inso.group02.entities.messaging;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Builder(toBuilder = true)
@Getter
@Setter
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class MessageReceivalID implements Serializable {
    @Column(nullable = false, name = "user_id")
    private Long userID;

    @Column(nullable = false, name = "message_id", columnDefinition = "UUID")
    private UUID messageID;
}
