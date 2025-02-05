package at.ac.ase.inso.group02.messaging;

import at.ac.ase.inso.group02.entities.User;
import at.ac.ase.inso.group02.entities.exchange.ExchangeChat;
import at.ac.ase.inso.group02.entities.messaging.ChatMessage;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.validation.constraints.Min;

import java.util.List;
import java.util.UUID;

/**
 * Repository for Chat-Messages
 */
public interface ChatMessageRepository extends PanacheRepositoryBase<ChatMessage, UUID> {
    /**
     * retrieves all messages for a given exchange (=Chat-Room), ordered by most recent first
     * @param exchangeChat ExchangeChat for which to find messages for
     * @param beforeMessage when not null, will only retrieve messages created before the given message
     * @param count the number of messages to retrieve
     * @return list of messages, ordered by most recent first
     */
    List<ChatMessage> getMessagesForExchange(ExchangeChat exchangeChat, ChatMessage beforeMessage, @Min(0) Long count);

    /**
     * retrieves the (single) most recent message for a given exchange (=Chat-Room)
     * @param exchangeChat ExchangeChat for which to find the most recent message for
     * @return the most recent message (should never be null since an exchange must be created with an initial message)
     */
    ChatMessage getMostRecentMessageForExchange(ExchangeChat exchangeChat);

    /**
     * retrieves a query object for all unread messages of the user, ordered by most recent first
     * @param user the user to find messages for
     * @return a query object for the unread messages
     */
    PanacheQuery<ChatMessage> getUnreadMessagesForUser(User user);

    /**
     * retrieves the number of unread messages for the current user for the given ExchangeChat
     *  (should only be used by other services)
     * @param exchangeChat the exchange-chat to get the other messages for
     * @param user the user to get unread messages for
     * @return paginated list of unread messages, ordered by most recent first
     */
    Long getUnreadMessagesCount(ExchangeChat exchangeChat, User user);
}
