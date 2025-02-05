package at.ac.ase.inso.group02.messaging;

import at.ac.ase.inso.group02.authentication.AuthenticationService;
import at.ac.ase.inso.group02.authentication.UserRepository;
import at.ac.ase.inso.group02.bartering.ExchangeChatRepository;
import at.ac.ase.inso.group02.bartering.ExchangeService;
import at.ac.ase.inso.group02.bartering.exception.NotPartOfExchangeException;
import at.ac.ase.inso.group02.entities.SkillDemand;
import at.ac.ase.inso.group02.entities.User;
import at.ac.ase.inso.group02.entities.UserState;
import at.ac.ase.inso.group02.entities.exchange.ExchangeChat;
import at.ac.ase.inso.group02.entities.exchange.ExchangeItem;
import at.ac.ase.inso.group02.entities.messaging.ChatMessage;
import at.ac.ase.inso.group02.entities.messaging.MessageReadState;
import at.ac.ase.inso.group02.entities.messaging.MessageUnreadState;
import at.ac.ase.inso.group02.entities.messaging.WSTicket;
import at.ac.ase.inso.group02.exceptions.UnauthenticatedException;
import at.ac.ase.inso.group02.messaging.dto.*;
import at.ac.ase.inso.group02.messaging.exceptions.ChatMessagePublishException;
import at.ac.ase.inso.group02.messaging.exceptions.IllegalWSTicketException;
import at.ac.ase.inso.group02.messaging.exceptions.WorkerStartException;
import at.ac.ase.inso.group02.messaging.exceptions.WorkerStopException;
import at.ac.ase.inso.group02.util.pagination.PaginatedQueryDTO;
import at.ac.ase.inso.group02.util.pagination.PaginationParamsDTO;
import com.rabbitmq.client.CancelCallback;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import io.quarkiverse.rabbitmqclient.RabbitMQClient;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.websockets.next.UserData;
import io.quarkus.websockets.next.WebSocketConnection;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@QuarkusTest
public class MessagingServiceTest {
    @Inject
    @InjectMocks
    MessagingService messagingService;

    @InjectMock
    private AuthenticationService authenticationServiceMock;

    @InjectMock
    private UserRepository userRepositoryMock;

    @InjectMock
    private ChatMessageRepository messageRepositoryMock;

    @InjectMock
    private WSTicketRepository wsTicketRepositoryMock;

    @InjectMock
    private ExchangeChatRepository exchangeChatRepositoryMock;

    @InjectMock
    private RabbitMQClient rabbitMQClientMock;

    @Mock
    private static Channel rabbitMQChannelMock;

    private static final String EXCHANGE_NAME = "chat-exchange";

    @Mock
    private static WebSocketConnection webSocketConnectionMock;

    @Mock
    private static Connection rabbitMQConnectionMock;

    @ConfigProperty(name = "ws.ticket.expiry-seconds", defaultValue = "60")
    private Long ticketExpirySeconds;

    static User autheticatedUser = User.builder()
            .id(-1L)
            .username("authenticatedUser")
            .state(UserState.ACTIVE)
            .build();

    static User otherUser = User.builder()
            .id(-2L)
            .username("otherUser")
            .state(UserState.ACTIVE)
            .build();

    static User otherUser2 = User.builder()
            .id(-3L)
            .username("otherOtherUser")
            .state(UserState.ACTIVE)
            .build();

    @BeforeAll
    static void setUp() {
        // need to Mock rabbitMQ connection and channel statically so they don't get replaced from test-to-test
        rabbitMQChannelMock = mock(Channel.class);
        webSocketConnectionMock = mock(WebSocketConnection.class);
        rabbitMQConnectionMock = mock(Connection.class);
    }

    @BeforeEach
    void setup() throws IOException {
        // reset behavior of static mocks possibly altered by each test
        reset(rabbitMQChannelMock, webSocketConnectionMock, rabbitMQConnectionMock);

        UserData userDataMock = mock(UserData.class);
        when(webSocketConnectionMock.userData()).thenReturn(userDataMock);

        // mock the UserData of the webSocketConnection
        doAnswer(invocation -> invocation.<Long>getArgument(1)).when(userDataMock).put(any(), anyLong());
        // always return the user's id when querying anything in the userData
        // we don't need it for anything else anyway
        when(userDataMock.get(any())).thenReturn(autheticatedUser.getId());

        when(authenticationServiceMock.getCurrentUser()).thenReturn(autheticatedUser);
        when(userRepositoryMock.findById(autheticatedUser.getId())).thenReturn(autheticatedUser);
        when(userRepositoryMock.findById(otherUser.getId())).thenReturn(otherUser);
        when(userRepositoryMock.findById(otherUser2.getId())).thenReturn(otherUser2);

        when(rabbitMQClientMock.connect()).thenReturn(rabbitMQConnectionMock);
        when(rabbitMQConnectionMock.isOpen()).thenReturn(true);
        when(rabbitMQConnectionMock.createChannel()).thenReturn(rabbitMQChannelMock);
        when(rabbitMQChannelMock.isOpen()).thenReturn(true);

        when(webSocketConnectionMock.id()).thenReturn(UUID.randomUUID().toString());

        // mock setting random UUID on message persist
        doAnswer(invocation -> {
            ChatMessage argument = invocation.getArgument(0);
            argument.setId(UUID.randomUUID());
            return null;
        }).when(messageRepositoryMock).persistAndFlush(any());
    }


    // BEGIN TESTS createChatWorker()
    @Test
    void testCreateChatWorker_shouldSucceedForValidTicket() throws IOException {
        // Arrange
        UUID ticketUUID = UUID.randomUUID();

        when(wsTicketRepositoryMock.findById(ticketUUID)).thenReturn(
                WSTicket.builder()
                        .forUser(autheticatedUser)
                        .createdAt(LocalDateTime.now())
                        .ticketUUID(ticketUUID)
                        .build()
        );

        // Act
        messagingService.createChatWorker(webSocketConnectionMock, WSTicketDTO.builder()
                .ticketUUID(ticketUUID.toString())
                .build());

        // Assert
        verifyRabbitMQWorkerStarted();
    }

