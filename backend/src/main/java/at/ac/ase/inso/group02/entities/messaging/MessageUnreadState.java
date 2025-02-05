package at.ac.ase.inso.group02.entities.messaging;

import at.ac.ase.inso.group02.entities.User;
import jakarta.persistence.*;
import lombok.*;

/**
 * represents the receival state of a message
 */
@Entity
@Table(name = "message_receival")
@Getter
@Setter(AccessLevel.PUBLIC)
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class MessageUnreadState {
    @EmbeddedId
    private MessageReceivalID id;

    @ManyToOne(optional = false)
    @MapsId("userID")
    private User user;

    @ManyToOne(optional = false)
    @MapsId("messageID")
    private ChatMessage message;

    @Builder.Default
    @Column(nullable = false)
    private MessageReadState readState = MessageReadState.UNSEEN;
}
