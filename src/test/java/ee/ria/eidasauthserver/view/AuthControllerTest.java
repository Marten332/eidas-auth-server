package ee.ria.eidasauthserver.view;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;

import static io.restassured.RestAssured.given;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest( webEnvironment = RANDOM_PORT )
class AuthControllerTest {

    protected WireMockServer mockOidcServer;

    @LocalServerPort
    protected int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        mockOidcServer = new WireMockServer(WireMockConfiguration.wireMockConfig()
                .httpDisabled(true)
                .httpsPort(9877)
                .keystorePath("src/test/resources/tls-keystore.jks")
                .keystorePassword("changeit")
        );
    }

    @Test
    void authInit_NoInput() {
        given()
            .param("invalidParameter", "")
        .when()
            .get("/auth/init")
        .then()
            .assertThat()
            .statusCode(400);
    }

    @Test
    void authInit_loginChallenge_InvalidValue() {
        given()
            .param("login_challenge", "......")
        .when()
            .get("/auth/init")
        .then()
            .assertThat()
            .statusCode(400);
    }

    @Test
    void authInit_loginChallenge_InvalidLength() {
        given()
            .param("login_challenge", "123456789012345678901234567890123456789012345678900")
        .when()
            .get("/auth/init")
        .then()
            .assertThat()
            .statusCode(400);
    }

    @Test
    void authInit_loginChallenge_DuplicatedParam() {
        given()
            .param("login_challenge", "abcdefg098AAdsCC")
            .param("login_challenge", "abcdefg098AAdsCCasassa")
        .when()
            .get("/auth/init")
        .then()
            .assertThat()
            .statusCode(400);
    }

    // TODO multiple parameters with same name not allowed test

    @Test
    void authInit_loginChallenge() {
        mockOidcServer.start();
        mockOidcServer.stubFor(get(urlEqualTo("/oauth2/auth/requests/login"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json; charset=UTF-8")
                        .withBodyFile("mock_responses/mock_response.json")));

        given()
            .param("login_challenge", "abcdefg098AAdsCC")
        .when()
            .get("/auth/init")
        .then()
            .assertThat()
            .statusCode(200);

        // TODO assert correct content-type
    }

}