    private void verifyRabbitMQWorkerStarted() throws IOException {
        String queueID = "worker-queue-" + webSocketConnectionMock.id();
        verify(rabbitMQChannelMock, times(1)).queueDeclare(queueID, false, true, true, null);
        verify(rabbitMQChannelMock, times(1)).queueBind(queueID, "chat-exchange", autheticatedUser.getId().toString());
        verify(rabbitMQChannelMock, times(1)).basicConsume(eq(queueID), eq(true), any(), (CancelCallback) any());
        verify(webSocketConnectionMock.userData(), times(1)).put(UserData.TypedKey.forLong("userID"), autheticatedUser.getId());
    }

    @Test
    void testCreateChatWorkerWithMalformedUUID_shouldFailWithInvalidTicket() {
        assertIllegalTicket("this-uuid-looks-funny");
    }

    @Test
    void testCreateChatWorkerWithNonExistentTicket_shouldFailWithInvalidTicket() {
        assertIllegalTicket(UUID.randomUUID().toString());
    }

    @Test
    void testCreateChatWorkerWithExpiredTicket_shouldFailWithInvalidTicket() {
        // Arrange
        UUID ticketUUID = UUID.randomUUID();
        when(wsTicketRepositoryMock.findById(ticketUUID)).thenReturn(
                WSTicket.builder()
                        .forUser(autheticatedUser)
                        .createdAt(LocalDateTime.now().minusSeconds(ticketExpirySeconds + 1))
                        .ticketUUID(ticketUUID)
                        .build()
        );

        // Act & Assert
        assertIllegalTicket(ticketUUID.toString());
    }

    @Test
    void testCreateChatWorkerWithInactiveUserTicket_shouldFailWithInvalidTicket() {
        // Arrange
        UUID ticketUUID = UUID.randomUUID();
        when(wsTicketRepositoryMock.findById(ticketUUID)).thenReturn(
                WSTicket.builder()
                        .forUser(User.builder()
                                .state(UserState.INACTIVE)
                                .build())
                        .createdAt(LocalDateTime.now())
                        .ticketUUID(ticketUUID)
                        .build()
        );

        // Act & Assert
        assertIllegalTicket(ticketUUID.toString());
    }

    private void assertIllegalTicket(String ticketUUID) {
        Assertions.assertThrows(IllegalWSTicketException.class, () ->
                messagingService.createChatWorker(webSocketConnectionMock,
                        WSTicketDTO.builder()
                                .ticketUUID(ticketUUID)
                                .build()
                )
        );
    }

    @Test
    void testCreateChatWorkerRabbitMQFailed_shouldFailWithWorkerStartException() throws IOException {
        // Arrange
        UUID ticketUUID = UUID.randomUUID();
        when(wsTicketRepositoryMock.findById(ticketUUID)).thenReturn(
                WSTicket.builder()
                        .forUser(autheticatedUser)
                        .createdAt(LocalDateTime.now())
                        .ticketUUID(ticketUUID)
                        .build()
        );

        doThrow(new IOException("Something went wrong in RabbitMQ"))
                .when(rabbitMQChannelMock).basicConsume(anyString(), eq(true), any(), (CancelCallback) any());

        // Act & Assert
        Assertions.assertThrows(WorkerStartException.class, () ->
                messagingService.createChatWorker(webSocketConnectionMock,
                        WSTicketDTO.builder()
                                .ticketUUID(ticketUUID.toString())
                                .build()
                )
        );
    }

    // BEGIN TESTS getWSTicket()
    @Test
    void testGetWSTicket_shouldSucceedForAuthenticatedUser() throws IOException {
        // Arrange
        // when queried for a ticket that was created, return that ticket on subsequent requests
        doAnswer(invocation -> {
            WSTicket ticket = invocation.getArgument(0);
            ticket.setCreatedAt(LocalDateTime.now());
            ticket.setTicketUUID(UUID.randomUUID());
            when(wsTicketRepositoryMock.findById(ticket.getTicketUUID())).thenReturn(ticket);
            return ticket;
        }).when(wsTicketRepositoryMock).persistAndFlush(any(WSTicket.class));

        WSTicketDTO wsTicket = messagingService.getWSTicket();
        Assertions.assertNotNull(wsTicket);

        // Act
        // that ticket should be usable to create a Chat-Worker
        messagingService.createChatWorker(webSocketConnectionMock, WSTicketDTO.builder()
                .ticketUUID(wsTicket.getTicketUUID())
                .build());

        // Assert
        verifyRabbitMQWorkerStarted();
    }

    @Test
    void testGetWSTicketUnauthenticated_shouldFailWithUnauthenticatedException() {
        // Arrange
        doThrow(new UnauthenticatedException("Unauthenticated")).when(authenticationServiceMock).getCurrentUser();
        // Act & Assert
        Assertions.assertThrows(UnauthenticatedException.class, () -> messagingService.getWSTicket());
    }

    // BEGIN TESTS closeChatConnection()
    @Test
    void testCloseChatConnection_shouldSucceedForActiveConnection() throws IOException, TimeoutException {
        // Arrange
        // simulate an active connection
        testCreateChatWorker_shouldSucceedForValidTicket();

        // Act
        messagingService.closeChatConnection(webSocketConnectionMock);

        // Assert
        verify(rabbitMQChannelMock, times(1)).close();
    }

