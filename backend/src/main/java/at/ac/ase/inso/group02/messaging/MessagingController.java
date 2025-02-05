package at.ac.ase.inso.group02.messaging;

import at.ac.ase.inso.group02.messaging.dto.*;
import at.ac.ase.inso.group02.util.pagination.PaginatedQueryDTO;
import at.ac.ase.inso.group02.util.pagination.PaginationParamsDTO;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.ResponseStatus;
import org.jboss.resteasy.reactive.RestPath;

import java.util.List;

/**
 * REST endpoint for handling chat-messages (alongside the WebSocket)
 */
@Path("/chat")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface MessagingController {
    /**
     * creates and retrieves a one-time-use ticket to initiate a WebSocket connection with
     * @return a one-time-use ticket to authenticate a new WebSocket connection with
     */
    @POST
    @ResponseStatus(200)
    @RolesAllowed({"USER", "ADMIN"})
    @Path("/ticket")
    WSTicketDTO getTicket();

    /**
     * accepts a new message for a given chat-exchange
     * @param exchangeID ID of the ExchangeChat (=Chat-Room)
     * @param newChatMessageDTO data of the new Message
     * @return the newly received and stored message, will also be propagated via the WebSocket
     */
    @POST
    @ResponseStatus(201)
    @RolesAllowed({"USER", "ADMIN"})
    @Path("/{exchangeID}")
    ChatMessageDTO newMessage(@RestPath String exchangeID, @Valid NewChatMessageDTO newChatMessageDTO);

    /**
     * retrieves all messages for a given Exchange-Chat and implicitly marks the retrieved messages as read by the requesting user
     * @param exchangeID ID of the ExchangeChat (=Chat-Room) to retrieve messages for
     * @param chatQueryParamDTO parameters, which and how many messages to retrieve
     * @return list of messages, ordered by most recent first
     */
    @GET
    @ResponseStatus(200)
    @RolesAllowed({"USER", "ADMIN"})
    @Path("/{exchangeID}")
    List<ChatMessageDTO> getMessagesForExchange(@RestPath String exchangeID, @BeanParam ChatQueryParamDTO chatQueryParamDTO);

    /**
     * retrieves a paginated list of unread messages for the current user, ordered by most recent first
     * @param paginationParamsDTO pagination parameters
     * @return paginated list of unread messages, ordered by most recent first
     */
    @GET
    @ResponseStatus(200)
    @RolesAllowed({"USER", "ADMIN"})
    @Path("/")
    PaginatedQueryDTO<ChatMessageDTO> getUnreadMessages(PaginationParamsDTO paginationParamsDTO);

    /**
     * retrieves the number of unread messages that the user was not yet notified for
     * Also marks all these messages as notified (will be excluded in subsequent requests)
     * @return number of unread, un-notified messages
     */
    @GET
    @ResponseStatus(200)
    @RolesAllowed({"USER", "ADMIN"})
    @Path("/notifications")
    ChatNotificationDTO getNotifications();
}