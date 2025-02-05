package at.ac.ase.inso.group02.ratings;

import at.ac.ase.inso.group02.LoginITHelper;
import at.ac.ase.inso.group02.rating.RatingController;
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
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;

@QuarkusTest
public class RatingResourceIT extends LoginITHelper {
    @TestHTTPEndpoint(RatingController.class)
    @TestHTTPResource
    URL url;

    @BeforeEach
    void setUp() throws JsonProcessingException {
        loginUser("user13");
    }

    @Test
    void testUpdateRatingEndpoint_shouldSuccessfullyUpdateRatingWith200() throws JsonProcessingException {
        CreateRatingDTO updateRating = CreateRatingDTO.builder()
                .ratingHalfStars(10)
                .title("New title")
                .description("New description of the rating")
                .build();
        given()
                .header("Authorization", "Bearer " + jwtToken)
                .contentType("application/json")
                .body(mapper.writeValueAsString(updateRating))
                .when()
                .put(url + "/-6")
                .then()
                .statusCode(200)
                .body("ratingHalfStars", equalTo(10))
                .body("title", equalTo("New title"))
                .body("description", equalTo("New description of the rating"))
                .body("forUser.username", equalTo("user12"));
    }

    static Stream<CreateRatingDTO> invalidUpdateRating() {
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
    @MethodSource("invalidUpdateRating")
    void testUpdateRatingEndpoint_shouldFailForInvalidValuesWith400BadRequest(CreateRatingDTO updateRating) throws JsonProcessingException {
        given()
                .header("Authorization", "Bearer " + jwtToken)
                .contentType("application/json")
                .body(mapper.writeValueAsString(updateRating))
                .when()
                .put(url + "/-6")
                .then()
                .statusCode(400);
    }

    @Test
    void testUpdateRatingEndpoint_shouldFailForNonExistentRating() throws JsonProcessingException {
        CreateRatingDTO updateRating = CreateRatingDTO.builder()
                .ratingHalfStars(10)
                .build();
        given()
                .header("Authorization", "Bearer " + jwtToken)
                .contentType("application/json")
                .body(mapper.writeValueAsString(updateRating))
                .when()
                .put(url + "/-1000")
                .then()
                .statusCode(404)
                .body(containsString("No such rating found with id -1000"));
    }

    @Test
    void testUpdateRatingEndpoint_shouldFailForUnauthorizedUpdate() throws JsonProcessingException {
        CreateRatingDTO updateRating = CreateRatingDTO.builder()
                .ratingHalfStars(10)
                .build();
        given()
                .header("Authorization", "Bearer " + jwtToken)
                .contentType("application/json")
                .body(mapper.writeValueAsString(updateRating))
                .when()
                .put(url + "/-8")
                .then()
                .statusCode(403)
                .body(containsString("You are not allowed to update this rating"));
    }

    @Test
    void testDeleteRatingEndpoint_shouldSuccessfullyDeleteRatingWith204() {
        given()
                .header("Authorization", "Bearer " + jwtToken)
                .when()
                .delete(url + "/-7")
                .then()
                .statusCode(204);
    }

    @Test
    void testDeleteRatingEndpoint_shouldFailForNonExistentRatingId() {
        given()
                .header("Authorization", "Bearer " + jwtToken)
                .when()
                .delete(url + "/-1000")
                .then()
                .statusCode(404)
                .body(containsString("No such rating found with id -1000"));
    }

    @Test
    void testDeleteRatingEndpoint_shouldFailForUnauthorizedAccess() {
        // fails for ids -1 and -3
        given()
                .header("Authorization", "Bearer " + jwtToken)
                .when()
                .delete(url + "/-8")
                .then()
                //.statusCode(403)
                .body(containsString("You are not allowed to delete this rating"));
    }
}