    @Test
    void testCloseChatConnectionRabbitMQFailure_shouldFailWithWorkerStopException() throws IOException, TimeoutException {
        // Arrange
        // simulate an active connection
        testCreateChatWorker_shouldSucceedForValidTicket();

        doThrow(new TimeoutException("Timeout")).when(rabbitMQChannelMock).close();

        // Act & Assert
        Assertions.assertThrows(WorkerStopException.class, () -> messagingService.closeChatConnection(webSocketConnectionMock));
        verify(rabbitMQChannelMock, times(1)).close();
    }

    // BEGIN TESTS markMessageRead()
    @Test
    void testMarkMessageRead_shouldSucceedForValidMessage() throws IOException {
        // Arrange
        UUID messageUUID = UUID.randomUUID();
        MessageUnreadState userUnread = MessageUnreadState.builder()
                .user(autheticatedUser)
                .readState(MessageReadState.UNSEEN)
                .build();
        MessageUnreadState otherUserUnread = MessageUnreadState.builder()
                .user(otherUser)
                .readState(MessageReadState.UNSEEN)
                .build();

        ChatMessage message = ChatMessage.builder()
                .id(messageUUID)
                .unseenBy(new HashSet<>(Set.of(
                        userUnread,
                        otherUserUnread
                )))
                .author(otherUser2)
                .exchangeChat(ExchangeChat.builder()
                        .id(UUID.randomUUID())
                        .build())
                .content("My Message")
                .build();

        userUnread.setMessage(message);
        otherUserUnread.setMessage(message);
        autheticatedUser.getUnseenMessages().add(userUnread);
        otherUser.getUnseenMessages().add(otherUserUnread);

        when(messageRepositoryMock.findById(messageUUID)).thenReturn(
                message
        );

        // Act & Assert
        messagingService.markMessageRead(
                webSocketConnectionMock,
                ReadMessageDTO.builder()
                        .chatMessageID(messageUUID.toString())
                        .build()
        );

        // update to the author was published
        ArgumentCaptor<byte[]> captor = ArgumentCaptor.forClass(byte[].class);
        verify(rabbitMQChannelMock, times(1)).basicPublish(
                eq(EXCHANGE_NAME),
                eq(message.getAuthor().getId().toString()),
                any(),
                captor.capture()
        );
        List<byte[]> capturedMessages = captor.getAllValues();
        Assertions.assertEquals(1, capturedMessages.size());
        Assertions.assertEquals(
                "{\"id\":\"" + message.getId() + "\",\"exchangeID\":\"" + message.getExchangeChat().getId() + "\",\"content\":\"My Message\",\"exchangeChanged\":false,\"author\":{\"email\":null,\"username\":\"otherOtherUser\",\"displayName\":null,\"profilePicture\":null,\"createdAt\":null,\"averageRatingHalfStars\":null},\"timestamp\":null,\"readState\":\"UNSEEN\"}",
                new String(capturedMessages.getFirst())
        );

        // unseenBy now removed for the user, only the other user remains unread
        Assertions.assertEquals(1, message.getUnseenBy().size());
        Assertions.assertIterableEquals(Set.of(otherUserUnread), message.getUnseenBy());
    }

    @Test
    void testMarkMessageReadForNonexistentMessage_shouldFailWithNotFoundException() {
        // Arrange
        UUID messageUUID = UUID.randomUUID();
        // simulate non-existent message
        when(messageRepositoryMock.findById(messageUUID)).thenReturn(
                null
        );

        // Act & Assert
        Assertions.assertThrows(NotFoundException.class, () ->
                messagingService.markMessageRead(webSocketConnectionMock, ReadMessageDTO.builder()
                        .chatMessageID(messageUUID.toString())
                        .build()));
    }

    @Test
    void testMarkMessageReadForDeletedUser_shouldFailWithNotFoundException() {
        // Arrange
        UUID messageUUID = UUID.randomUUID();
        // simulate non-existent message
        when(userRepositoryMock.findById(any())).thenReturn(
                null
        );

        // Act & Assert
        Assertions.assertThrows(NotFoundException.class, () ->
                messagingService.markMessageRead(webSocketConnectionMock, ReadMessageDTO.builder()
                        .chatMessageID(messageUUID.toString())
                        .build()));
    }

    @Test
    void testMarkMessageReadWithMalformedUUID_shouldFailWithNotFoundException() {
        Assertions.assertThrows(NotFoundException.class, () ->
                messagingService.markMessageRead(webSocketConnectionMock, ReadMessageDTO.builder()
                        .chatMessageID("this-uuid-looks-funny")
                        .build()));
    }

    static Stream<ChatMessage> getMessagesNotUnread() {
        // Arrange
        return Stream.of(
                ChatMessage.builder()
                        .id(UUID.randomUUID())
                        .unseenBy(new HashSet<>(Set.of(
                                MessageUnreadState.builder()
                                        // message is only unseen by other user
                                        .user(otherUser)
                                        .readState(MessageReadState.UNSEEN)
                                        .build()
                        )))
                        .build(),
                ChatMessage.builder()
                        .id(UUID.randomUUID())
                        .unseenBy(new HashSet<>(Set.of(
                                MessageUnreadState.builder()
                                        // message is only unseen by other user
                                        .user(otherUser)
                                        .readState(MessageReadState.UNSEEN)
                                        .build()
                        )))
                        // user is author themselves
                        .author(autheticatedUser)
                        .build(),
                ChatMessage.builder()
                        .id(UUID.randomUUID())
                        .unseenBy(new HashSet<>(Set.of()))
                        // user is author themselves
                        .author(autheticatedUser)
                        .build()
        );
    }

