package at.ac.ase.inso.group02.messaging.impl;

import at.ac.ase.inso.group02.messaging.MessagingService;
import at.ac.ase.inso.group02.messaging.MessagingWebsocket;
import at.ac.ase.inso.group02.messaging.dto.ReadMessageDTO;
import at.ac.ase.inso.group02.messaging.dto.WSTicketDTO;
import io.quarkus.logging.Log;
import io.quarkus.websockets.next.*;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@WebSocket(path = "/chat/ws/{ticket}")
public class MessagingWebsocketImpl implements MessagingWebsocket {
    private WebSocketConnection connection;
    private MessagingService messagingService;

    /*
    We need to use the WebSocketConnection method parameter to be able to use the connections in async callbacks
    (RabbitMQ basicConsume()), see: https://github.com/quarkusio/quarkus/issues/41320#issuecomment-2186376549
     */

    @OnOpen(broadcast = false)
    @Override
    public void onOpen(WebSocketConnection webSocketConnection) {
        Log.info("New chat opened!");
        messagingService.createChatWorker(
                webSocketConnection,
                WSTicketDTO.builder()
                        .ticketUUID(connection.pathParam("ticket"))
                        .build()
        );
    }

    @OnClose
    @Override
    public void onClose(WebSocketConnection webSocketConnection) {
        Log.info("Chat closed!");
        messagingService.closeChatConnection(webSocketConnection);
    }

    @OnTextMessage(broadcast = false)
    @Override
    public void onMessage(ReadMessageDTO readMessageDTO, WebSocketConnection webSocketConnection) {
        Log.info("New chat message has been read: " + readMessageDTO);
        messagingService.markMessageRead(webSocketConnection, readMessageDTO);
    }
}
