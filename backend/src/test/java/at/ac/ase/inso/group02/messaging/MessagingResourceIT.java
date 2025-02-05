package at.ac.ase.inso.group02.messaging;

import at.ac.ase.inso.group02.LoginITHelper;
import at.ac.ase.inso.group02.entities.messaging.MessageReadState;
import at.ac.ase.inso.group02.messaging.dto.*;
import at.ac.ase.inso.group02.util.pagination.PaginatedQueryDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.quarkus.logging.Log;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.websockets.next.BasicWebSocketConnector;
import io.quarkus.websockets.next.WebSocketClientConnection;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;

@QuarkusTest
public class MessagingResourceIT extends LoginITHelper {
    @TestHTTPEndpoint(MessagingController.class)
    @TestHTTPResource
    URL url;

    @Inject
    BasicWebSocketConnector connector1;
    @Inject
    BasicWebSocketConnector connector2;

    Map<String, WebSocketClientConnection> webSockets = new HashMap<>();

    ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    // used to wait for a received message
    Map<String, CountDownLatch> latches = new HashMap<>();

    Map<String, List<ChatMessageDTO>> receivedMessages = new HashMap<>();

    final String myUsername = "messagingITUser1";
    final String otherUserName = "messagingITUser2";
    final int TIMEOUT_SECONDS = 5;

    @BeforeEach
    void setUp() throws JsonProcessingException, URISyntaxException {
        setupForUser(myUsername, connector1);
        setupForUser(otherUserName, connector2);
    }

    private void setupForUser(String username, BasicWebSocketConnector connector) throws JsonProcessingException, URISyntaxException {
        loginUser(username);

        // get Websocket ticket
        String responseJson = given()
                .header("Authorization", "Bearer " + jwtToken)
                .contentType("application/json")
                .when()
                .post(url + "/ticket")
                .then()
                .statusCode(200)
                .extract()
                .asString();

        WSTicketDTO ticketDTO = mapper.readValue(responseJson, WSTicketDTO.class);
        Assertions.assertNotNull(ticketDTO, "WSTicketDTO should not be null");
        Assertions.assertNotNull(ticketDTO.getTicketUUID(), "TicketUUID should not be null");

        URI webSocketUri = new URI(url.toString().replace("http", "ws") + "/ws/");

        if (webSockets.containsKey(username)) {
            webSockets.get(username).close().await().atMost(Duration.ofSeconds(TIMEOUT_SECONDS));
        }

        latches.put(username, new CountDownLatch(1));
        receivedMessages.put(username, new ArrayList<>());
        webSockets.put(username, connector
                .baseUri(webSocketUri)
                .path("/{ticket}")
                .pathParam("ticket", ticketDTO.getTicketUUID())
                .onTextMessage((c, m) -> {
                    synchronized (this) {
                        Log.infov("New Message for user {0}: {1}", username, m);
                        try {
                            this.receivedMessages.get(username).add(mapper.readValue(m, ChatMessageDTO.class));
                            this.latches.get(username).countDown();
                        } catch (JsonProcessingException e) {
                            Assertions.fail(e);
                        }
                    }
                })
                .connectAndAwait()
        );

        Assertions.assertTrue(webSockets.get(username).isOpen(), "Connection should be open");
    }

    private void resetReceivedMessages(String username) {
        resetReceivedMessages(username, 1);
    }

    private void resetReceivedMessages(String username, int expectedNumberOfMessages) {
        latches.put(username, new CountDownLatch(expectedNumberOfMessages));
        receivedMessages.get(username).clear();
    }

    static Stream<String> getAllExchangeUUIDs() {
        return Stream.of(
                "abcdef00-0000-0000-0000-000000000000", // this exchange is initiated by myUser
                "abcdef00-0000-0000-0000-000000000001" // this exchange is initiated by otherUser
        );
    }

