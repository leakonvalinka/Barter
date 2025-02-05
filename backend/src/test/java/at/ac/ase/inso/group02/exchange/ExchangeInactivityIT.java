package at.ac.ase.inso.group02.exchange;

import at.ac.ase.inso.group02.LoginITHelper;
import at.ac.ase.inso.group02.bartering.ExchangeController;
import at.ac.ase.inso.group02.bartering.dto.CreateExchangeDTO;
import at.ac.ase.inso.group02.bartering.dto.ExchangeChatDTO;
import at.ac.ase.inso.group02.bartering.dto.ExchangeItemDTO;
import at.ac.ase.inso.group02.bartering.dto.InitiateExchangesDTO;
import at.ac.ase.inso.group02.messaging.MessagingController;
import at.ac.ase.inso.group02.messaging.dto.NewChatMessageDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.util.Set;

import static io.restassured.RestAssured.given;

@QuarkusTest
public class ExchangeInactivityIT extends LoginITHelper {
    @TestHTTPEndpoint(ExchangeController.class)
    @TestHTTPResource
    URL url;

    @TestHTTPEndpoint(MessagingController.class)
    @TestHTTPResource
    URL messagingUrl;

    ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    final String myUsername = "exchangeInactivityUser1";
    final String otherUsername = "exchangeInactivityUser2";

    @ConfigProperty(name = "exchange.rating.allow-after-inactivity", defaultValue = "5")
    Long chatInactivityTimeSeconds;

    private String initiateFreshExchange() throws JsonProcessingException {
        loginUser(myUsername);

        InitiateExchangesDTO initiateExchangesDTO = InitiateExchangesDTO.builder()
                .chatMessage(NewChatMessageDTO.builder()
                        .content("Hi! I would like to exchange this (initial message)")
                        .build())
                .exchanges(Set.of(
                        CreateExchangeDTO.builder()
                                .skillID(-555555555500L)
                                .build()
                ))
                .build();

        String response = given()
                .header("Authorization", "Bearer " + jwtToken)
                .contentType("application/json")
                .body(mapper.writeValueAsString(initiateExchangesDTO))
                .when()
                .post(url)
                .then()
                .statusCode(201)
                .extract()
                .asString();

        ExchangeChatDTO exchangeChat = mapper.readValue(response, ExchangeChatDTO.class);
        return exchangeChat.getId().toString();
    }

    /*
    In both these tests, myUser has initiated an exchange and sent an initial message.
    otherUser has not yet confirmed it by sending a message themselves

    So only when otherUser sends a message, the inactivity timer should start
     */

    @Test
    void testConfirmationMessageFromOtherUser_StartsInactivityTimer() throws JsonProcessingException, InterruptedException {
        String exchangeID = initiateFreshExchange();

        loginUser(otherUsername);

        String exchangeResponse = given()
                .header("Authorization", "Bearer " + jwtToken)
                .contentType("application/json")
                .when()
                .get(url + "/" + exchangeID)
                .then()
                .statusCode(200)
                .extract()
                .asString();

        ExchangeChatDTO exchangeChatDTO = mapper.readValue(exchangeResponse, ExchangeChatDTO.class);

        Assertions.assertEquals(1, exchangeChatDTO.getExchanges().size());
        ExchangeItemDTO exchangeItemDTO = exchangeChatDTO.getExchanges().stream().findFirst().orElseThrow();

        Assertions.assertFalse(exchangeItemDTO.isRatable(), "Exchange-Item should not be ratable at the start");

        // when I post a message, the inactivity timer starts and after 5 seconds, the exchange should be ratable
        NewChatMessageDTO newChatMessageDTO = NewChatMessageDTO.builder()
                .content("Hi! I can confirm this exchange looks good for me!")
                .build();


        given()
                .header("Authorization", "Bearer " + jwtToken)
                .contentType("application/json")
                .body(mapper.writeValueAsString(newChatMessageDTO))
                .when()
                .post(messagingUrl + "/" + exchangeID)
                .then()
                .statusCode(201);

        // 2 seconds grace period
        Thread.sleep(chatInactivityTimeSeconds * 1000 + 2000);

        // now, the exchange should be ratable
        exchangeResponse = given()
                .header("Authorization", "Bearer " + jwtToken)
                .contentType("application/json")
                .when()
                .get(url + "/" + exchangeID)
                .then()
                .statusCode(200)
                .extract()
                .asString();

        exchangeChatDTO = mapper.readValue(exchangeResponse, ExchangeChatDTO.class);

        Assertions.assertEquals(1, exchangeChatDTO.getExchanges().size());
        exchangeItemDTO = exchangeChatDTO.getExchanges().stream().findFirst().orElseThrow();

        Assertions.assertTrue(exchangeItemDTO.isRatable(), "Exchange-Item should now be ratable after both parties exchanged a message and the inactivity period passed");
    }


    @Test
    void testMessageFromMyUser_DoesNotStartInactivityTimer() throws JsonProcessingException, InterruptedException {
        String exchangeID = initiateFreshExchange();

        loginUser(myUsername);

        String exchangeResponse = given()
                .header("Authorization", "Bearer " + jwtToken)
                .contentType("application/json")
                .when()
                .get(url + "/" + exchangeID)
                .then()
                .statusCode(200)
                .extract()
                .asString();

        ExchangeChatDTO exchangeChatDTO = mapper.readValue(exchangeResponse, ExchangeChatDTO.class);

        Assertions.assertEquals(1, exchangeChatDTO.getExchanges().size());
        ExchangeItemDTO exchangeItemDTO = exchangeChatDTO.getExchanges().stream().findFirst().orElseThrow();

        Assertions.assertFalse(exchangeItemDTO.isRatable(), "Exchange-Item should not be ratable at the start");

        // when I post a message, the inactivity timer does not start and after 5 seconds, the exchange is still not ratable
        NewChatMessageDTO newChatMessageDTO = NewChatMessageDTO.builder()
                .content("Hi! Can you please confirm???")
                .build();


        given()
                .header("Authorization", "Bearer " + jwtToken)
                .contentType("application/json")
                .body(mapper.writeValueAsString(newChatMessageDTO))
                .when()
                .post(messagingUrl + "/" + exchangeID)
                .then()
                .statusCode(201);

        // 2 seconds grace period
        Thread.sleep(chatInactivityTimeSeconds * 1000 + 2000);

        // now, the exchange should still not be ratable
        exchangeResponse = given()
                .header("Authorization", "Bearer " + jwtToken)
                .contentType("application/json")
                .when()
                .get(url + "/" + exchangeID)
                .then()
                .statusCode(200)
                .extract()
                .asString();

        exchangeChatDTO = mapper.readValue(exchangeResponse, ExchangeChatDTO.class);

        Assertions.assertEquals(1, exchangeChatDTO.getExchanges().size());
        exchangeItemDTO = exchangeChatDTO.getExchanges().stream().findFirst().orElseThrow();

        Assertions.assertFalse(exchangeItemDTO.isRatable(), "Exchange-Item should still not be ratable after only the initiator exchanged a message, the other user still needs to confirm");
    }
}
