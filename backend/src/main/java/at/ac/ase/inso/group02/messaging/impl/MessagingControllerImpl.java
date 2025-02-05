package at.ac.ase.inso.group02.messaging.impl;

import at.ac.ase.inso.group02.messaging.MessagingController;
import at.ac.ase.inso.group02.messaging.MessagingService;
import at.ac.ase.inso.group02.messaging.dto.*;
import at.ac.ase.inso.group02.util.pagination.PaginatedQueryDTO;
import at.ac.ase.inso.group02.util.pagination.PaginationParamsDTO;
import io.quarkus.logging.Log;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class MessagingControllerImpl implements MessagingController {

    private MessagingService messagingService;

    @Override
    public WSTicketDTO getTicket() {
        Log.info("Getting WS ticket");
        return messagingService.getWSTicket();
    }

    @Override
    public ChatMessageDTO newMessage(String exchangeID, NewChatMessageDTO newChatMessageDTO) {
        Log.infov("New Message for exchange {0}: {1}", exchangeID, newChatMessageDTO);
        return messagingService.newMessage(exchangeID, newChatMessageDTO);
    }

    @Override
    public List<ChatMessageDTO> getMessagesForExchange(String exchangeID, ChatQueryParamDTO chatQueryParamDTO) {
        Log.infov("Fetching messages for exchange {0} with query params: {1}", exchangeID, chatQueryParamDTO);
        return messagingService.getMessagesForExchange(exchangeID, chatQueryParamDTO);
    }

    @Override
    public PaginatedQueryDTO<ChatMessageDTO> getUnreadMessages(PaginationParamsDTO paginationParamsDTO) {
        return messagingService.getUnreadMessages(paginationParamsDTO);
    }

    @Override
    public ChatNotificationDTO getNotifications() {
        return messagingService.getNotifications();
    }
}
