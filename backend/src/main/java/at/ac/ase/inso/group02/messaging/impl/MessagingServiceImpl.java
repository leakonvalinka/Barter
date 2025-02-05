package at.ac.ase.inso.group02.messaging.impl;

import at.ac.ase.inso.group02.authentication.AuthenticationService;
import at.ac.ase.inso.group02.authentication.UserRepository;
import at.ac.ase.inso.group02.authentication.dto.UserInfoDTO;
import at.ac.ase.inso.group02.bartering.ExchangeChatRepository;
import at.ac.ase.inso.group02.bartering.ExchangeService;
import at.ac.ase.inso.group02.bartering.exception.NotPartOfExchangeException;
import at.ac.ase.inso.group02.bartering.inactivity.ExchangeExpirationListener;
import at.ac.ase.inso.group02.bartering.inactivity.ExchangeInactivityService;
import at.ac.ase.inso.group02.entities.User;
import at.ac.ase.inso.group02.entities.UserState;
import at.ac.ase.inso.group02.entities.exchange.ExchangeChat;
import at.ac.ase.inso.group02.entities.messaging.*;
import at.ac.ase.inso.group02.messaging.ChatMessageRepository;
import at.ac.ase.inso.group02.messaging.MessagingService;
import at.ac.ase.inso.group02.messaging.WSTicketRepository;
import at.ac.ase.inso.group02.messaging.dto.*;
import at.ac.ase.inso.group02.messaging.exceptions.ChatMessagePublishException;
import at.ac.ase.inso.group02.messaging.exceptions.IllegalWSTicketException;
import at.ac.ase.inso.group02.messaging.exceptions.WorkerStartException;
import at.ac.ase.inso.group02.messaging.exceptions.WorkerStopException;
import at.ac.ase.inso.group02.messaging.rabbitmq.RabbitMQInitializer;
import at.ac.ase.inso.group02.messaging.worker.ChatRabbitMQWorker;
import at.ac.ase.inso.group02.util.MapperUtil;
import at.ac.ase.inso.group02.util.pagination.PaginatedQueryDTO;
import at.ac.ase.inso.group02.util.pagination.PaginationParamsDTO;
import at.ac.ase.inso.group02.util.pagination.PaginationUtil;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import io.quarkiverse.rabbitmqclient.RabbitMQClient;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.logging.Log;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.websockets.next.UserData;
import io.quarkus.websockets.next.WebSocketConnection;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeoutException;

@Slf4j
@ApplicationScoped
public class MessagingServiceImpl implements MessagingService {

    private AuthenticationService authenticationService;
    private UserRepository userRepository;
    private ChatMessageRepository messageRepository;
    private WSTicketRepository wsTicketRepository;
    private ExchangeService exchangeService;
    private ExchangeChatRepository exchangeChatRepository;
    private RabbitMQClient rabbitMQClient;
    private Channel channel;
    private ExchangeInactivityService inactivityService;
    private ExchangeExpirationListener expirationListener;
    private RabbitMQInitializer rabbitMQInitializer;

    @ConfigProperty(name = "ws.ticket.expiry-seconds", defaultValue = "60")
    private Long ticketExpirySeconds;

    private Map<String, ChatRabbitMQWorker> workers;


    public MessagingServiceImpl
            (AuthenticationService authenticationService,
             UserRepository userRepository,
             ChatMessageRepository messageRepository,
             WSTicketRepository wsTicketRepository,
             ExchangeService exchangeService,
             ExchangeChatRepository exchangeChatRepository,
             RabbitMQClient rabbitMQClient,
             ExchangeInactivityService inactivityService,
             ExchangeExpirationListener expirationListener,
             RabbitMQInitializer rabbitMQInitializer) {
        this.authenticationService = authenticationService;
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
        this.wsTicketRepository = wsTicketRepository;
        this.exchangeService = exchangeService;
        this.exchangeChatRepository = exchangeChatRepository;
        this.rabbitMQClient = rabbitMQClient;
        this.inactivityService = inactivityService;
        this.expirationListener = expirationListener;
        this.rabbitMQInitializer = rabbitMQInitializer;

        this.workers = new HashMap<>();
    }