    @ParameterizedTest
    @MethodSource("getMessagesNotUnread")
    void testMarkMessageReadNotUnread_shouldDoNothing(ChatMessage message) throws IOException {
        // Arrange
        Set<MessageUnreadState> unreadBefore = new HashSet<>(message.getUnseenBy());
        when(messageRepositoryMock.findById(message.getId())).thenReturn(
                message
        );

        // Act
        messagingService.markMessageRead(
                webSocketConnectionMock,
                ReadMessageDTO.builder()
                        .chatMessageID(message.getId().toString())
                        .build()
        );

        // Assert
        // no update should be published
        verify(rabbitMQChannelMock, times(0)).basicPublish(eq(EXCHANGE_NAME), any(), any(), any());
        // unread state should be unchanged
        Assertions.assertIterableEquals(unreadBefore, message.getUnseenBy());
    }

    @Test
    void testMarkMessageReadRabbitMQError_shouldFailWithChatMessagePublishException() throws IOException {
        // Arrange
        UUID messageUUID = UUID.randomUUID();

        MessageUnreadState userUnread = MessageUnreadState.builder()
                .user(autheticatedUser)
                .readState(MessageReadState.UNSEEN)
                .build();
        MessageUnreadState otherUserUnread = MessageUnreadState.builder()
                .user(otherUser)
                .readState(MessageReadState.UNSEEN)
                .build();
        ChatMessage message = ChatMessage.builder()
                .id(messageUUID)
                .unseenBy(new HashSet<>(Set.of(
                        userUnread,
                        otherUserUnread
                )))
                .author(otherUser2)
                .exchangeChat(ExchangeChat.builder()
                        .id(UUID.randomUUID())
                        .build())
                .content("My Message")
                .build();

        userUnread.setMessage(message);
        otherUserUnread.setMessage(message);
        autheticatedUser.getUnseenMessages().add(userUnread);
        otherUser.getUnseenMessages().add(otherUserUnread);

        when(messageRepositoryMock.findById(messageUUID)).thenReturn(
                message
        );

        doThrow(new IOException("RabbitMQ publish exception"))
                .when(rabbitMQChannelMock)
                .basicPublish(any(), any(), any(), any());

        // Act & Assert
        Assertions.assertThrows(ChatMessagePublishException.class, () -> messagingService.markMessageRead(
                webSocketConnectionMock,
                ReadMessageDTO.builder()
                        .chatMessageID(messageUUID.toString())
                        .build()
        ));
    }

    static Stream<ExchangeChat> getExchangeChatsForValidMessage() {
        // Arrange
        // In all these ExchangeChats, authenticatedUser and otherUser are participants, otherUser2 is not
        ExchangeChat exchangeChat = ExchangeChat.builder()
                .id(UUID.randomUUID())
                .initiator(autheticatedUser)
                .exchangeItems(new HashSet<>())
                .build();

        // single-skill exchange
        exchangeChat.getExchangeItems().add(
                ExchangeItem.builder()
                        .initiator(autheticatedUser)
                        .exchangedSkill(SkillDemand.builder()
                                .byUser(otherUser)
                                .id(-10L)
                                .build())
                        .exchangeChat(exchangeChat)
                        .build()
        );

        ExchangeChat exchangeChat2 = ExchangeChat.builder()
                .id(UUID.randomUUID())
                .initiator(otherUser)
                .exchangeItems(new HashSet<>())
                .build();

        // single-skill exchange
        exchangeChat2.getExchangeItems().add(
                ExchangeItem.builder()
                        .initiator(otherUser)
                        .exchangedSkill(SkillDemand.builder()
                                .byUser(autheticatedUser)
                                .id(-11L)
                                .build())
                        .exchangeChat(exchangeChat2)
                        .build()
        );

        ExchangeChat exchangeChat3 = ExchangeChat.builder()
                .id(UUID.randomUUID())
                .initiator(otherUser)
                .exchangeItems(new HashSet<>())
                .build();

        // multiple-skill exchange
        exchangeChat3.getExchangeItems().add(
                ExchangeItem.builder()
                        .initiator(otherUser)
                        .exchangedSkill(SkillDemand.builder()
                                .byUser(autheticatedUser)
                                .id(-11L)
                                .build())
                        .exchangeChat(exchangeChat3)
                        .build()
        );
        exchangeChat3.getExchangeItems().add(
                ExchangeItem.builder()
                        .initiator(autheticatedUser)
                        .exchangedSkill(SkillDemand.builder()
                                .byUser(otherUser)
                                .id(-10L)
                                .build())
                        .exchangeChat(exchangeChat3)
                        .build()
        );

        return Stream.of(
                exchangeChat,
                exchangeChat2,
                exchangeChat3
        );
    }

    // START TESTS newMessage()
    @ParameterizedTest
    @MethodSource("getExchangeChatsForValidMessage")
    void testNewMessageValid_shouldSucceed(ExchangeChat exchangeChat) throws IOException {
        // Arrange
        when(exchangeChatRepositoryMock.findById(exchangeChat.getId())).thenReturn(exchangeChat);
        NewChatMessageDTO newMessage = NewChatMessageDTO.builder().content("Hello there").build();

        // Act
        ChatMessageDTO resultMessage = messagingService.newMessage(exchangeChat.getId().toString(),
                newMessage);

        // Assert
        Assertions.assertNotNull(resultMessage);
        Assertions.assertEquals(newMessage.getContent(), resultMessage.getContent());
        Assertions.assertEquals(exchangeChat.getId().toString(), resultMessage.getExchangeID());
        Assertions.assertEquals(autheticatedUser.getUsername(), resultMessage.getAuthor().getUsername());
        Assertions.assertEquals(MessageReadState.UNSEEN, resultMessage.getReadState());

        // message is published to every participating user
        // authenticatedUser
        String publishedMessage = verifyMessagePublished(autheticatedUser, resultMessage);
        Assertions.assertTrue(publishedMessage.contains("\"readState\":\"UNSEEN\"")); // the author sees the read-state of the message

        // otherUser
        publishedMessage = verifyMessagePublished(otherUser, resultMessage);
        Assertions.assertTrue(publishedMessage.contains("\"readState\":null")); // other users don't see the read-state of the message

        // message is not published to non-participating user
        verify(rabbitMQChannelMock, times(0)).basicPublish(eq(EXCHANGE_NAME), eq(otherUser2.getId().toString()), any(), any());
    }

