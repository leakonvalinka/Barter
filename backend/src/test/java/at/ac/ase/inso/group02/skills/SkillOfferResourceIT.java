package at.ac.ase.inso.group02.skills;

import at.ac.ase.inso.group02.LoginITHelper;
import at.ac.ase.inso.group02.skills.dto.CreateSkillDTO;
import at.ac.ase.inso.group02.skills.dto.CreateSkillOfferDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.URL;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;

@QuarkusTest
public class SkillOfferResourceIT extends LoginITHelper {
    @TestHTTPEndpoint(SkillOfferController.class)
    @TestHTTPResource
    URL url;

    /**
     * login user before test as for most cases, user role is required
     *
     * @throws JsonProcessingException
     */
    @BeforeEach
    void setup() throws JsonProcessingException {
        loginUser("skillOfferITUser");
    }

    static Stream<CreateSkillOfferDTO> invalidOffers() {
        return Stream.of(
                CreateSkillOfferDTO.builder()
                        .title("")
                        .description("This is the new updated description of the offer")
                        .category(CreateSkillDTO.SkillCreateCategoryDTO.builder().id(-1L).build())
                        .schedule("weekends")
                        .build(),
                CreateSkillOfferDTO.builder()
                        .title("New updated title")
                        .description("")
                        .category(CreateSkillDTO.SkillCreateCategoryDTO.builder().id(-1L).build())
                        .schedule("weekends")
                        .build(),
                CreateSkillOfferDTO.builder()
                        .title("New updated title")
                        .description("This is the new updated description of the offer")
                        .category(CreateSkillDTO.SkillCreateCategoryDTO.builder().id(-1L).build())
                        .schedule("")
                        .build(),
                CreateSkillOfferDTO.builder()
                        .description("This is the new updated description of the offer")
                        .category(CreateSkillDTO.SkillCreateCategoryDTO.builder().id(-1L).build())
                        .schedule("weekends")
                        .build(),
                CreateSkillOfferDTO.builder()
                        .title("New updated title")
                        .category(CreateSkillDTO.SkillCreateCategoryDTO.builder().id(-1L).build())
                        .schedule("weekends")
                        .build(),
                CreateSkillOfferDTO.builder()
                        .title("New updated title")
                        .description("This is the new updated description of the offer")
                        .build()
        );
    }

    @Test
    void testCreateOffer_shouldSuccessfullyCreateWith201() throws JsonProcessingException {
        CreateSkillOfferDTO offer = CreateSkillOfferDTO.builder()
                .title("Watering plants")
                .description("I can water your garden for you")
                .category(CreateSkillDTO.SkillCreateCategoryDTO.builder().id(-1L).build())
                .schedule("weekends")
                .build();

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .body(mapper.writeValueAsString(offer))
                .contentType("application/json")
                .when()
                .post(url)
                .then()
                .statusCode(201)
                .body("title", equalTo("Watering plants"))
                .body("schedule", equalTo("weekends"))
                .body("byUser.username", equalTo("skillOfferITUser"));
    }

    @ParameterizedTest
    @MethodSource("invalidOffers")
    void testCreateOfferWithEmptyTitle_shouldFailWith400BadRequest(CreateSkillOfferDTO offer) throws JsonProcessingException {
        given()
                .header("Authorization", "Bearer " + jwtToken)
                .body(mapper.writeValueAsString(offer))
                .contentType("application/json")
                .when()
                .post(url)
                .then()
                .statusCode(400)
                .body(containsString("Validation error"));
    }

    @Test
    void testCreateDemandWithNonExistentCategory_shouldFailWith404NotFound() throws JsonProcessingException {
        CreateSkillOfferDTO offer = CreateSkillOfferDTO.builder()
                .title("Watering plants")
                .description("I can water your garden for you")
                .category(CreateSkillDTO.SkillCreateCategoryDTO.builder().id(-10000L).build())
                .schedule("weekends")
                .build();

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .body(mapper.writeValueAsString(offer))
                .contentType("application/json")
                .when()
                .post(url)
                .then()
                .statusCode(404)
                .body(containsString("Invalid Skill Category"));
    }

    @Test
    void testUpdateDemandAllValues_shouldSuccessfullyUpdateWith200() throws JsonProcessingException {
        CreateSkillOfferDTO offerUpdate = CreateSkillOfferDTO.builder()
                .title("New updated title")
                .description("This is the new updated description of the demand")
                .category(CreateSkillDTO.SkillCreateCategoryDTO.builder().id(-1L).build())
                .schedule("weekends")
                .build();

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .body(mapper.writeValueAsString(offerUpdate))
                .contentType("application/json")
                .when()
                .put(url + "/-9")
                .then()
                .statusCode(200)
                .body("title", equalTo("New updated title"))
                .body("description", equalTo("This is the new updated description of the demand"))
                .body("schedule", equalTo("weekends"))
                .body("category.id", equalTo(-1));
    }

    @ParameterizedTest
    @MethodSource("invalidOffers")
    void testUpdateDemandEmptyTitle_shouldFailWith400BadRequest(CreateSkillOfferDTO offer) throws JsonProcessingException {
        given()
                .header("Authorization", "Bearer " + jwtToken)
                .body(mapper.writeValueAsString(offer))
                .contentType("application/json")
                .when()
                .put(url + "/-9")
                .then()
                .statusCode(400)
                .body(containsString("Validation error"));
    }

    @Test
    void testUpdateDemandNonExistentCategory_shouldFailWith400BadRequest() throws JsonProcessingException {
        CreateSkillOfferDTO demandUpdated = CreateSkillOfferDTO.builder()
                .title("New updated title")
                .description("This is the new updated description of the demand")
                .category(CreateSkillDTO.SkillCreateCategoryDTO.builder().id(-10000L).build())
                .schedule("weekends")
                .build();

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .body(mapper.writeValueAsString(demandUpdated))
                .contentType("application/json")
                .when()
                .put(url + "/-9")
                .then()
                .statusCode(404)
                .body(containsString("Invalid Skill Category"));
    }
}