    @ParameterizedTest
    @MethodSource("getAllExchangeUUIDs")
    void testSendNewMessage_shouldSucceedAndPublishToMe(String exchangeID) throws JsonProcessingException, InterruptedException {
        loginUser(myUsername);
        NewChatMessageDTO newChatMessageDTO = NewChatMessageDTO.builder()
                .content("Hello there!")
                .build();

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .contentType("application/json")
                .body(mapper.writeValueAsString(newChatMessageDTO))
                .when()
                .post(url + "/" + exchangeID)
                .then()
                .statusCode(201);

        // Wait for the message or timeout
        boolean messageReceived = latches.get(myUsername).await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        Assertions.assertTrue(messageReceived, "Message should be received");

        Assertions.assertNotNull(receivedMessages.get(myUsername));
        Assertions.assertEquals(1, receivedMessages.get(myUsername).size());

        Assertions.assertEquals(newChatMessageDTO.getContent(), receivedMessages.get(myUsername).getFirst().getContent());
        Assertions.assertEquals(myUsername, receivedMessages.get(myUsername).getFirst().getAuthor().getUsername());
        Assertions.assertEquals(MessageReadState.UNSEEN, receivedMessages.get(myUsername).getFirst().getReadState());
        Assertions.assertEquals(exchangeID, receivedMessages.get(myUsername).getFirst().getExchangeID());
    }

    @ParameterizedTest
    @MethodSource("getAllExchangeUUIDs")
    void testSendNewMessage_shouldSucceedAndPublishToOtherParticipants(String exchangeID) throws JsonProcessingException, InterruptedException {

        // login as the other participant of the exchange
        loginUser(otherUserName);

        NewChatMessageDTO newChatMessageDTO = NewChatMessageDTO.builder()
                .content("Hello there, other user!")
                .build();

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .contentType("application/json")
                .body(mapper.writeValueAsString(newChatMessageDTO))
                .when()
                .post(url + "/" + exchangeID)
                .then()
                .statusCode(201);

        // Wait for the message or timeout
        boolean messageReceived = latches.get(myUsername).await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        Assertions.assertTrue(messageReceived, "Message should be received");

        Assertions.assertNotNull(receivedMessages.get(myUsername));
        Assertions.assertEquals(1, receivedMessages.get(myUsername).size());

        Assertions.assertEquals(newChatMessageDTO.getContent(), receivedMessages.get(myUsername).getFirst().getContent());
        Assertions.assertEquals(otherUserName, receivedMessages.get(myUsername).getFirst().getAuthor().getUsername());
        Assertions.assertNull(receivedMessages.get(myUsername).getFirst().getReadState());
    }

    @ParameterizedTest
    @MethodSource("getAllExchangeUUIDs")
    void testSendNewMessageAndMarkAsRead_shouldSucceedAndPublishUpdateToOtherParticipants(String exchangeID) throws JsonProcessingException, InterruptedException {

        // login as the other participant of the exchange
        loginUser(otherUserName);

        NewChatMessageDTO newChatMessageDTO = NewChatMessageDTO.builder()
                .content("Hello there!")
                .build();

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .contentType("application/json")
                .body(mapper.writeValueAsString(newChatMessageDTO))
                .when()
                .post(url + "/" + exchangeID)
                .then()
                .statusCode(201);

        // Wait for the message or timeout
        boolean messageReceived = latches.get(myUsername).await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        Assertions.assertTrue(messageReceived, "Message should be received");

        Assertions.assertEquals(1, receivedMessages.get(myUsername).size());
        ChatMessageDTO receivedMessage = receivedMessages.get(myUsername).getFirst();
        Assertions.assertNotNull(receivedMessage);

        Assertions.assertEquals(newChatMessageDTO.getContent(), receivedMessage.getContent());
        Assertions.assertEquals(otherUserName, receivedMessage.getAuthor().getUsername());
        Assertions.assertNull(receivedMessage.getReadState());

        // other user also receives the message
        messageReceived = latches.get(otherUserName).await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        Assertions.assertTrue(messageReceived, "Message should be received");
        resetReceivedMessages(otherUserName);

        // simulate answer to read the received message
        webSockets.get(myUsername).sendText(mapper.writeValueAsString(ReadMessageDTO.builder()
                        .chatMessageID(receivedMessage.getId())
                        .build()))
                .await()
                .atMost(Duration.of(TIMEOUT_SECONDS, ChronoUnit.SECONDS));

        // otherUser should receive update about the read-state
        messageReceived = latches.get(otherUserName).await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        Assertions.assertTrue(messageReceived, "Message update should be received");

        Assertions.assertNotNull(receivedMessages.get(otherUserName));
        Assertions.assertEquals(1, receivedMessages.get(otherUserName).size());

        Assertions.assertEquals(receivedMessage.getId(), receivedMessages.get(otherUserName).getFirst().getId());
        Assertions.assertEquals(otherUserName, receivedMessages.get(otherUserName).getFirst().getAuthor().getUsername());
        Assertions.assertEquals(MessageReadState.SEEN, receivedMessages.get(otherUserName).getFirst().getReadState());
    }


