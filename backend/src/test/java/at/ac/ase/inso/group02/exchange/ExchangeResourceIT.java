package at.ac.ase.inso.group02.exchange;

import at.ac.ase.inso.group02.LoginITHelper;
import at.ac.ase.inso.group02.bartering.ExchangeController;
import at.ac.ase.inso.group02.bartering.dto.CreateExchangeDTO;
import at.ac.ase.inso.group02.bartering.dto.InitiateExchangesDTO;
import at.ac.ase.inso.group02.messaging.dto.NewChatMessageDTO;
import at.ac.ase.inso.group02.rating.dto.CreateRatingDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.URL;
import java.util.Set;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;

@QuarkusTest
public class ExchangeResourceIT  extends LoginITHelper {
    @TestHTTPEndpoint(ExchangeController.class)
    @TestHTTPResource
    URL url;

    @BeforeEach
    void setUp() throws JsonProcessingException {
        loginUser("user15");
    }

    @Test
    void testCreateRating_shouldCreateRatingSuccessfully() throws JsonProcessingException {
        // create rating by user15 for user17 (responder-rating): exchange -10
        CreateRatingDTO rating = CreateRatingDTO.builder()
                .ratingHalfStars(9)
                .title("Friendly guy")
                .description("Helped me a lot.")
                .build();

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .contentType("application/json")
                .body(mapper.writeValueAsString(rating))
                .when()
                .post(url + "/item/-10/rate")
                .then()
                .statusCode(201)
                .body("title", equalTo("Friendly guy"))
                .body("description", equalTo("Helped me a lot."))
                .body("ratingHalfStars", equalTo(9))
                .body("forUser.username", equalTo("user17"));
    }

    static Stream<CreateRatingDTO> invalidRatings() {
        return Stream.of(
                CreateRatingDTO.builder()
                        .ratingHalfStars(11)
                        .build(),
                CreateRatingDTO.builder()
                        .ratingHalfStars(-1)
                        .build()
        );
    }

    @ParameterizedTest
    @MethodSource("invalidRatings")
    void testCreateRating_shouldFailForInvalidValues(CreateRatingDTO rating) throws JsonProcessingException {
        given()
                .header("Authorization", "Bearer " + jwtToken)
                .contentType("application/json")
                .body(mapper.writeValueAsString(rating))
                .when()
                .post(url + "/item/-11/rate")
                .then()
                .statusCode(400);
    }

    @Test
    void testCreateRating_shouldFailForNonExistentExchangeId() throws JsonProcessingException {
        CreateRatingDTO rating = CreateRatingDTO.builder()
                .ratingHalfStars(9)
                .build();

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .contentType("application/json")
                .body(mapper.writeValueAsString(rating))
                .when()
                .post(url + "/item/-1000/rate")
                .then()
                .statusCode(404)
                .body(containsString("No exchange found"));
    }

    @Test
    void testCreateRating_shouldFailBecauseUserIsNotPartOfExchange() throws JsonProcessingException {
        CreateRatingDTO rating = CreateRatingDTO.builder()
                .ratingHalfStars(9)
                .build();

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .contentType("application/json")
                .body(mapper.writeValueAsString(rating))
                .when()
                .post(url + "/item/-11/rate")
                .then()
                .statusCode(403)
                .body(containsString("You are not allowed to create a rating for the exchange, " +
                        "since you were not part of it"));
    }

    @Test
    void testCreateRating_shouldFailBecauseExchangeIsNotRatable() throws JsonProcessingException {
        CreateRatingDTO rating = CreateRatingDTO.builder()
                .ratingHalfStars(9)
                .build();

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .contentType("application/json")
                .body(mapper.writeValueAsString(rating))
                .when()
                .post(url + "/item/-13/rate")
                .then()
                .statusCode(409)
                .body(containsString("Exchange is not ratable"));
    }

    @Test
    void testGetExchange_shouldReturnExchange() {
        given()
                .header("Authorization", "Bearer " + jwtToken)
                .when()
                .get(url + "/item/-8")
                .then()
                .statusCode(200)
                .body("numberOfExchanges", equalTo(1))
                .body("ratable", equalTo(true))
                .body("responderMarkedComplete", equalTo(false))
                .body("initiatorMarkedComplete", equalTo(false))
                .body("initiator.username", equalTo("user16"))
                .body("initiatorRating.title", equalTo("Initiatorrating exchange -8"))
                .body("responderRating.title", equalTo("Responderrating exchange -8"))
                .body("exchangedSkill.id", equalTo(-13));
    }

