package ee.ria.eidasauthserver.view;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.given;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
class AuthInitControllerTest {

    private static final String TEST_LOGIN_CHALLENGE = "abcdefg098AAdsCC";

    static {
        System.setProperty("javax.net.ssl.trustStore", "src/test/resources/tls-truststore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "changeit");
        System.setProperty("javax.net.ssl.trustStoreType", "jks");
    }

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
                .notifier(new ConsoleNotifier(true))
        );
    }

    @Test
    void authInit_NoInput() {
        given()
                .param("loginChallenge", "")
                .when()
                .get("/auth/init")
                .then()
                .assertThat()
                .statusCode(400);
    }

    @Test
    void authInit_loginChallenge_InvalidValue() {
        given()
                .param("loginChallenge", "......")
                .when()
                .get("/auth/init")
                .then()
                .assertThat()
                .statusCode(400);
    }

    @Test
    void authInit_loginChallenge_InvalidLength() {
        given()
                .param("loginChallenge", "123456789012345678901234567890123456789012345678900")
                .when()
                .get("/auth/init")
                .then()
                .assertThat()
                .statusCode(400);
    }

    @Test
    void authInit_loginChallenge_DuplicatedParam() {
        given()
                .param("loginChallenge", TEST_LOGIN_CHALLENGE)
                .param("loginChallenge", "abcdefg098AAdsCCasassa")
                .when()
                .get("/auth/init")
                .then()
                .assertThat()
                .statusCode(400);
    }

    @Test
    void authInit_loginChallenge() {
        mockOidcServer.start();
        mockOidcServer.stubFor(get(urlEqualTo("/oauth2/auth/requests/login?login_challenge=" + TEST_LOGIN_CHALLENGE))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json; charset=UTF-8")
                        .withBodyFile("mock_responses/mock_response.json")));

        String testCookie = given()
                .param("loginChallenge", TEST_LOGIN_CHALLENGE)
                .when()
                .get("/auth/init")
                .then()
                .assertThat()
                .statusCode(200)
                .extract().cookie("JSESSIONID");

        given()
                .param("loginChallenge", TEST_LOGIN_CHALLENGE)
                .cookie("JSESSIONID", testCookie)
                .when()
                .get("/auth/init")
                .then()
                .assertThat()
                .statusCode(200)
                .assertThat().cookie("JSESSIONID");

        mockOidcServer.stop();
        // TODO assert correct content-type
    }


}