    static Stream<String> getPredefinedMessagesExchangeUUIDs() {
        return Stream.of(
                "abcdef00-0000-0000-0000-000000000002" // this exchange is initiated by otherUser
        );
    }

    @ParameterizedTest
    @MethodSource("getPredefinedMessagesExchangeUUIDs")
    void testGetMessagesForExchange_shouldSucceedAndPublishUpdateToOtherParticipants(String exchangeID) throws JsonProcessingException, InterruptedException {

        // login as the other participant of the exchange
        loginUser(otherUserName);

        List<ChatMessageDTO> existingMessages = given()
                .header("Authorization", "Bearer " + jwtToken)
                .contentType("application/json")
                .when()
                .get(url + "/" + exchangeID)
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeReference<List<ChatMessageDTO>>() {}.getType());

        Assertions.assertEquals(4, existingMessages.size(), "There should be 4 messages in this exchange");

        // the two most recent ones are by otherUser and unseen
        Assertions.assertEquals(otherUserName, existingMessages.get(0).getAuthor().getUsername());
        Assertions.assertEquals(otherUserName, existingMessages.get(1).getAuthor().getUsername());
        Assertions.assertEquals(MessageReadState.UNSEEN, existingMessages.get(0).getReadState());
        Assertions.assertEquals(MessageReadState.UNSEEN, existingMessages.get(1).getReadState());

        // login as myUser
        // querying the list of messages as myUser marks them as seen in the view of the other user:
        loginUser(myUsername);

        // the next action should cause 2 new messages for otherUser
        resetReceivedMessages(otherUserName, 2);

