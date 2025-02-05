package at.ac.ase.inso.group02.authentication;

import at.ac.ase.inso.group02.authentication.dto.UserDetailDTO;
import at.ac.ase.inso.group02.authentication.dto.UserLocationDTO;
import at.ac.ase.inso.group02.authentication.dto.UserUpdateDTO;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestHTTPEndpoint(UserController.class)
class UserResourceTest {

    JsonMapper jsonMapper = JsonMapper.builder().build();

    @InjectMock
    UserService userServiceMock;

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
    void testGetUserByUsernameEndpoint_shouldReturnUser() {
        // Arrange
        when(userServiceMock.getUserByUsername("username")).thenReturn(sampleUser);

        // Act & Assert
        given()
                .pathParam("username", "username")
                .when()
                .get("/{username}")
                .then()
                .statusCode(200)
                .body("username", equalTo("username"))
                .body("displayName", equalTo("My Name"))
                .body("createdAt", is(now.format(DateTimeFormatter.ofPattern(dateFormat))));
    }

    @Test
    void testGetUserByUsername_shouldFailForInvalidUsername() {
        // Arrange
        when(userServiceMock.getUserByUsername("nonexistent")).thenThrow(new NotFoundException("User not found"));

        // Act & Assert
        given()
                .pathParam("username", "nonexistent")
                .when()
                .get("/{username}")
                .then()
                .statusCode(404);
    }

    @Test
    void testUpdateUserEndpoint_shouldFailWithoutPermission() {
        // Arrange
        UserDetailDTO updateData = UserDetailDTO.builder()
                .email("newemail@example.com")
                .build();

        UserDetailDTO updatedUser = UserDetailDTO.builder()
                .email("newemail@example.com")
                .username("username")
                .displayName("Updated Name")
                .bio("Updated Bio")
                .profilePicture("http://example.com/new-pic.jpg")
                .location(UserLocationDTO.builder()
                        .build())
                .createdAt(now)
                .build();

        when(userServiceMock.updateUser(any(UserUpdateDTO.class))).thenReturn(updatedUser);

        // Act & Assert
        given()
                .body(updateData) // Use updateData as the JSON body
                .contentType("application/json")
                .when()
                .put()
                .then()
                .statusCode(401);
    }

    @Test
    void testDeleteUserEndpoint_shouldFailWithoutPermission() {
        // Arrange
        Mockito.doNothing().when(userServiceMock).deleteUser();

        // Act & Assert
        given()
                .queryParam("username", "username")
                .when()
                .delete()
                .then()
                .statusCode(401);
    }
}
