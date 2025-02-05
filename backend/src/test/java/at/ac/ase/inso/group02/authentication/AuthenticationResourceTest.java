package at.ac.ase.inso.group02.authentication;

import at.ac.ase.inso.group02.authentication.dto.UserDetailDTO;
import at.ac.ase.inso.group02.authentication.dto.UserInfoDTO;
import at.ac.ase.inso.group02.authentication.dto.UserRegistrationDTO;
import at.ac.ase.inso.group02.authentication.dto.UserVerificationDTO;
import at.ac.ase.inso.group02.authentication.exception.AlreadyVerifiedException;
import at.ac.ase.inso.group02.authentication.exception.VerificationTokenExpiredException;
import at.ac.ase.inso.group02.authentication.exception.WrongVerificationTokenException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestHTTPEndpoint(AuthenticationController.class)
public class AuthenticationResourceTest {

    JsonMapper jsonMapper = JsonMapper.builder().build();

    @InjectMock
    AuthenticationService authenticationServiceMock;

    String dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS";
    private LocalDateTime now;
    private UserDetailDTO sampleUser;

    @BeforeEach
    void setup() {
        now = LocalDateTime.now();
        sampleUser = UserDetailDTO.builder()
                .email("my@email.com")
                .username("username")
                .displayName("My Name")
                .createdAt(now)
                .build();
    }


    @Test
    void testRegisterUserEndpointValid_shouldSucceedAndCreateUser() throws JsonProcessingException {
        // Act
        UserRegistrationDTO toRegisterUser = UserRegistrationDTO.builder()
                .email("my@email.com")
                .password("TesT_ASE24W")
                .username("username")
                .build();
        LocalDateTime now = LocalDateTime.now();
        UserInfoDTO registeredUser = UserInfoDTO.builder()
                .email("my@email.com")
                .username("username")
                .displayName("My Name")
                .createdAt(now)
                .build();

        when(authenticationServiceMock.registerUser(toRegisterUser)).thenReturn(registeredUser);

        String toRegisterAsJson = jsonMapper.writeValueAsString(toRegisterUser);

        // Act & Assert
        given()
                .body(toRegisterAsJson)
                .contentType("application/json")
                .when().post("/register")
                .then()
                .statusCode(201)
                .body("email", equalTo(toRegisterUser.getEmail()))
                .body("username", equalTo(toRegisterUser.getUsername()))
                .body("displayName", equalTo("My Name"))
                .body("createdAt", is(now.format(DateTimeFormatter.ofPattern(dateFormat))));
    }


    @Test
    void testRegisterUserEndpointInvalidUsername_shouldFail() throws JsonProcessingException {
        // Arrange
        UserRegistrationDTO toRegisterUser = UserRegistrationDTO.builder()
                .email("my@email.com")
                .password("TesT_ASE24W")
                .username("an@email.username")
                .build();

        when(authenticationServiceMock.registerUser(toRegisterUser)).thenThrow(ConstraintViolationException.class);

        String toRegisterAsJson = jsonMapper.writeValueAsString(toRegisterUser);

        // Act & Assert
        given()
                .body(toRegisterAsJson)
                .contentType("application/json")
                .when().post("/register")
                .then()
                .statusCode(400);
    }

    @Test
    void testVerifyUser_shouldSucceed() throws JsonProcessingException {
        // Arrange
        UserVerificationDTO verificationDTO = UserVerificationDTO
                .builder()
                .verificationToken("131269")
                .email("test@test.com")
                .build();

        String dtoJson = jsonMapper.writeValueAsString(verificationDTO);

        // Act & Assert
        given()
                .body(dtoJson)
                .contentType("application/json")
                .when().post("/verify")
                .then()
                .statusCode(200);
    }

    @Test
    void testVerifyUserExpiredCode_shouldFailWithBadRequest() throws JsonProcessingException {
        // Arrange
        UserVerificationDTO verificationDTO = UserVerificationDTO
                .builder()
                .verificationToken("131269")
                .email("test@test.com")
                .build();

        String dtoJson = jsonMapper.writeValueAsString(verificationDTO);

        doThrow(VerificationTokenExpiredException.class).when(authenticationServiceMock).verifyUser(verificationDTO);

        // Act & Assert
        given()
                .body(dtoJson)
                .contentType("application/json")
                .when().post("/verify")
                .then()
                .statusCode(400);
    }

    @Test
    void testVerifyUserWrongCode_shouldFailWithBadRequest() throws JsonProcessingException {
        // Arrange
        UserVerificationDTO verificationDTO = UserVerificationDTO
                .builder()
                .verificationToken("131269")
                .email("test@test.com")
                .build();

        String dtoJson = jsonMapper.writeValueAsString(verificationDTO);
        doThrow(WrongVerificationTokenException.class).when(authenticationServiceMock).verifyUser(verificationDTO);

        // Act & Assert
        given()
                .body(dtoJson)
                .contentType("application/json")
                .when().post("/verify")
                .then()
                .statusCode(400);
    }

    @Test
    void testVerifyUserAlreadyVerified_shouldFailWithConflict() throws JsonProcessingException {
        // Arrange
        UserVerificationDTO verificationDTO = UserVerificationDTO
                .builder()
                .verificationToken("131269")
                .email("test@test.com")
                .build();

        doThrow(AlreadyVerifiedException.class).when(authenticationServiceMock).verifyUser(verificationDTO);

        String dtoJson = jsonMapper.writeValueAsString(verificationDTO);

        // Act & Assert
        given()
                .body(dtoJson)
                .contentType("application/json")
                .when().post("/verify")
                .then()
                .statusCode(409);
    }
}
