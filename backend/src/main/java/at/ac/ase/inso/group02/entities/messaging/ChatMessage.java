package at.ac.ase.inso.group02.entities.messaging;

import at.ac.ase.inso.group02.entities.User;
import at.ac.ase.inso.group02.entities.exchange.ExchangeChat;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * represents one message sent by one user
 */
@Slf4j
@Entity
@Table(name = "chat_message")
@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exchange_chat_id")
    @JsonBackReference(value = "exchange-chat-message")
    private ExchangeChat exchangeChat;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "author_user_id")
    @JsonBackReference(value = "message-author")
    private User author;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    @NotBlank
    private String content;

    @Column
    @Builder.Default
    private boolean exchangeChanged = false;

    @OneToMany(mappedBy = "message", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonIgnore
    private Set<MessageUnreadState> unseenBy = new HashSet<>();
}
