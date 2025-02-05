package at.ac.ase.inso.group02.authentication;

import at.ac.ase.inso.group02.authentication.dto.UserDetailDTO;
import at.ac.ase.inso.group02.authentication.dto.UserLocationDTO;
import at.ac.ase.inso.group02.authentication.dto.UserLoginDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.net.URL;

import static at.ac.ase.inso.group02.authentication.AuthenticationResourceIT.getJWTForUser;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;

@QuarkusTest
public class UserResourceIT {
    @TestHTTPEndpoint(UserController.class)
    @TestHTTPResource
    URL url;

    JsonMapper mapper = JsonMapper.builder().build();

    @Test
    void testfindUserByUsername_shouldFindUser() {
        given()
                .when()
                .get(url+"/user")
                .then()
                .statusCode(200)
                .body("username", equalTo("user"))
                .body("displayName", equalTo("User 1"));
    }

    @Test
    void testfindUserByUsername_shouldFailForNonExistentUsername() {
        given()
                .when()
                .get(url+"/user12345")
                .then()
                .statusCode(404)
                .body(containsString("User not found for username: user12345"));
    }

    @Test
    void testDeleteUser_shouldSuccessfullyDeleteLoggedInUser() throws JsonProcessingException {
        UserLoginDTO userLogin = UserLoginDTO.builder()
                .emailOrUsername("toDeleteUser")
                .password("Password123!")
                .build();

        String userLoginAsJson = mapper.writeValueAsString(userLogin);

        String token = getJWTForUser(userLoginAsJson);

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .delete(url)
                .then()
                .statusCode(204);
    }

    @Test
    void testDeleteUser_shouldFailWith401ForMissingToken() {
        given()
                .when()
                .delete(url)
                .then()
                .statusCode(401);
    }

    @Test
    void testUpdateUser_shouldSuccessfullyUpdateLoggedInUser() throws JsonProcessingException {
        UserLoginDTO userLogin = UserLoginDTO.builder()
                .emailOrUsername("toUpdateUser")
                .password("Password123!")
                .build();
        UserDetailDTO updateData = UserDetailDTO.builder()
                .email("toUpdateUser@example.com")
                .username("toUpdateUser")
                .displayName("New Display Name")
                .profilePicture(null)
                .bio("New Bio")
                .build();

        String userLoginAsJson = mapper.writeValueAsString(userLogin);

        String token = getJWTForUser(userLoginAsJson);
        String updateDataAsJson = mapper.writeValueAsString(updateData);

        given()
                .header("Authorization", "Bearer " + token)
                .body(updateDataAsJson)
                .contentType("application/json")
                .when()
                .put(url)
                .then()
                .statusCode(200)
                .body("email", equalTo("toUpdateUser@example.com"))
                .body("username", equalTo("toUpdateUser"))
                .body("displayName", equalTo(updateData.getDisplayName()))
                .body("bio", equalTo(updateData.getBio()))
                .body("profilePicture", equalTo(updateData.getProfilePicture()));
    }

    @Test
    void testUpdateUser_shouldSucceedWhenUpdatingAddress() throws JsonProcessingException {
        UserLoginDTO userLogin = UserLoginDTO.builder()
                .emailOrUsername("toUpdateUser")
                .password("Password123!")
                .build();
        UserDetailDTO updateData = UserDetailDTO.builder()
                .email("toUpdateUser@example.com")
                .username("toUpdateUser")
                .location(UserLocationDTO.builder()
                        .street("New Street")
                        .streetNumber("82/5")
                        .city("Graz")
                        .postalCode(8010)
                        .build())
                .build();

        String userLoginAsJson = mapper.writeValueAsString(userLogin);

        String token = getJWTForUser(userLoginAsJson);
        String updateDataAsJson = mapper.writeValueAsString(updateData);

        given()
                .header("Authorization", "Bearer " + token)
                .body(updateDataAsJson)
                .contentType("application/json")
                .when()
                .put(url)
                .then()
                .statusCode(200)
                .body("email", equalTo("toUpdateUser@example.com"))
                .body("username", equalTo("toUpdateUser"))
                .body("location.street", equalTo(updateData.getLocation().getStreet()))
                .body("location.streetNumber", equalTo(updateData.getLocation().getStreetNumber()))
                .body("location.city", equalTo(updateData.getLocation().getCity()))
                .body("location.postalCode", equalTo(updateData.getLocation().getPostalCode()));
    }

    @Test
    void testUpdateUser_shouldFailWith401ForMissingToken() throws JsonProcessingException {
        UserDetailDTO updateData = UserDetailDTO.builder()
                .email("toUpdateUser@example.com")
                .username("toUpdateUser")
                .displayName("New Display Name")
                .bio("New Bio")
                .build();

        String updateDataAsJson = mapper.writeValueAsString(updateData);

        given()
                .body(updateDataAsJson)
                .contentType("application/json")
                .when()
                .put(url)
                .then()
                .statusCode(401);
    }

    @Test
    void testGetRatingsForUsername_shouldReturnTwoRatings() throws JsonProcessingException {
        UserLoginDTO userLogin = UserLoginDTO.builder()
                .emailOrUsername("user12")
                .password("Password123!")
                .build();

        String jwtToken = given()
                .body(mapper.writeValueAsString(userLogin))
                .contentType("application/json")
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .body("jwt", startsWith("ey"))
                .extract().response()
                .jsonPath().get("jwt");

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .when()
                .get(url+"/user13/ratings")
                .then()
                .statusCode(200)
                .body("total", equalTo(2))
                .body("items.id", hasItems(-9, -10));
    }

    @Test
    void testGetRatingsForUsername_shouldReturn404ForNonExistentUser() throws JsonProcessingException {
        UserLoginDTO userLogin = UserLoginDTO.builder()
                .emailOrUsername("user12")
                .password("Password123!")
                .build();

        String jwtToken = given()
                .body(mapper.writeValueAsString(userLogin))
                .contentType("application/json")
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .body("jwt", startsWith("ey"))
                .extract().response()
                .jsonPath().get("jwt");

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .when()
                .get(url+"/user1000/ratings")
                .then()
                .statusCode(404)
                .body(containsString("No user found with username user1000"));
    }
}