    private static String verifyMessagePublished(User toUser, ChatMessageDTO chatMessage) throws IOException {
        ArgumentCaptor<byte[]> captor = ArgumentCaptor.forClass(byte[].class);
        verify(rabbitMQChannelMock, times(1)).basicPublish(
                eq(EXCHANGE_NAME),
                eq(toUser.getId().toString()),
                any(),
                captor.capture()
        );

        List<byte[]> capturedMessages = captor.getAllValues();
        Assertions.assertEquals(1, capturedMessages.size());
        String publishedMessage = new String(capturedMessages.getFirst());
        Assertions.assertTrue(
                publishedMessage.contains(
                        "\"id\":\"" + chatMessage.getId() + "\""
                )
        );

        Assertions.assertTrue(
                publishedMessage.contains(
                        "\"exchangeID\":\"" + chatMessage.getExchangeID() + "\""
                )
        );

        Assertions.assertTrue(
                publishedMessage.contains(
                        "\"username\":\"" + chatMessage.getAuthor().getUsername() + "\""
                )
        );
        Assertions.assertTrue(
                publishedMessage.contains(
                        "\"content\":\"" + chatMessage.getContent() + "\""
                )
        );
        return publishedMessage;
    }


    static Stream<ExchangeChat> getExchangeChatsNotParticipating() {
        // In all these ExchangeChats, authenticatedUser is not a participant and should not be allowed to post messages
        ExchangeChat exchangeChat = ExchangeChat.builder()
                .id(UUID.randomUUID())
                .initiator(otherUser)
                .exchangeItems(new HashSet<>())
                .build();

        // single-skill exchange
        exchangeChat.getExchangeItems().add(
                ExchangeItem.builder()
                        .initiator(otherUser)
                        .exchangedSkill(SkillDemand.builder()
                                .byUser(otherUser2)
                                .id(-10L)
                                .build())
                        .exchangeChat(exchangeChat)
                        .build()
        );

        ExchangeChat exchangeChat2 = ExchangeChat.builder()
                .id(UUID.randomUUID())
                .initiator(otherUser)
                .exchangeItems(new HashSet<>())
                .build();

        // single-skill exchange
        exchangeChat2.getExchangeItems().add(
                ExchangeItem.builder()
                        .initiator(otherUser2)
                        .exchangedSkill(SkillDemand.builder()
                                .byUser(otherUser)
                                .id(-11L)
                                .build())
                        .exchangeChat(exchangeChat2)
                        .build()
        );

        ExchangeChat exchangeChat3 = ExchangeChat.builder()
                .id(UUID.randomUUID())
                .initiator(otherUser)
                .exchangeItems(new HashSet<>())
                .build();

        // multiple-skill exchange
        exchangeChat3.getExchangeItems().add(
                ExchangeItem.builder()
                        .initiator(otherUser)
                        .exchangedSkill(SkillDemand.builder()
                                .byUser(otherUser2)
                                .id(-11L)
                                .build())
                        .exchangeChat(exchangeChat3)
                        .build()
        );
        exchangeChat3.getExchangeItems().add(
                ExchangeItem.builder()
                        .initiator(otherUser2)
                        .exchangedSkill(SkillDemand.builder()
                                .byUser(otherUser)
                                .id(-10L)
                                .build())
                        .exchangeChat(exchangeChat3)
                        .build()
        );

        return Stream.of(
                exchangeChat,
                exchangeChat2,
                exchangeChat3
        );
    }

    @ParameterizedTest
    @MethodSource("getExchangeChatsNotParticipating")
    void testNewMessageNotParticipating_shouldFailWithNotPartOfExchangeException(ExchangeChat exchangeChat) throws IOException {
        // Arrange
        when(exchangeChatRepositoryMock.findById(exchangeChat.getId())).thenReturn(exchangeChat);
        NewChatMessageDTO newMessage = NewChatMessageDTO.builder().content("Hello there").build();

        // Act & Assert
        Assertions.assertThrows(NotPartOfExchangeException.class, () -> messagingService.newMessage(exchangeChat.getId().toString(),
                newMessage));
        // message is not published to anyone
        verify(rabbitMQChannelMock, times(0)).basicPublish(eq(EXCHANGE_NAME), any(), any(), any());
    }


    @ParameterizedTest
    @MethodSource("getExchangeChatsForValidMessage")
    void testNewMessageRabbitMQError_shouldFailWithChatMessagePublishException(ExchangeChat exchangeChat) throws IOException {
        // Arrange
        when(exchangeChatRepositoryMock.findById(exchangeChat.getId())).thenReturn(exchangeChat);
        doThrow(new IOException("RabbitMQ error")).when(rabbitMQChannelMock).basicPublish(any(), any(), any(), any());
        NewChatMessageDTO newMessage = NewChatMessageDTO.builder().content("Hello there").build();

        // Act & Assert
        Assertions.assertThrows(ChatMessagePublishException.class, () -> messagingService.newMessage(exchangeChat.getId().toString(),
                newMessage));

        // Note that the message will still be persisted, only the message-publish will not work
        // recipients can still query for the message manually
        // (as a kind of fallback if RabbitMQ fails, the chat service frontend could revert to polling)
    }