    @Test
    void testGetExchange_shouldFailAsUserIsNotPartOfExchange() {
        given()
                .header("Authorization", "Bearer " + jwtToken)
                .when()
                .get(url + "/item/-9")
                .then()
                .statusCode(403)
                .body(containsString("You are not part of this exchange"));
    }

    @Test
    void testGetExchange_shouldFailForNonExistentExchange() {
        given()
                .header("Authorization", "Bearer " + jwtToken)
                .when()
                .get(url + "/item/-1000")
                .then()
                .statusCode(404)
                .body(containsString("No exchange found"));
    }

    @Test
    void testMarkExchangeAsCompelte_shouldMarkAsComplete() {
        given()
                .header("Authorization", "Bearer " + jwtToken)
                .header("Content-Type", "application/json")
                .when()
                .post(url + "/item/-12/complete")
                .then()
                .statusCode(200);
    }

    @Test
    void testMarkExchangeAsCompelte_shouldFailAsUserIsNotPartOfExchange() {
        given()
                .header("Authorization", "Bearer " + jwtToken)
                .header("Content-Type", "application/json")
                .when()
                .post(url + "/item/-9/complete")
                .then()
                .statusCode(403)
                .body(containsString("You cannot finalize this exchange because you are not part of it"));
    }

    @Test
    void testMarkExchangeAsCompelte_shouldFailForNonExistentExchange() {
        given()
                .header("Authorization", "Bearer " + jwtToken)
                .header("Content-Type", "application/json")
                .when()
                .post(url + "/item/-1000/complete")
                .then()
                .statusCode(404)
                .body(containsString("No exchange found"));
    }

    @Test
    void testInitiateExchange_shouldSuccessfullyInitiate() throws JsonProcessingException {
        InitiateExchangesDTO newExchange = InitiateExchangesDTO.builder()
                .chatMessage(NewChatMessageDTO.builder()
                        .content("Hello, I would like to take you up on this offer!")
                        .build())
                .exchanges(Set.of(CreateExchangeDTO.builder()
                        .skillID(Long.valueOf(-16))
                        .build()))
                .build();

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .header("Content-Type", "application/json")
                .body(mapper.writeValueAsString(newExchange))
                .when()
                .post(url)
                .then()
                .statusCode(201)
                .body("initiator.username", equalTo("user15"))
                .body("confirmationResponsePending", equalTo(false))
                .body("exchanges[0].exchangedSkill.id", equalTo(-16))
                .body("mostRecentMessage.content", equalTo("Hello, I would like to take you up on this offer!"));
    }

    @Test
    void testInitiateExchange_shouldFailForMissingSkill() throws JsonProcessingException {
        InitiateExchangesDTO newExchange = InitiateExchangesDTO.builder()
                .chatMessage(NewChatMessageDTO.builder()
                        .content("Hello, I would like to take you up on this offer!")
                        .build())
                .exchanges(Set.of())
                .build();

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .header("Content-Type", "application/json")
                .body(mapper.writeValueAsString(newExchange))
                .when()
                .post(url)
                .then()
                .statusCode(400);
    }

    @Test
    void testInitiateExchange_shouldFailWhenTryingToExchangeWithOneself() throws JsonProcessingException {
        InitiateExchangesDTO newExchange = InitiateExchangesDTO.builder()
                .chatMessage(NewChatMessageDTO.builder()
                        .content("Hello, I would like to take you up on this offer!")
                        .build())
                .exchanges(Set.of(CreateExchangeDTO.builder()
                        .skillID(Long.valueOf(-13))
                        .build()))
                .build();

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .header("Content-Type", "application/json")
                .body(mapper.writeValueAsString(newExchange))
                .when()
                .post(url)
                .then()
                .statusCode(409)
                .body(containsString("You cannot exchange a skill with yourself!"));
    }

