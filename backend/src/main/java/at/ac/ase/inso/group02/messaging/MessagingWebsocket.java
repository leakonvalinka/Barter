package at.ac.ase.inso.group02.messaging;

import at.ac.ase.inso.group02.messaging.dto.ReadMessageDTO;
import io.quarkus.websockets.next.OnClose;
import io.quarkus.websockets.next.OnOpen;
import io.quarkus.websockets.next.OnTextMessage;
import io.quarkus.websockets.next.WebSocketConnection;

/**
 * Web Socket for chat messaging
 *
 * (for some reason, the @WebSocket annotation here causes errors...guess it is an experimental feature after all)
 */
public interface MessagingWebsocket {

    /**
     * authenticates the WebSocket with the given ticket
     * and prepares the necessary worker to propagate new messages to this WebSocket
     * @param webSocketConnection the new WebSocket connection
     */
    @OnOpen(broadcast = false)
    void onOpen(WebSocketConnection webSocketConnection);

    /**
     * performs the necessary actions before closing the WebSocket connection
     * @param webSocketConnection the WebSocket that is about to be closed
     */
    @OnClose
    void onClose(WebSocketConnection webSocketConnection);

    /**
     * marks a single chat-message as read by the sender
     * @param readMessageDTO data about the message that has been read
     * @param webSocketConnection WebSocket that received the message
     */
    @OnTextMessage(broadcast = false)
    void onMessage(ReadMessageDTO readMessageDTO, WebSocketConnection webSocketConnection);
}
