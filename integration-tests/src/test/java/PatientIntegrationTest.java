import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.core.IsNull.notNullValue;

public class PatientIntegrationTest {
    @BeforeAll
    static void setUp(){
        RestAssured.baseURI = "http://localhost:4004/";
    }

    private String getDevToken(String email) {
        return given()
                .contentType("application/json")
                .body("{\"email\":\"" + email + "\"}")
                .when()
                .post("/token")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .get("token");
    }

    @Test
    public void shouldReturnPatientWithValidToken(){
        String token = getDevToken("patient@test.com");

       given()
               .header("Authorization", "Bearer " + token)
               .when()
               .get("api/patients")
               .then()
               .statusCode(200)
               .body("patients", notNullValue());
    }

    @Test
    public void shouldRequestConsent(){
        String token = getDevToken("doctor@test.com");

        String consentRequestId = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body("""
                    {
                        "patientId": "PMS-005",
                        "doctorId": "DOC004",
                        "hospitalId": "HOSP-001"
                    }
                    """)
                .when()
                .post("api/consent/request")
                .then()
                .statusCode(201)
                .body("consentRequestId", notNullValue())
                .extract()
                .jsonPath()
                .get("consentRequestId");

        assert consentRequestId != null : "consentRequestId should not be null";
        assert consentRequestId.toString().startsWith("CSNT") : "consentRequestId should start with CSNT";
    }
}