    // START TESTS newMessageForUpdatedExchange()
    @ParameterizedTest
    @MethodSource("getExchangeChatsForValidMessage")
    void testNewMessageForUpdatedExchangeValid_shouldSucceed(ExchangeChat exchangeChat) throws IOException {
        // Arrange
        when(exchangeChatRepositoryMock.findById(exchangeChat.getId())).thenReturn(exchangeChat);
        NewChatMessageDTO newMessage = NewChatMessageDTO.builder().content("Hello there").build();

        // Act
        ChatMessageDTO resultMessage = messagingService.newMessageForUpdatedExchange(exchangeChat,
                newMessage);

        // Assert
        Assertions.assertNotNull(resultMessage);
        Assertions.assertEquals(newMessage.getContent(), resultMessage.getContent());
        Assertions.assertEquals(exchangeChat.getId().toString(), resultMessage.getExchangeID());
        Assertions.assertEquals(autheticatedUser.getUsername(), resultMessage.getAuthor().getUsername());
        Assertions.assertEquals(MessageReadState.UNSEEN, resultMessage.getReadState());
        Assertions.assertTrue(resultMessage.getExchangeChanged());

        // message is published to every participating user
        // authenticatedUser
        String publishedMessage = verifyMessagePublished(autheticatedUser, resultMessage);
        Assertions.assertTrue(publishedMessage.contains("\"readState\":\"UNSEEN\"")); // the author sees the read-state of the message
        Assertions.assertTrue(publishedMessage.contains("\"exchangeChanged\":true"));

        // otherUser
        publishedMessage = verifyMessagePublished(otherUser, resultMessage);
        Assertions.assertTrue(publishedMessage.contains("\"readState\":null")); // other users don't see the read-state of the message
        Assertions.assertTrue(publishedMessage.contains("\"exchangeChanged\":true"));

        // message is not published to non-participating user
        verify(rabbitMQChannelMock, times(0)).basicPublish(eq(EXCHANGE_NAME), eq(otherUser2.getId().toString()), any(), any());
    }

    // since newMessageForUpdatedExchange() is called by other services (ExchangeService), we can assume that the
    // Exchange is valid (the user is participant, etc.)

    // START TESTS getMessagesForExchange()
    @ParameterizedTest
    @MethodSource("getExchangeChatsForValidMessage")
    void testGetMessagesForExchangeValid_shouldSucceedAndReturnMessages(ExchangeChat exchangeChat) throws IOException {
        // Arrange
        when(exchangeChatRepositoryMock.findById(exchangeChat.getId())).thenReturn(
                exchangeChat
        );
        List<ChatMessage> expectedMessages = new ArrayList<>();

        when(messageRepositoryMock.getMessagesForExchange(eq(exchangeChat), eq(null), anyLong())).thenAnswer(
                invocationOnMock -> {
                    List<ChatMessage> messages = getMockChatMessages(exchangeChat, invocationOnMock);
                    expectedMessages.addAll(messages);
                    return messages;
                }
        );
        ChatQueryParamDTO chatQueryParamDTO = new ChatQueryParamDTO();
        chatQueryParamDTO.setCount(10L);

        // Act
        List<ChatMessageDTO> resultMessages = messagingService.getMessagesForExchange(exchangeChat.getId().toString(), chatQueryParamDTO);

        // Assert
        verifyResultMessages(expectedMessages, resultMessages);
    }

    private static void verifyResultMessages(List<ChatMessage> expectedMessages, List<ChatMessageDTO> resultMessages) {
        Assertions.assertEquals(expectedMessages.size(), resultMessages.size());

        // every message id is contained
        Assertions.assertTrue(expectedMessages.stream().allMatch(expectedMessage ->
                        resultMessages.stream()
                                .anyMatch(resultMessage -> expectedMessage.getId().toString().equals(resultMessage.getId()))
                )
        );

        // every message authored by me is unread by the other
        Assertions.assertTrue(resultMessages.stream().allMatch(resultMessage ->
                        // either unseen or I am not the author
                        MessageReadState.UNSEEN.equals(resultMessage.getReadState())
                                || !resultMessage.getAuthor().getUsername().equals(autheticatedUser.getUsername())
                )
        );
        Assertions.assertTrue(expectedMessages.stream().allMatch(expectedMessage ->
                // either unseen or I am not the author
                !expectedMessage.getAuthor().equals(autheticatedUser)
                        || expectedMessage.getUnseenBy().stream().anyMatch(unseen -> unseen.getUser().equals(otherUser))
        ));

        // every message authored by the other user is now read
        Assertions.assertTrue(expectedMessages.stream().allMatch(expectedMessage ->
                // if the other user authored, then must not be unseen by me
                !expectedMessage.getAuthor().equals(otherUser)
                        || expectedMessage.getUnseenBy().isEmpty()
        ));
    }

    private static List<ChatMessage> getMockChatMessages(ExchangeChat exchangeChat, InvocationOnMock invocationOnMock) {
        long count = invocationOnMock.getArgument(2);
        List<ChatMessage> messages = new ArrayList<>();
        for (long i = 0; i < count; i++) {
            messages.add(
                    ChatMessage.builder()
                            .id(UUID.randomUUID())
                            .author(i % 2 == 0 ? autheticatedUser : otherUser)
                            .unseenBy(new HashSet<>(Set.of(
                                    MessageUnreadState.builder()
                                            .readState(MessageReadState.UNSEEN)
                                            .user(i % 2 == 0 ? otherUser : autheticatedUser)
                                            .build()
                            )))
                            .content("Message " + i)
                            .exchangeChat(exchangeChat)
                            .build()
            );
        }
        return messages;
    }

