package at.ac.ase.inso.group02;

import at.ac.ase.inso.group02.authentication.dto.UserLoginDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.startsWith;

public abstract class LoginITHelper {
    public JsonMapper mapper = JsonMapper.builder().build();
    public String jwtToken;

    public void loginUser(String username) throws JsonProcessingException {
        UserLoginDTO userLogin = UserLoginDTO.builder()
                .emailOrUsername(username)
                .password("Password123!")
                .build();

        jwtToken = given()
                .body(mapper.writeValueAsString(userLogin))
                .contentType("application/json")
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .body("jwt", startsWith("ey"))
                .extract().response()
                .jsonPath().get("jwt");
    }
}
