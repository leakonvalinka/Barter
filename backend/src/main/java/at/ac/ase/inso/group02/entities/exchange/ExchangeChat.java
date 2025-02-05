package at.ac.ase.inso.group02.entities.exchange;

import at.ac.ase.inso.group02.entities.User;
import at.ac.ase.inso.group02.entities.messaging.ChatMessage;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a collection of Exchanges for which a chat-room should be served
 */
@Entity
@Table(name = "exchange_chat")
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder(toBuilder = true)
@Getter
@Setter
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class ExchangeChat {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "initiator_user_id")
    @JsonBackReference(value = "exchange-chat-initiator")
    // this user initiates the exchange, another (exchangedSkill.byUser) either offers or demands the exchanged service
    private User initiator;

    @OneToMany(mappedBy = "exchangeChat", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference(value = "exchange-chat")
    private Set<ExchangeItem> exchangeItems;

    @ManyToMany(fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private Set<User> requiredResponders = new HashSet<>();

    @OneToMany(mappedBy = "exchangeChat", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<ChatMessage> chatMessages;
}