        List<ChatMessageDTO> existingMessagesMyUser = given()
                .header("Authorization", "Bearer " + jwtToken)
                .contentType("application/json")
                .when()
                .get(url + "/" + exchangeID)
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeReference<List<ChatMessageDTO>>() {}.getType());

        Assertions.assertEquals(4, existingMessagesMyUser.size(), "There should be 4 messages in this exchange");

        // the two most recent ones are by otherUser and I don't see their read-state
        Assertions.assertEquals(otherUserName, existingMessagesMyUser.get(0).getAuthor().getUsername());
        Assertions.assertEquals(otherUserName, existingMessagesMyUser.get(1).getAuthor().getUsername());
        Assertions.assertNull(existingMessagesMyUser.get(0).getReadState());
        Assertions.assertNull(existingMessagesMyUser.get(1).getReadState());

        // OtherUser has received updates that the messages were seen
        boolean messageReceived = latches.get(otherUserName).await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        Assertions.assertTrue(messageReceived, "Message updates should be received");

        Assertions.assertEquals(2, receivedMessages.get(otherUserName).size());
        // both read updates have been received (order does not matter)
        Assertions.assertTrue(existingMessages.get(1).getId().equals(receivedMessages.get(otherUserName).get(1).getId()) ||
                existingMessages.get(1).getId().equals(receivedMessages.get(otherUserName).get(0).getId()));

        Assertions.assertTrue(existingMessages.get(0).getId().equals(receivedMessages.get(otherUserName).get(0).getId()) ||
                existingMessages.get(0).getId().equals(receivedMessages.get(otherUserName).get(1).getId()));

        // both messages should be from otherUser and now read
        Assertions.assertEquals(otherUserName, receivedMessages.get(otherUserName).get(0).getAuthor().getUsername());
        Assertions.assertEquals(otherUserName, receivedMessages.get(otherUserName).get(1).getAuthor().getUsername());
        Assertions.assertEquals(MessageReadState.SEEN, receivedMessages.get(otherUserName).get(0).getReadState());
        Assertions.assertEquals(MessageReadState.SEEN, receivedMessages.get(otherUserName).get(1).getReadState());

        // we can also query for the messages before the second-to-last one
        List<ChatMessageDTO> existingMessagesBefore = given()
                .header("Authorization", "Bearer " + jwtToken)
                .contentType("application/json")
                .when()
                .get(url + "/" + exchangeID + "?beforeMessageUUID="+existingMessages.get(1).getId())
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeReference<List<ChatMessageDTO>>() {}.getType());

        Assertions.assertEquals(2, existingMessagesBefore.size(), "There should be 2 messages in this exchange before the latest 2");
        Assertions.assertEquals(existingMessages.get(2).getId(), existingMessagesBefore.get(0).getId());
        Assertions.assertEquals(existingMessages.get(3).getId(), existingMessagesBefore.get(1).getId());
    }

    @Test
    void testGetUnreadMessages_shouldSucceedAndNotMarkAsRead() throws JsonProcessingException {
        loginUser(myUsername);

        resetReceivedMessages(otherUserName);

        PaginatedQueryDTO<ChatMessageDTO> unreadMessages = given()
                .header("Authorization", "Bearer " + jwtToken)
                .contentType("application/json")
                .when()
                .get(url)
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeReference<PaginatedQueryDTO<ChatMessageDTO>>() {}.getType());

        // We cannot assume much here, since other Tests may have introduced more unread messages (or might have read previously unread ones),
        // but these pre-defined ones are never read by the user!
        Assertions.assertTrue(unreadMessages.getItems().stream().anyMatch(msg -> msg.getId().equals("abcdef00-1234-0000-0000-000000000005")));
        Assertions.assertTrue(unreadMessages.getItems().stream().anyMatch(msg -> msg.getId().equals("abcdef00-1234-0000-0000-000000000006")));

        // no unread message can be authored by me, so I cannot see the read-state
        Assertions.assertTrue(unreadMessages.getItems().stream().allMatch(msg -> msg.getReadState() == null));

        // these actions have not marked the messages as read, so otherUser still sees unseen:
        loginUser(otherUserName);
        List<ChatMessageDTO> existingMessages = given()
                .header("Authorization", "Bearer " + jwtToken)
                .contentType("application/json")
                .when()
                .get(url + "/" + "abcdef00-0000-0000-0000-000000000003")
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeReference<List<ChatMessageDTO>>() {}.getType());

        // last two are still unseen:
        Assertions.assertEquals(MessageReadState.UNSEEN, existingMessages.get(0).getReadState());
        Assertions.assertEquals(MessageReadState.UNSEEN, existingMessages.get(1).getReadState());

        // this also causes no update via WebSocket
        Assertions.assertEquals(0, receivedMessages.get(otherUserName).size());
    }


    @Test
    void testGetNotifications_shouldSucceedAndMarkAsNotified() throws JsonProcessingException{
        loginUser(myUsername);

        resetReceivedMessages(otherUserName);

        ChatNotificationDTO notification = given()
                .header("Authorization", "Bearer " + jwtToken)
                .contentType("application/json")
                .when()
                .get(url + "/notifications")
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeReference<ChatNotificationDTO>() {}.getType());

        // We cannot assume much here, since other Tests may have introduced more unread messages (or might have read previously unread ones),
        // but there are always at least two unread messages!
        Assertions.assertTrue(2 <= notification.getNumberOfMessages());

        // doing this again results in no new notifications
        notification = given()
                .header("Authorization", "Bearer " + jwtToken)
                .contentType("application/json")
                .when()
                .get(url + "/notifications")
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeReference<ChatNotificationDTO>() {}.getType());
        Assertions.assertEquals(0, notification.getNumberOfMessages());

        // these actions have not marked the messages as read, so otherUser still sees unseen
        // (Users do not see if other users were notified because that indicates that they are in front of their computer)
        loginUser(otherUserName);
        List<ChatMessageDTO> existingMessages = given()
                .header("Authorization", "Bearer " + jwtToken)
                .contentType("application/json")
                .when()
                .get(url + "/" + "abcdef00-0000-0000-0000-000000000004")
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeReference<List<ChatMessageDTO>>() {}.getType());

        // last two are still unseen:
        Assertions.assertEquals(MessageReadState.UNSEEN, existingMessages.get(0).getReadState());
        Assertions.assertEquals(MessageReadState.UNSEEN, existingMessages.get(1).getReadState());

        // this also causes no update via WebSocket
        Assertions.assertEquals(0, receivedMessages.get(otherUserName).size());
    }
}