package at.ac.ase.inso.group02.skills;

import at.ac.ase.inso.group02.LoginITHelper;
import at.ac.ase.inso.group02.skills.dto.SkillCategoryDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URL;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.hasSize;

@QuarkusTest
public class SkillCategoryResourceIT extends LoginITHelper {
    @TestHTTPEndpoint(SkillCategoryController.class)
    @TestHTTPResource
    URL url;

    SkillCategoryDTO category1, category2;

    /**
     * login user before test as for most cases, user role is required
     *
     * @throws JsonProcessingException
     */
    @BeforeEach
    void setup() throws JsonProcessingException {
        loginUser("user");

        category1 = SkillCategoryDTO.builder()
                .id(-1L)
                .name("Gardening")
                .description("Whatever happens outside in the garden!")
                .build();
        category2 = SkillCategoryDTO.builder()
                .id(-2L)
                .name("Cooking")
                .description("The art of preparing and making food.")
                .build();
    }

    @Test
    void testGetAllCategories_shouldFindAllThreeCategories() throws JsonProcessingException {
        given()
                .header("Authorization", "Bearer " + jwtToken)
                .when()
                .get(url)
                .then()
                .statusCode(200)
                .body("", hasSize(2))
                .body(containsString(mapper.writeValueAsString(category1)))
                .body(containsString(mapper.writeValueAsString(category2)));
    }

    @Test
    void testGetAllCategoriesWithQuery_shouldFindOneCategory() throws JsonProcessingException {
        given()
                .header("Authorization", "Bearer " + jwtToken)
                .when()
                .get(url + "?q=co")
                .then()
                .statusCode(200)
                .body("", hasSize(1))
                .body(containsString(mapper.writeValueAsString(category2)));
    }

    @Test
    void testGetAllCategoriesWithNoMatchQuery_shouldReturnEmptyArray() {
        given()
                .header("Authorization", "Bearer " + jwtToken)
                .when()
                .get(url + "?q=aaaaaa")
                .then()
                .statusCode(200)
                .body("", hasSize(0));
    }

    @Test
    void testGetAllCategoriesWithNoToken_shouldFailWith401() {
        given()
                .when()
                .get(url)
                .then()
                .statusCode(401);
    }

    @Test
    void testGetCategoryWithId_shouldFindCategory() throws JsonProcessingException {
        given()
                .header("Authorization", "Bearer " + jwtToken)
                .when()
                .get(url + "/-1")
                .then()
                .statusCode(200)
                .body(containsString(mapper.writeValueAsString(category1)));
    }

    @Test
    void testGetCategoryWithNonExistentId_shouldProduce404NotFound() throws JsonProcessingException {
        given()
                .header("Authorization", "Bearer " + jwtToken)
                .when()
                .get(url + "/-1000")
                .then()
                .statusCode(404)
                .body(containsString("Skill category with id -1000 not found"));
    }
}
