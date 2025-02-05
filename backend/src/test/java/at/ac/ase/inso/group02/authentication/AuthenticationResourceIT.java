package at.ac.ase.inso.group02.authentication;

import at.ac.ase.inso.group02.authentication.dto.UserLoginDTO;
import at.ac.ase.inso.group02.authentication.dto.UserRegistrationDTO;
import at.ac.ase.inso.group02.authentication.dto.UserVerificationDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.builder.RequestSpecBuilder;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.jupiter.api.Assertions.fail;

@QuarkusTest
@TestHTTPEndpoint(AuthenticationController.class)
public class AuthenticationResourceIT {

    JsonMapper mapper = JsonMapper.builder().build();

    /**
     * helper function to get a JWT for given user-login information.
     * Useful also for other integration-tests
     */
    public static String getJWTForUser(String userLoginDTOAsString) {
        return given(new RequestSpecBuilder().setBasePath("/auth").build())
                .body(userLoginDTOAsString)
                .contentType("application/json")
                .when()
                .post("/login")
                .then()
                .statusCode(200)
                .body("jwt", startsWith("ey"))
                .body("refreshToken", startsWith("ey"))
                .extract().response()
                .jsonPath().get("jwt");
    }

    @Test
    void testRegisterUser_shouldSucceedWith201() throws JsonProcessingException {
        UserRegistrationDTO user = UserRegistrationDTO.builder()
                .email("test1@email.com")
                .password("TesT_ASE24W")
                .username("username1")
                .build();

        String userAsJson = mapper.writeValueAsString(user);

        given()
                .body(userAsJson)
                .contentType("application/json")
                .when()
                .post("/register")
                .then()
                .statusCode(201)
                .body("email", equalTo("test1@email.com"))
                .body("username", equalTo("username1"))
                .body("displayName", equalTo(null));
    }

    @Test
    void testRegisterUserWithExistingEmail_shouldFailWith409() throws JsonProcessingException {
        UserRegistrationDTO user = UserRegistrationDTO.builder()
                .email("user@example.com")
                .password("TesT_ASE24W")
                .username("username2")
                .build();

        String userAsJson = mapper.writeValueAsString(user);

        given()
                .body(userAsJson)
                .contentType("application/json")
                .when()
                .post("/register")
                .then()
                .statusCode(409)
                .body(containsString("E-Mail already exists"));
    }

    @Test
    void testRegisterUserWithEmptyEmail_shouldFailWith400ValidationError() throws JsonProcessingException {
        UserRegistrationDTO user = UserRegistrationDTO.builder()
                .email("")
                .password("TesT_ASE24W")
                .username("username3")
                .build();

        String userAsJson = mapper.writeValueAsString(user);

        given()
                .body(userAsJson)
                .contentType("application/json")
                .when()
                .post("/register")
                .then()
                .statusCode(400)
                .body(containsString("Validation error"));
    }

    @Test
    void testRegisterUserWithExistingUsername_shouldFailWith409() throws JsonProcessingException {
        UserRegistrationDTO user = UserRegistrationDTO.builder()
                .email("mySecond@email.com")
                .password("TesT_ASE24W")
                .username("user")
                .build();

        String userAsJson = mapper.writeValueAsString(user);

        given()
                .body(userAsJson)
                .contentType("application/json")
                .when()
                .post("/register")
                .then()
                .statusCode(409)
                .body(containsString("Username already exists"));
    }

    @Test
    void testLoginUserForNewUser_shouldFailForUnconfirmedEmail() throws JsonProcessingException {
        UserRegistrationDTO user = UserRegistrationDTO.builder()
                .email("test4@email.com")
                .password("TesT_ASE24W")
                .username("username4")
                .build();

        String userAsJson = mapper.writeValueAsString(user);

        given()
                .body(userAsJson)
                .contentType("application/json")
                .when()
                .post("/register")
                .then()
                .statusCode(201)
                .body("email", equalTo("test4@email.com"))
                .body("username", equalTo("username4"))
                .body("displayName", equalTo(null));

        UserLoginDTO userLogin = UserLoginDTO.builder()
                .emailOrUsername("test4@email.com")
                .password("TesT_ASE24W")
                .build();

        String userLoginAsJson = mapper.writeValueAsString(userLogin);

        given()
                .body(userLoginAsJson)
                .contentType("application/json")
                .when()
                .post("/login")
                .then()
                .statusCode(403)
                .body(containsString("Confirm your email before logging in"));
    }