    // demo setup
    @PostConstruct
    private void setupRabbitMQ() {
        try {
            rabbitMQInitializer.initRabbitMQExchanges();

            Connection connection = rabbitMQClient.connect();
            // create a channel
            channel = connection.createChannel();

            expirationListener.setupExpirationListener();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @PreDestroy
    private void teardownRabbitMQ() {
        try {
            if (channel.getConnection().isOpen()) {
                channel.getConnection().close();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    @Transactional
    public void createChatWorker(WebSocketConnection connection, WSTicketDTO ticketDTO) {
        Log.infov("New Chat-WebSocket connection");

        User user = this.redeemTicket(ticketDTO);

        ChatRabbitMQWorker worker = new ChatRabbitMQWorker(rabbitMQClient, user.getId(), connection);
        workers.put(getConnectionID(connection), worker);
        try {
            worker.startWorker();
        } catch (IOException e) {
            throw new WorkerStartException("Worker start failed!", e);
        }

        connection.userData().put(UserData.TypedKey.forLong("userID"), user.getId());
    }

    private static String getConnectionID(WebSocketConnection connection) {
        return connection.id();
    }

    @Override
    @Transactional
    public WSTicketDTO getWSTicket() {
        User user = authenticationService.getCurrentUser();

        WSTicket ticket = WSTicket.builder()
                .forUser(user)
                .build();
        wsTicketRepository.persistAndFlush(ticket);

        WSTicketDTO ticketDTO = MapperUtil.map(ticket, WSTicketDTO.class);
        ticketDTO.setExpires(
                getExpiry(ticket).toString()
        );

        return ticketDTO;
    }

    @Override
    public void closeChatConnection(WebSocketConnection webSocketConnection) {
        ChatRabbitMQWorker chatRabbitMQWorker = this.workers.remove(getConnectionID(webSocketConnection));

        if (chatRabbitMQWorker != null) {
            try {
                chatRabbitMQWorker.stopWorker();
            } catch (IOException | TimeoutException e) {
                throw new WorkerStopException("Worker shutdown failed", e);
            }
        }
    }

    @Override
    @Transactional
    public void markMessageRead(WebSocketConnection webSocketConnection, ReadMessageDTO readMessageDTO) {
        Log.infov("New Read-Message: {0}", readMessageDTO);

        ChatMessage message = getMessageEntityByID(readMessageDTO.getChatMessageID());
        User user = getConnectedUser(webSocketConnection);

        markMessageRead(message, user);
    }

    private void markMessageRead(ChatMessage message, User user) {
        boolean removed = message.getUnseenBy().removeIf(
                unreadState -> user.equals(unreadState.getUser())
                        && !MessageReadState.SEEN.equals(unreadState.getReadState())
        )
                && user.getUnseenMessages().removeIf(
                unreadState -> message.equals(unreadState.getMessage())
                        && !MessageReadState.SEEN.equals(unreadState.getReadState()));

        // only if the message was previously unseen, publish a message update to the author
        if (removed) {
            try {
                Log.info("Telling author about new read-message updates");
                ChatMessageDTO chatMessageDTO = mapChatMessageToDTOForUser(message, message.getAuthor());
                channel.basicPublish("chat-exchange", message.getAuthor().getId().toString(), null, MapperUtil.convertToJson(chatMessageDTO).getBytes());
            } catch (IOException ioException) {
                throw new ChatMessagePublishException("Could not publish chat message", ioException);
            }
        }
    }

    @Override
    @Transactional
    public ChatMessageDTO newMessage(String exchangeID, NewChatMessageDTO newChatMessageDTO) {
        ExchangeChat exchangeChat = getExchangeChatEntityById(exchangeID);

        return processNewChatMessage(newChatMessageDTO, exchangeChat, false);
    }

    @Override
    @Transactional
    public ChatMessageDTO newMessageForUpdatedExchange(ExchangeChat exchangeChat, NewChatMessageDTO chatMessage) {
        return processNewChatMessage(chatMessage, exchangeChat, true);
    }


    private ChatMessageDTO processNewChatMessage(NewChatMessageDTO newChatMessageDTO, ExchangeChat exchangeChat, boolean exchangeChanged) {
        User author = authenticationService.getCurrentUser();

        if (!exchangeService.isUserPartOfExchange(author, exchangeChat.getExchangeItems())) {
            throw new NotPartOfExchangeException("You cannot post messages to an exchange you are not part of!");
        }
        ChatMessage newMessage = MapperUtil.map(newChatMessageDTO, ChatMessage.class);

        newMessage.setAuthor(author);
        newMessage.setExchangeChat(exchangeChat);
        newMessage.setExchangeChanged(exchangeChanged);

        // make sure messages are persisted before messages are sent

        QuarkusTransaction.requiringNew().run(() -> {
            ExchangeChat managedExchangeChat = exchangeChatRepository.findById(exchangeChat.getId());

            // this message confirms any exchange-updates on behalf of the author
            managedExchangeChat.getRequiredResponders().remove(author);

            exchangeChatRepository.persistAndFlush(managedExchangeChat);
            messageRepository.persistAndFlush(newMessage);


            exchangeService.getExchangeParticipants(managedExchangeChat.getExchangeItems()).forEach(participant ->
            {
                if (!author.equals(participant)) {
                    newMessage.getUnseenBy().add(
                            MessageUnreadState.builder()
                                    .id(
                                            MessageReceivalID.builder()
                                                    .messageID(newMessage.getId())
                                                    .userID(participant.getId())
                                                    .build()
                                    )
                                    .user(participant)
                                    .message(newMessage)
                                    .readState(MessageReadState.UNSEEN)
                                    .build()
                    );
                }
            });
        });

        for (User recipient : exchangeService.getExchangeParticipants(exchangeChat.getExchangeItems())) {
            ChatMessageDTO chatMessageDTO = mapChatMessageToDTOForUser(newMessage, recipient);

            try {
                channel.basicPublish("chat-exchange", recipient.getId().toString(), null, MapperUtil.convertToJson(chatMessageDTO).getBytes());

                inactivityService.declareChatQueue(exchangeChat.getId());
                inactivityService.onNewMessageForExchangeChat(exchangeChat.getId());
            } catch (IOException ioException) {
                throw new ChatMessagePublishException("Could not publish chat message", ioException);
            }
        }

        return mapChatMessageToDTOForUser(newMessage, author);
    }

    @Override
    public List<ChatMessageDTO> getMessagesForExchange(String exchangeID, ChatQueryParamDTO chatQueryParamDTO) {
        ExchangeChat exchangeChat = getExchangeChatEntityById(exchangeID);

        User user = authenticationService.getCurrentUser();

        if (!exchangeService.isUserPartOfExchange(user, exchangeChat.getExchangeItems())) {
            throw new NotPartOfExchangeException("You cannot view messages for an exchange you are not part of!");
        }

        ChatMessage beforeMessage = null;
        if (chatQueryParamDTO.getBeforeMessageUUID() != null) {
            beforeMessage = getMessageEntityByID(chatQueryParamDTO.getBeforeMessageUUID());
            if (!exchangeChat.equals(beforeMessage.getExchangeChat())) {
                throw new NotFoundException("Chat-Message with ID \"" + chatQueryParamDTO.getBeforeMessageUUID() + "\" not found");
            }
        }

        return messageRepository.getMessagesForExchange(exchangeChat, beforeMessage, chatQueryParamDTO.getCount())
                .stream()
                .peek(message -> this.markMessageRead(message, user))
                .map(message -> mapChatMessageToDTOForUser(message, user))
                .toList();
    }

    @Override
    public ChatMessageDTO getMostRecentMessageForExchange(ExchangeChat exchangeChat) {
        return mapChatMessageToDTOForUser(messageRepository.getMostRecentMessageForExchange(exchangeChat), authenticationService.getCurrentUser());
    }

    @Override
    public Long getUnreadMessagesCount(ExchangeChat exchangeChat, User user) {
        return messageRepository.getUnreadMessagesCount(exchangeChat, user);
    }

    @Override
    @Transactional
    public PaginatedQueryDTO<ChatMessageDTO> getUnreadMessages(PaginationParamsDTO paginationParamsDTO) {
        User currentUser = this.authenticationService.getCurrentUser();
        Log.infov("Fetching unread messages for user {0} with params {1}",currentUser.getUsername(), paginationParamsDTO);

        return PaginationUtil.getPaginatedQueryDTO(
                paginationParamsDTO,
                messageRepository.getUnreadMessagesForUser(currentUser),
                (message -> mapChatMessageToDTOForUser(message, currentUser))
        );
    }

    @Override
    @Transactional
    public ChatNotificationDTO getNotifications() {
        User user = this.authenticationService.getCurrentUser();
        Log.infov("Fetching notifications for user {0}", user.getUsername());

        return ChatNotificationDTO.builder()
                .numberOfMessages(
                        user.getUnseenMessages().stream()
                                .filter(messageUnreadState -> messageUnreadState.getReadState().equals(MessageReadState.UNSEEN))
                                .peek(messageUnreadState -> messageUnreadState.setReadState(MessageReadState.NOTIFIED))
                                .count())
                .build();
    }

    private static ChatMessageDTO mapChatMessageToDTOForUser(ChatMessage chatMessage, User user) {
        if (chatMessage == null) {
            return null;
        }

        ChatMessageDTO chatMessageDTO = MapperUtil.map(chatMessage, ChatMessageDTO.class);
        chatMessageDTO.setExchangeID(chatMessage.getExchangeChat().getId().toString());
        chatMessageDTO.setAuthor(MapperUtil.map(chatMessage.getAuthor(), UserInfoDTO.class));

        if (user.equals(chatMessage.getAuthor())) {
            chatMessageDTO.setReadState(chatMessage.getUnseenBy().isEmpty() ? MessageReadState.SEEN : MessageReadState.UNSEEN);
        }

        return chatMessageDTO;
    }

    private ExchangeChat getExchangeChatEntityById(String id) {
        return getEntityByUUID(id, exchangeChatRepository, "Chat-Exchange");
    }

    private ChatMessage getMessageEntityByID(String id) {
        return getEntityByUUID(id, messageRepository, "Chat-Message");
    }

    private <E> E getEntityByUUID(String id, PanacheRepositoryBase<E, UUID> repo, String entityName) {
        UUID uuid;
        try {
            uuid = UUID.fromString(id);
        } catch (IllegalArgumentException iae) {
            throw new NotFoundException(entityName + " with ID \"" + id + "\" not found", iae);
        }

        E entity = repo.findById(uuid);

        if (entity == null) {
            throw new NotFoundException(entityName + " with ID \"" + id + "\" not found");
        }

        return entity;
    }

    private User getConnectedUser(WebSocketConnection webSocketConnection) {
        User user = userRepository.findById(webSocketConnection.userData().get(UserData.TypedKey.forLong("userID")));
        if (user == null) {
            throw new NotFoundException("Error when trying to find user associated to the WebSocket! User was probably deleted!");
        }
        return user;
    }

    private User redeemTicket(WSTicketDTO ticketDTO) {
        Log.infov("Redeeming ticket {0}", ticketDTO);

        UUID ticketID;
        try {
            ticketID = UUID.fromString(ticketDTO.getTicketUUID());
        } catch (IllegalArgumentException iae) {
            throw new IllegalWSTicketException("Invalid WebSocket-Ticket: Not a valid UUID");
        }

        WSTicket ticket = wsTicketRepository.findById(ticketID);

        if (ticket == null) {
            throw new IllegalWSTicketException("Invalid WebSocket-Ticket: No ticket with ID " + ticketID + " found");
        }

        if (getExpiry(ticket).isBefore(LocalDateTime.now())) {
            wsTicketRepository.delete(ticket);
            wsTicketRepository.flush();
            throw new IllegalWSTicketException("Invalid WebSocket-Ticket: Expired");
        }

        User user = ticket.getForUser();
        if (!user.getState().equals(UserState.ACTIVE)) {
            wsTicketRepository.delete(ticket);
            wsTicketRepository.flush();
            throw new IllegalWSTicketException("Invalid WebSocket-Ticket: User not active");
        }

        wsTicketRepository.delete(ticket);
        wsTicketRepository.flush();
        return user;
    }

    private LocalDateTime getExpiry(WSTicket ticket) {
        return ticket.getCreatedAt().plusSeconds(ticketExpirySeconds);
    }
}