    @Test
    void testUpdateExchange_shouldSuccessfullyUpdate() throws JsonProcessingException {
        InitiateExchangesDTO newExchange = InitiateExchangesDTO.builder()
                .chatMessage(NewChatMessageDTO.builder()
                                        .content("Test")
                                        .build())
                .exchanges(Set.of(CreateExchangeDTO.builder()
                                        .skillID(Long.valueOf(-16))
                                        .skillCounterPartID(Long.valueOf(-13))
                                        .forUser("user15")
                                        .build()))
                .build();

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .header("Content-Type", "application/json")
                .body(mapper.writeValueAsString(newExchange))
                .when()
                .put(url + "/00000000-0000-4000-8000-000000000004")
                .then()
                .statusCode(201)
                .body("id", equalTo("00000000-0000-4000-8000-000000000004"))
                .body("exchanges[0].exchangedSkill.id", equalTo(-16))
                .body("exchanges[0].exchangedSkillCounterpart.id", equalTo(-13));
    }

    @Test
    void testUpdateExchange_shouldFailToUpdateForSkillAndCounterpartBeingDemands() throws JsonProcessingException {
        InitiateExchangesDTO newExchange = InitiateExchangesDTO.builder()
                .chatMessage(NewChatMessageDTO.builder()
                        .content("Test")
                        .build())
                .exchanges(Set.of(CreateExchangeDTO.builder()
                        .skillID(Long.valueOf(-16))
                        .skillCounterPartID(Long.valueOf(-17))
                        .build()))
                .build();

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .header("Content-Type", "application/json")
                .body(mapper.writeValueAsString(newExchange))
                .when()
                .put(url + "/00000000-0000-4000-8000-000000000004")
                .then()
                .statusCode(409)
                .body(containsString("The counterpart to an exchanged skill is the same type (demand/offer) as the exchanged skill itself!"));
    }

    @Test
    void testUpdateExchange_shouldFailToUpdateForUninvolvedUser() throws JsonProcessingException {
        InitiateExchangesDTO newExchange = InitiateExchangesDTO.builder()
                .chatMessage(NewChatMessageDTO.builder()
                        .content("Test")
                        .build())
                .exchanges(Set.of(CreateExchangeDTO.builder()
                        .skillID(Long.valueOf(-16))
                        .skillCounterPartID(Long.valueOf(-13))
                        .forUser("user17")
                        .build()))
                .build();

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .header("Content-Type", "application/json")
                .body(mapper.writeValueAsString(newExchange))
                .when()
                .put(url + "/00000000-0000-4000-8000-000000000004")
                .then()
                .statusCode(409)
                .body(containsString("The initiator for skill -16 does not correspond to the creator of the skill-counterpart"));
    }

    @Test
    void testUpdateExchange_shouldFailForUserNotBeingPartOfTheExchange() throws JsonProcessingException {
        InitiateExchangesDTO newExchange = InitiateExchangesDTO.builder()
                .chatMessage(NewChatMessageDTO.builder()
                        .content("Test")
                        .build())
                .exchanges(Set.of(CreateExchangeDTO.builder()
                        .skillID(Long.valueOf(-16))
                        .forUser("user16")
                        .build()))
                .build();

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .header("Content-Type", "application/json")
                .body(mapper.writeValueAsString(newExchange))
                .when()
                .put(url + "/00000000-0000-4000-8000-000000000004")
                .then()
                .statusCode(409)
                .body(containsString("You must partake in an exchange"));
    }

    @Test
    void testUpdateExchange_shouldFailForDuplicateExchange() throws JsonProcessingException {
        InitiateExchangesDTO newExchange = InitiateExchangesDTO.builder()
                .chatMessage(NewChatMessageDTO.builder()
                        .content("Test")
                        .build())
                .exchanges(Set.of(CreateExchangeDTO.builder()
                                        .skillID(Long.valueOf(-16))
                                        .skillCounterPartID(Long.valueOf(-13))
                                        .forUser("user15")
                                        .build(),
                                CreateExchangeDTO.builder()
                                        .skillID(Long.valueOf(-16))
                                        .skillCounterPartID(Long.valueOf(-13))
                                        .build()))
                .build();

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .header("Content-Type", "application/json")
                .body(mapper.writeValueAsString(newExchange))
                .when()
                .put(url + "/00000000-0000-4000-8000-000000000004")
                .then()
                .statusCode(409)
                .body(containsString("There are duplicate exchanges! The exchange of one skill with one user can only occur once in an exchange!"));
    }