    @Test
    void testLoginForExistingUser_shouldSucceedWith200() throws JsonProcessingException {
        UserLoginDTO userLogin = UserLoginDTO.builder()
                .emailOrUsername("user@example.com")
                .password("Password123!")
                .build();

        String userLoginAsJson = mapper.writeValueAsString(userLogin);

        getJWTForUser(userLoginAsJson);
    }

    @Test
    void testLoginForExistingUser_shouldFailForWrongPasswordWith401() throws JsonProcessingException {
        UserLoginDTO userLogin = UserLoginDTO.builder()
                .emailOrUsername("user")
                .password("Abcd.123")
                .build();

        String userLoginAsJson = mapper.writeValueAsString(userLogin);

        given()
                .body(userLoginAsJson)
                .contentType("application/json")
                .when()
                .post("/login")
                .then()
                .statusCode(401)
                .body(containsString("Invalid credentials"));
    }


    @Test
    void testVerifyUserEndpoint_shouldVerifyUserSuccessfully() {
        UserVerificationDTO verificationDTO = UserVerificationDTO.builder()
                .email("verify@valid.com")
                .verificationToken("131269")
                .build();

        String dtoJson = "";
        try {
            dtoJson = mapper.writeValueAsString(verificationDTO);
        } catch (JsonProcessingException e) {
            fail("error serializing UserVerificationDTO to json");
        }

        given()
                .body(dtoJson)
                .contentType("application/json")
                .when()
                .post("/verify")
                .then()
                .statusCode(200)
                .body("jwt", startsWith("ey"));
    }


    @Test
    void testVerifyUserEndpointWithWrongCode_shouldFailWithBadRequest() {
        UserVerificationDTO verificationDTO = UserVerificationDTO.builder()
                .email("verify@wrongCode.com")
                .verificationToken("000000")
                .build();

        String dtoJson = "";
        try {
            dtoJson = mapper.writeValueAsString(verificationDTO);
        } catch (JsonProcessingException e) {
            fail("error serializing UserVerificationDTO to json");
        }

        given()
                .body(dtoJson)
                .contentType("application/json")
                .when()
                .post("/verify")
                .then()
                .statusCode(400);
    }


    @Test
    void testVerifyUserEndpointWithExpiredCode_shouldFailWithBadRequest() {
        UserVerificationDTO verificationDTO = UserVerificationDTO.builder()
                .email("verify@expired.com")
                .verificationToken("131269")
                .build();

        String dtoJson = "";
        try {
            dtoJson = mapper.writeValueAsString(verificationDTO);
        } catch (JsonProcessingException e) {
            fail("error serializing UserVerificationDTO to json");
        }

        given()
                .body(dtoJson)
                .contentType("application/json")
                .when()
                .post("/verify")
                .then()
                .statusCode(400);
    }

    @Test
    void testVerifyUserEndpointAlreadyVerified_shouldFailWithConflict() {
        UserVerificationDTO verificationDTO = UserVerificationDTO.builder()
                .email("verify@alreadyVerified.com")
                .verificationToken("131269")
                .build();

        String dtoJson = "";
        try {
            dtoJson = mapper.writeValueAsString(verificationDTO);
        } catch (JsonProcessingException e) {
            fail("error serializing UserVerificationDTO to json");
        }

        given()
                .body(dtoJson)
                .contentType("application/json")
                .when()
                .post("/verify")
                .then()
                .statusCode(409);
    }
}