    @ParameterizedTest
    @MethodSource("getExchangeChatsForValidMessage")
    void testGetMessagesForExchangeWithBeforeParameter_shouldSucceedAndReturnMessages(ExchangeChat exchangeChat) {
        // Arrange
        when(exchangeChatRepositoryMock.findById(exchangeChat.getId())).thenReturn(
                exchangeChat
        );

        ChatMessage beforeMessage = ChatMessage.builder()
                .id(UUID.randomUUID())
                .exchangeChat(exchangeChat)
                .build();

        when(messageRepositoryMock.findById(beforeMessage.getId())).thenReturn(
                beforeMessage
        );

        List<ChatMessage> expectedMessages = new ArrayList<>();

        when(messageRepositoryMock.getMessagesForExchange(eq(exchangeChat), eq(beforeMessage), anyLong())).thenAnswer(
                invocationOnMock -> {
                    List<ChatMessage> messages = getMockChatMessages(exchangeChat, invocationOnMock);
                    expectedMessages.addAll(messages);
                    return messages;
                }
        );

        ChatQueryParamDTO chatQueryParamDTO = new ChatQueryParamDTO();
        chatQueryParamDTO.setCount(10L);
        chatQueryParamDTO.setBeforeMessageUUID(beforeMessage.getId().toString());

        //Act
        List<ChatMessageDTO> resultMessages = messagingService.getMessagesForExchange(exchangeChat.getId().toString(), chatQueryParamDTO);

        // Assert
        verifyResultMessages(expectedMessages, resultMessages);
    }

    @ParameterizedTest
    @MethodSource("getExchangeChatsForValidMessage")
    void testGetMessagesForExchangeWithBeforeParameterWrongChat_shouldFailWithNotFoundException(ExchangeChat exchangeChat) {
        // Arrange
        when(exchangeChatRepositoryMock.findById(exchangeChat.getId())).thenReturn(
                exchangeChat
        );

        ChatMessage beforeMessage = ChatMessage.builder()
                .id(UUID.randomUUID())
                // this message belongs to a different Chat!
                .exchangeChat(ExchangeChat.builder()
                        .id(UUID.randomUUID())
                        .build())
                .build();

        when(messageRepositoryMock.findById(beforeMessage.getId())).thenReturn(
                beforeMessage
        );

        ChatQueryParamDTO chatQueryParamDTO = new ChatQueryParamDTO();
        chatQueryParamDTO.setCount(10L);
        chatQueryParamDTO.setBeforeMessageUUID(beforeMessage.getId().toString());

        // Act & Assert
        Assertions.assertThrows(NotFoundException.class, () -> messagingService.getMessagesForExchange(exchangeChat.getId().toString(), chatQueryParamDTO));
    }

    @ParameterizedTest
    @MethodSource("getExchangeChatsNotParticipating")
    void testGetMessagesForExchangeNotParticipating_shouldFailWithNotPartOfExchangeException(ExchangeChat exchangeChat) {
        // Arrange
        when(exchangeChatRepositoryMock.findById(exchangeChat.getId())).thenReturn(
                exchangeChat
        );

        ChatMessage beforeMessage = ChatMessage.builder()
                .id(UUID.randomUUID())
                // this message belongs to a different Chat!
                .exchangeChat(exchangeChat)
                .build();

        when(messageRepositoryMock.findById(beforeMessage.getId())).thenReturn(
                beforeMessage
        );

        ChatQueryParamDTO chatQueryParamDTO = new ChatQueryParamDTO();
        chatQueryParamDTO.setCount(10L);

        // Act & Assert
        Assertions.assertThrows(NotPartOfExchangeException.class, () -> messagingService.getMessagesForExchange(exchangeChat.getId().toString(), chatQueryParamDTO));
        // also fails with the beforeMessageUUID
        chatQueryParamDTO.setBeforeMessageUUID(beforeMessage.getId().toString());
        Assertions.assertThrows(NotPartOfExchangeException.class, () -> messagingService.getMessagesForExchange(exchangeChat.getId().toString(), chatQueryParamDTO));
    }

    @ParameterizedTest
    @MethodSource("getExchangeChatsForValidMessage")
    void testGetMessagesForExchangeMalformedMessageID_ShouldFailWithNotFoundException(ExchangeChat exchangeChat) {
        // Arrange
        when(exchangeChatRepositoryMock.findById(exchangeChat.getId())).thenReturn(
                exchangeChat
        );

        ChatMessage beforeMessage = ChatMessage.builder()
                .id(UUID.randomUUID())
                // this message belongs to a different Chat!
                .exchangeChat(exchangeChat)
                .build();

        when(messageRepositoryMock.findById(beforeMessage.getId())).thenReturn(
                beforeMessage
        );

        ChatQueryParamDTO chatQueryParamDTO = new ChatQueryParamDTO();
        chatQueryParamDTO.setCount(10L);
        chatQueryParamDTO.setBeforeMessageUUID("this-uuid-looks-funny");

        // Act & Assert
        Assertions.assertThrows(NotFoundException.class, () -> messagingService.getMessagesForExchange(exchangeChat.getId().toString(), chatQueryParamDTO));

        // also fails with empty uuid
        chatQueryParamDTO.setBeforeMessageUUID("");
        Assertions.assertThrows(NotFoundException.class, () -> messagingService.getMessagesForExchange(exchangeChat.getId().toString(), chatQueryParamDTO));
    }

    // START TESTS getMostRecentMessageForExchange()
    // The input ExchangeChat will always be valid, since this method is only called by other services

    @ParameterizedTest
    @MethodSource("getExchangeChatsForValidMessage")
    void testGetMostRecentMessageForExchangeWithMessages_shouldSucceed(ExchangeChat exchangeChat) {
        // Arrange
        ChatMessage message = ChatMessage.builder()
                .id(UUID.randomUUID())
                .exchangeChat(exchangeChat)
                .unseenBy(Set.of(
                        MessageUnreadState.builder()
                                .readState(MessageReadState.UNSEEN)
                                .user(autheticatedUser)
                                .build()
                ))
                .author(otherUser)
                .content("Hello World")
                .build();
        when(messageRepositoryMock.getMostRecentMessageForExchange(exchangeChat)).thenReturn(
                message
        );

        // Act
        ChatMessageDTO resultMessage = messagingService.getMostRecentMessageForExchange(exchangeChat);

        // Assert
        // I am not the author, I should not see the read-state
        Assertions.assertNull(resultMessage.getReadState());

        Assertions.assertEquals(message.getId().toString(), resultMessage.getId());
        Assertions.assertEquals(message.getAuthor().getUsername(), resultMessage.getAuthor().getUsername());
        Assertions.assertEquals(message.getContent(), resultMessage.getContent());
    }