    @Test
    void testUpdateExchange_shouldFailForInvalidChatId() throws JsonProcessingException {
        InitiateExchangesDTO newExchange = InitiateExchangesDTO.builder()
                .chatMessage(NewChatMessageDTO.builder()
                        .content("Test")
                        .build())
                .exchanges(Set.of(CreateExchangeDTO.builder()
                                .skillID(Long.valueOf(-16))
                                .skillCounterPartID(Long.valueOf(-13))
                                .forUser("user15")
                                .build(),
                        CreateExchangeDTO.builder()
                                .skillID(Long.valueOf(-16))
                                .skillCounterPartID(Long.valueOf(-13))
                                .build()))
                .build();

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .header("Content-Type", "application/json")
                .body(mapper.writeValueAsString(newExchange))
                .when()
                .put(url + "/00000000-0000-4000-8000-000000001111")
                .then()
                .statusCode(404)
                .body(containsString("No exchange-chat found with id 00000000-0000-4000-8000-000000001111"));
    }

    @Test
    void testUpdateExchange_shouldFailForSameUser() throws JsonProcessingException {
        InitiateExchangesDTO newExchange = InitiateExchangesDTO.builder()
                .chatMessage(NewChatMessageDTO.builder()
                        .content("Test")
                        .build())
                .exchanges(Set.of(CreateExchangeDTO.builder()
                                .skillID(Long.valueOf(-16))
                                .skillCounterPartID(Long.valueOf(-15))
                                .build()))
                .build();

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .header("Content-Type", "application/json")
                .body(mapper.writeValueAsString(newExchange))
                .when()
                .put(url + "/00000000-0000-4000-8000-000000000004")
                .then()
                .statusCode(409)
                .body(containsString("Another user cannot exchange a skill with themselves"));
    }

    @Test
    void testUpdateExchange_shouldFailForAlreadyCompletedExchange() throws JsonProcessingException {
        InitiateExchangesDTO newExchange = InitiateExchangesDTO.builder()
                .chatMessage(NewChatMessageDTO.builder()
                        .content("Test")
                        .build())
                .exchanges(Set.of(CreateExchangeDTO.builder()
                        .skillID(Long.valueOf(-18))
                        .skillCounterPartID(Long.valueOf(-13))
                        .build()))
                .build();

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .header("Content-Type", "application/json")
                .body(mapper.writeValueAsString(newExchange))
                .when()
                .put(url + "/00000000-0000-4000-8000-000000000005")
                .then()
                .statusCode(409)
                .body(containsString("Cannot edit at least one exchange because it is already finalized"));
    }

    @Test
    void testGetExchangeChat_shouldFindChatById() {
        given()
                .header("Authorization", "Bearer " + jwtToken)
                .when()
                .get(url + "/00000000-0000-4000-8000-000000000005")
                .then()
                .statusCode(200)
                .body("initiator.username", equalTo("user15"));
    }

    @Test
    void testGetExchangeChat_shouldNotFindChatForNonExistentId() {
        given()
                .header("Authorization", "Bearer " + jwtToken)
                .when()
                .get(url + "/00000000-0000-4000-8000-000000000111")
                .then()
                .statusCode(404);
    }

    @Test
    void testGetExchangeChats_shouldFindAllChatsForUser() throws JsonProcessingException {
        // user 16 because previous tests initiate an exchange for user 15 and 17
        loginUser("user16");

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .when()
                .get(url)
                .then()
                .statusCode(200)
                .body("total", equalTo(1))
                .body("items[0].id", equalTo("00000000-0000-4000-8000-000000000005"));
    }

    @Test
    void testGetExchangeChatsForOtherUser_shouldFindAllChatsForUserWithProvidedUsername() {
        given()
                .header("Authorization", "Bearer " + jwtToken)
                .when()
                .get(url + "/user/user16")
                .then()
                .statusCode(200)
                .body("total", equalTo(1))
                .body("items[0].id", equalTo("00000000-0000-4000-8000-000000000005"));
    }

    @Test
    void testGetExchangeChatsForOtherUser_shouldFind0ChatsForUserWithNoExchanges() {
        given()
                .header("Authorization", "Bearer " + jwtToken)
                .when()
                .get(url + "/user/user18")
                .then()
                .statusCode(200)
                .body("total", equalTo(0));
    }

    @Test
    void testGetExchangeChatsForOtherUser_shouldFailForNonExistentUser() {
        given()
                .header("Authorization", "Bearer " + jwtToken)
                .when()
                .get(url + "/user/user100")
                .then()
                .statusCode(404);
    }
}
