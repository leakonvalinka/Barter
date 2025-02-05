package at.ac.ase.inso.group02.messaging;

import at.ac.ase.inso.group02.bartering.exception.NotPartOfExchangeException;
import at.ac.ase.inso.group02.entities.User;
import at.ac.ase.inso.group02.entities.exchange.ExchangeChat;
import at.ac.ase.inso.group02.exceptions.UnauthenticatedException;
import at.ac.ase.inso.group02.messaging.dto.*;
import at.ac.ase.inso.group02.messaging.exceptions.ChatMessagePublishException;
import at.ac.ase.inso.group02.messaging.exceptions.IllegalWSTicketException;
import at.ac.ase.inso.group02.messaging.exceptions.WorkerStartException;
import at.ac.ase.inso.group02.messaging.exceptions.WorkerStopException;
import at.ac.ase.inso.group02.util.pagination.PaginatedQueryDTO;
import at.ac.ase.inso.group02.util.pagination.PaginationParamsDTO;
import io.quarkus.websockets.next.WebSocketConnection;
import jakarta.ws.rs.NotFoundException;

import java.util.List;

/**
 * Service for handling Chat-Messages
 */
public interface MessagingService {
    /**
     * creates a new Chat-Worker that propagates new messages to the provided WebSocket connection
     * @param connection the Websocket to propagate messages to
     * @param ticketDTO the one-time-use ticket to authenticate the connection with
     * @throws WorkerStartException when an error occurred starting the worker (=RabbitMQ failure)
     * @throws IllegalWSTicketException when the provided ticket is invalid or has expired
     */
    void createChatWorker(WebSocketConnection connection, WSTicketDTO ticketDTO);

    /**
     * creates a new WebSocket-Ticket that can be used to authenticate a new WebSocket connection
     * @return the new WebSocket-Ticket
     * @throws UnauthenticatedException if the user is not authenticated and thus cannot create a ticket
     */
    WSTicketDTO getWSTicket();

    /**
     * Performs the necessary operations when a WebSocket connection is closed
     * (stops the worker that was previously created with createChatWorker())
     * @param connection WebSocket that is being closed
     * @throws WorkerStopException when the worker could not be stopped
     */
    void closeChatConnection(WebSocketConnection connection);

    /**
     * explicitly marks a single chat message as read
     * @param connection WebSocket connection that received the message
     * @param readMessageDTO the received update that a message has been read
     * @throws NotFoundException when a non-existent message was to be marked "read"
     */
    void markMessageRead(WebSocketConnection connection, ReadMessageDTO readMessageDTO);

    /**
     * creates and saves a new Chat-Message with the given data, and propagates it to all workers to send it via WebSocket
     * @param exchangeID ExchangeChat (=Chat-Room) for which the message is intended
     * @param newChatMessageDTO chat message data
     * @return the newly created message (will also be propagated via WebSocket)
     * @throws NotPartOfExchangeException when the authenticated user is not member of the Chat-Room
     * @throws ChatMessagePublishException when an error occurred publishing the message (via RabbitMQ)
     */
    ChatMessageDTO newMessage(String exchangeID, NewChatMessageDTO newChatMessageDTO);

    /**
     * like newMessage(), but indicates that this message is included in an exchange-update
     * (should only be called by other services, thus the presence of an entity)
     * @param exchangeChat the exchangeChat that was updated
     * @param chatMessage the message provided with the update
     * @return the newly created message
     * @throws NotPartOfExchangeException when the authenticated user is not member of the Chat-Room
     * @throws ChatMessagePublishException when an error occurred publishing the message (via RabbitMQ)
     */
    ChatMessageDTO newMessageForUpdatedExchange(ExchangeChat exchangeChat, NewChatMessageDTO chatMessage);

    /**
     * retrieves messages for a given exchange and implicitly marks the retrieved messages as read
     * @param exchangeID ID of the ExchangeChat (=Chat-Room) to retrieve messages for
     * @param chatQueryParamDTO parameters, which and how many messages to retrieve
     * @return list of messages, ordered by most recent first
     * @throws NotPartOfExchangeException when the authenticated user is not member of the Chat-Room
     * @throws NotFoundException when a non-existent ExchangeChat was queried
     */
    List<ChatMessageDTO> getMessagesForExchange(String exchangeID, ChatQueryParamDTO chatQueryParamDTO);

    /**
     * retrieves the (single) most recent message for a given exchange (=Chat-Room)
     * (should only be used by other services)
     * @param exchangeChat ExchangeChat for which to find the most recent message for
     * @return the most recent message (should never be null since an exchange must be created with an initial message)
     */
    ChatMessageDTO getMostRecentMessageForExchange(ExchangeChat exchangeChat);

    /**
     * retrieves the number of unread messages for the current user for the given ExchangeChat
     *  (should only be used by other services)
     * @param exchangeChat the exchange-chat to get the other messages for
     * @param user the user to get unread messages for
     * @return paginated list of unread messages, ordered by most recent first
     */
    Long getUnreadMessagesCount(ExchangeChat exchangeChat, User user);

    /**
     * retrieves a paginated list of unread messages for the current user, ordered by most recent first
     * @param paginationParamsDTO pagination parameters
     * @return paginated list of unread messages, ordered by most recent first
     */
    PaginatedQueryDTO<ChatMessageDTO> getUnreadMessages(PaginationParamsDTO paginationParamsDTO);

    /**
     * retrieves the number of unread messages that the user was not yet notified for
     * Also marks all these messages as notified (will be excluded in subsequent requests)
     * @return number of unread, un-notified messages
     */
    ChatNotificationDTO getNotifications();
}