    @ParameterizedTest
    @MethodSource("getExchangeChatsForValidMessage")
    void testGetMostRecentMessageForExchangeNoMessages_shouldSucceed(ExchangeChat exchangeChat) {
        // Arrange
        when(messageRepositoryMock.getMostRecentMessageForExchange(exchangeChat)).thenReturn(
                null
        );

        // Act
        ChatMessageDTO resultMessage = messagingService.getMostRecentMessageForExchange(exchangeChat);

        // Assert
        // There should be no message, but also no error
        Assertions.assertNull(resultMessage);
    }

    static Stream<List<ChatMessage>> getUnreadMessages() {
        return Stream.of(
                List.of(
                        ChatMessage.builder()
                                .content("Hello World")
                                .author(otherUser)
                                .unseenBy(Set.of(
                                        MessageUnreadState.builder()
                                                .readState(MessageReadState.UNSEEN)
                                                .user(autheticatedUser)
                                                .build()
                                ))
                                .id(UUID.randomUUID())
                                .exchangeChat(ExchangeChat.builder()
                                        .id(UUID.randomUUID())
                                        .build())
                                .build(),
                        ChatMessage.builder()
                                .content("Hello World 2")
                                .author(otherUser)
                                .unseenBy(Set.of(
                                        MessageUnreadState.builder()
                                                .readState(MessageReadState.UNSEEN)
                                                .user(autheticatedUser)
                                                .build()
                                ))
                                .id(UUID.randomUUID())
                                .exchangeChat(ExchangeChat.builder()
                                        .id(UUID.randomUUID())
                                        .build())
                                .build(),
                        ChatMessage.builder()
                                .content("Hello World 3")
                                .author(otherUser)
                                .unseenBy(Set.of(
                                        MessageUnreadState.builder()
                                                .readState(MessageReadState.NOTIFIED)
                                                .user(autheticatedUser)
                                                .build()
                                ))
                                .id(UUID.randomUUID())
                                .exchangeChat(ExchangeChat.builder()
                                        .id(UUID.randomUUID())
                                        .build())
                                .build()
                )
        );
    }

    // START TESTS getUnreadMessages()
    @ParameterizedTest
    @MethodSource("getUnreadMessages")
    void testGetUnreadMessages_shouldAlwaysSucceed(List<ChatMessage> chatMessages) {
        // Arrange
        PaginationParamsDTO paginationParamsDTO = new PaginationParamsDTO();

        PanacheQuery chatMessagePanacheQuery = mock(PanacheQuery.class);

        when(chatMessagePanacheQuery.page(any())).thenReturn(chatMessagePanacheQuery);
        when(chatMessagePanacheQuery.page()).thenReturn(new Page(paginationParamsDTO.getPage(), paginationParamsDTO.getPageSize()));
        when(chatMessagePanacheQuery.count()).thenReturn((long) chatMessages.size());
        when(chatMessagePanacheQuery.pageCount()).thenReturn(1);
        when(chatMessagePanacheQuery.stream()).thenReturn(chatMessages.stream());

        when(messageRepositoryMock.getUnreadMessagesForUser(autheticatedUser)).thenReturn(chatMessagePanacheQuery);

        // Act
        PaginatedQueryDTO<ChatMessageDTO> unreadMessages = messagingService.getUnreadMessages(paginationParamsDTO);

        // Assert
        Assertions.assertFalse(unreadMessages.isHasMore());
        Assertions.assertEquals(chatMessages.size(), unreadMessages.getTotal());
        Assertions.assertEquals(paginationParamsDTO.getPageSize(), unreadMessages.getPageSize());
        Assertions.assertEquals(paginationParamsDTO.getPage(), unreadMessages.getPage());

        // readState should always be null. A message can only be unread when I am not its author
        // If I am not its author, I cannot see the readState
        Assertions.assertTrue(unreadMessages.getItems().stream().allMatch(msg -> msg.getReadState() == null));
    }


    // START TESTS getNotifications()

    @ParameterizedTest
    @MethodSource("getUnreadMessages")
    void testGetNotifications_shouldSucceedAlways(List<ChatMessage> chatMessages) {
        // Arrange
        autheticatedUser.setUnseenMessages(new HashSet<>(
                        chatMessages.stream()
                                .map(msg ->
                                        msg.getUnseenBy()
                                                .stream()
                                                .filter(unseen -> unseen.getUser().equals(autheticatedUser))
                                                .findFirst()
                                )
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .collect(Collectors.toSet())
                )
        );

        Long expectedNumberOfMessages = chatMessages.stream()
                .map(msg ->
                        msg.getUnseenBy()
                                .stream()
                                .filter(unseen -> unseen.getUser().equals(autheticatedUser) && unseen.getReadState().equals(MessageReadState.UNSEEN))
                                .findFirst()
                )
                .filter(Optional::isPresent)
                .count();


        ChatNotificationDTO notifications = messagingService.getNotifications();

        Assertions.assertEquals(expectedNumberOfMessages, notifications.getNumberOfMessages());

        // after this operation, the message-state is "NOTIFIED"
        Assertions.assertTrue(chatMessages.stream().allMatch(msg -> msg.getUnseenBy()
                        .stream()
                        .anyMatch(unseen -> unseen.getUser().equals(autheticatedUser) && unseen.getReadState().equals(MessageReadState.NOTIFIED))
                )
        );

        // subsequent queries will no longer include those messages
        Assertions.assertEquals(0, messagingService.getNotifications().getNumberOfMessages());

        // reset
        autheticatedUser.setUnseenMessages(new HashSet<>());
    }
}
