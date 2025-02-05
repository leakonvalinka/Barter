package at.ac.ase.inso.group02.skills;

import at.ac.ase.inso.group02.LoginITHelper;
import at.ac.ase.inso.group02.authentication.dto.UserLoginDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URL;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;

@QuarkusTest
public class SkillResourceIT extends LoginITHelper {
    @TestHTTPEndpoint(SkillController.class)
    @TestHTTPResource
    URL url;

    /**
     * login user before test as for most cases, user role is required
     * @throws JsonProcessingException
     */
    @BeforeEach
    void setup() throws JsonProcessingException {
        loginUser("user");
    }

    @Test
    void testGetSkills_shouldReturnAllFourSkillsInRange() {
        given()
                .header("Authorization", "Bearer " + jwtToken)
                .when()
                .get(url + "?includeOwn=true")
                .then()
                .statusCode(200)
                .body("total", equalTo(4))
                .body("hasMore", equalTo(false));
    }

    @Test
    void testGetSkillsWithoutOwn_shouldReturnAllTwoSkillsInRange() {
        given()
                .header("Authorization", "Bearer " + jwtToken)
                .when()
                .get(url)
                .then()
                .statusCode(200)
                .body("total", equalTo(2))
                .body("hasMore", equalTo(false));
    }

    @Test
    void testGetSkills_shouldFailForMissingPermissionsWith401() {
        given()
                .when()
                .get(url)
                .then()
                .statusCode(401);
    }

    @Test
    void testGetSkillsWithCategoryFilter_shouldReturnTwoMatchingSkills() {
        given()
                .header("Authorization", "Bearer " + jwtToken)
                .when()
                .get(url + "?category=-2&includeOwn=true")
                .then()
                .statusCode(200)
                .body("total", equalTo(2))
                .body("hasMore", equalTo(false));
    }

    @Test
    void testGetSkillsWith20MeterRadiusFilter_shouldReturnTwoMatchingSkills() {
        given()
                .header("Authorization", "Bearer " + jwtToken)
                .when()
                .get(url + "?radius=20&includeOwn=true")
                .then()
                .statusCode(200)
                .body("total", equalTo(2))
                .body("hasMore", equalTo(false));
    }

    @Test
    void testGetSkillWithGivenId_shouldReturnSkill() {
        given()
                .header("Authorization", "Bearer " + jwtToken)
                .when()
                .get(url + "/-1")
                .then()
                .statusCode(200)
                .body("title", equalTo("Lawnmowing"))
                .body("description", equalTo("I can borrow you my lawnmower!"))
                .body("category.id", equalTo(-1))
                .body("type", equalTo("offer"))
                .body("byUser.username", equalTo("user"))
                .body("schedule", equalTo("weekends"));
    }

    @Test
    void testGetSkillWithNonExistentId_should404NotFound() {
        given()
                .header("Authorization", "Bearer " + jwtToken)
                .when()
                .get(url + "/-1000")
                .then()
                .statusCode(404)
                .body(containsString("Skill with id -1000 does not exist"));
    }

    @Test
    void testDeleteSkillById_shouldDeleteSkill() throws JsonProcessingException {
        UserLoginDTO userLogin = UserLoginDTO.builder()
                .emailOrUsername("deleteSkillUser")
                .password("Password123!")
                .build();

        String userLoginAsJson = mapper.writeValueAsString(userLogin);

        Response response = given()
                .body(userLoginAsJson)
                .contentType("application/json")
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .body("jwt", startsWith("ey"))
                .extract().response();

        String token = response.jsonPath().get("jwt");

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .delete(url + "/-7")
                .then()
                .statusCode(204);
    }

    @Test
    void testDeleteSkillByNonExistentId_shouldFailWith404NotFound() {
        given()
                .header("Authorization", "Bearer " + jwtToken)
                .when()
                .delete(url + "/-1000")
                .then()
                .statusCode(404)
                .body(containsString("Skill with id -1000 does not exist"));
    }

    @Test
    void testDeleteSkillForSkillOfOtherUser_shouldFailWith403Forbidden() {
        given()
                .header("Authorization", "Bearer " + jwtToken)
                .when()
                .delete(url + "/-6")
                .then()
                .statusCode(403)
                .body(containsString("Unauthorized"));
    }

    @Test
    void testGetSkillRatings_shouldReturnTwoRatings() {
        given()
                .header("Authorization", "Bearer " + jwtToken)
                .when()
                .get(url + "/-10/ratings")
                .then()
                .statusCode(200)
                .body("total", equalTo(2))
                .body("items.id", hasItems(-8, -6));
    }

    @Test
    void testGetSkillRatings_shouldFailWithNotFoundForNonExistentSkillId() {
        given()
                .header("Authorization", "Bearer " + jwtToken)
                .when()
                .get(url + "/-1000/ratings")
                .then()
                .statusCode(404)
                .body(containsString("Skill with id -1000 does not exist"));
    }
}
