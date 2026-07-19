import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.core.IsNull.notNullValue;


public class AuthIntegrationTests {
    @BeforeAll
    static void beforeAll() {
        RestAssured.baseURI = "http://localhost:4004";
    }

    @Test
    public void shouldReturnOkWithValidToken() {
        Response response = given()
                .contentType("application/json")
                .body("{\"email\":\"doctor@test.com\"}")
                .when()
                .post("/token")
                .then()
                .statusCode(200)
                .body("token", notNullValue())
                .extract()
                .response();

        System.out.println("Generated token: " + response.jsonPath().getString("token"));
    }

    @Test
    public void shouldReturnUnauthorizedOnInvalidToken() {
        given()
                .contentType("application/json")
                .body("{\"email\":\"nonexistent@test.com\"}")
                .when()
                .post("/token")
                .then()
                .statusCode(401);
    }
}
