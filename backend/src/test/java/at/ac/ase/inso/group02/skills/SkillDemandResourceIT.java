package at.ac.ase.inso.group02.skills;

import at.ac.ase.inso.group02.LoginITHelper;
import at.ac.ase.inso.group02.entities.DemandUrgency;
import at.ac.ase.inso.group02.skills.dto.CreateSkillDTO;
import at.ac.ase.inso.group02.skills.dto.CreateSkillDemandDTO;
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
import static java.lang.String.valueOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;

@QuarkusTest
public class SkillDemandResourceIT extends LoginITHelper {
    @TestHTTPEndpoint(SkillDemandController.class)
    @TestHTTPResource
    URL url;

    /**
     * login user before test as for most cases, user role is required
     *
     * @throws JsonProcessingException
     */
    @BeforeEach
    void setup() throws JsonProcessingException {
        loginUser("skillDemandITUser");
    }

    static Stream<CreateSkillDemandDTO> invalidDemands() {
        return Stream.of(
                CreateSkillDemandDTO.builder()
                        .title("")
                        .description("This is the new updated description of the demand")
                        .category(CreateSkillDTO.SkillCreateCategoryDTO.builder().id(-1L).build())
                        .urgency(DemandUrgency.HIGH)
                        .build(),
                CreateSkillDemandDTO.builder()
                        .title("New updated title")
                        .description("")
                        .category(CreateSkillDTO.SkillCreateCategoryDTO.builder().id(-1L).build())
                        .urgency(DemandUrgency.HIGH)
                        .build(),
                CreateSkillDemandDTO.builder()
                        .category(CreateSkillDTO.SkillCreateCategoryDTO.builder().id(-1L).build())
                        .urgency(DemandUrgency.HIGH)
                        .build(),
                CreateSkillDemandDTO.builder()
                        .title("New updated title")
                        .description("This is the new updated description of the demand")
                        .build()
        );
    }

    @Test
    void testCreateDemand_shouldSuccessfullyCreateWith201() throws JsonProcessingException {
        CreateSkillDemandDTO demand = CreateSkillDemandDTO.builder()
                .title("Watering plants")
                .description("I need someone to water my garden twice a week as I am unable to at the moment.")
                .category(CreateSkillDTO.SkillCreateCategoryDTO.builder().id(-1L).build())
                .urgency(DemandUrgency.HIGH)
                .build();

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .body(mapper.writeValueAsString(demand))
                .contentType("application/json")
                .when()
                .post(url)
                .then()
                .statusCode(201)
                .body("title", equalTo("Watering plants"))
                .body("urgency", equalTo(valueOf(DemandUrgency.HIGH)))
                .body("byUser.username", equalTo("skillDemandITUser"));
    }

    @ParameterizedTest
    @MethodSource("invalidDemands")
    void testCreateDemandWithEmptyTitle_shouldFailWith400BadRequest(CreateSkillDemandDTO demand) throws JsonProcessingException {
        given()
                .header("Authorization", "Bearer " + jwtToken)
                .body(mapper.writeValueAsString(demand))
                .contentType("application/json")
                .when()
                .post(url)
                .then()
                .statusCode(400)
                .body(containsString("Validation error"));
    }

    @Test
    void testCreateDemandWithNonExistentCategory_shouldFailWith404NotFound() throws JsonProcessingException {
        CreateSkillDemandDTO demand = CreateSkillDemandDTO.builder()
                .title("Watering garden")
                .description("I need someone to water my garden twice a week as I am unable to at the moment.")
                .category(CreateSkillDTO.SkillCreateCategoryDTO.builder().id(-10000L).build())
                .urgency(DemandUrgency.HIGH)
                .build();

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .body(mapper.writeValueAsString(demand))
                .contentType("application/json")
                .when()
                .post(url)
                .then()
                .statusCode(404)
                .body(containsString("Invalid Skill Category"));
    }

    @Test
    void testUpdateDemandAllValues_shouldSuccessfullyUpdateWith200() throws JsonProcessingException {
        CreateSkillDemandDTO demandUpdated = CreateSkillDemandDTO.builder()
                .title("New updated title")
                .description("This is the new updated description of the demand")
                .category(CreateSkillDTO.SkillCreateCategoryDTO.builder().id(-1L).build())
                .urgency(DemandUrgency.HIGH)
                .build();

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .body(mapper.writeValueAsString(demandUpdated))
                .contentType("application/json")
                .when()
                .put(url + "/-8")
                .then()
                .statusCode(200)
                .body("title", equalTo("New updated title"))
                .body("description", equalTo("This is the new updated description of the demand"))
                .body("urgency", equalTo(valueOf(DemandUrgency.HIGH)))
                .body("category.id", equalTo(-1));
    }

    @ParameterizedTest
    @MethodSource("invalidDemands")
    void testUpdateDemandEmptyTitle_shouldFailWith400BadRequest(CreateSkillDemandDTO demand) throws JsonProcessingException {
        given()
                .header("Authorization", "Bearer " + jwtToken)
                .body(mapper.writeValueAsString(demand))
                .contentType("application/json")
                .when()
                .put(url + "/-8")
                .then()
                .statusCode(400)
                .body(containsString("Validation error"));
    }

    @Test
    void testUpdateDemandNonExistentCategory_shouldFailWith400BadRequest() throws JsonProcessingException {
        CreateSkillDemandDTO demandUpdated = CreateSkillDemandDTO.builder()
                .title("New updated title")
                .description("This is the new updated description of the demand")
                .category(CreateSkillDTO.SkillCreateCategoryDTO.builder().id(-10000L).build())
                .urgency(DemandUrgency.HIGH)
                .build();

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .body(mapper.writeValueAsString(demandUpdated))
                .contentType("application/json")
                .when()
                .put(url + "/-8")
                .then()
                .statusCode(404)
                .body(containsString("Invalid Skill Category"));
    }
}
