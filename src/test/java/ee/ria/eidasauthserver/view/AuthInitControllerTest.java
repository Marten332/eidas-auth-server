package ee.ria.eidasauthserver.view;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.context.WebApplicationContext;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
class AuthInitControllerTest {

    private static final String TEST_LOGIN_CHALLENGE = "abcdefg098AAdsCC";

    static {
        System.setProperty("javax.net.ssl.trustStore", "src/test/resources/tls-truststore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "changeit");
        System.setProperty("javax.net.ssl.trustStoreType", "jks");
    }

    protected static WireMockServer mockOidcServer;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @LocalServerPort
    protected int port;

    @BeforeAll
    static void setUpAll() {
        mockOidcServer = new WireMockServer(WireMockConfiguration.wireMockConfig()
                .httpDisabled(true)
                .httpsPort(9877)
                .keystorePath("src/test/resources/tls-keystore.jks")
                .keystorePassword("changeit")
                .notifier(new ConsoleNotifier(true))
        );
        mockOidcServer.start();
        configureRestAssured();
    }

    @AfterAll
    static void tearDownAll() {
        mockOidcServer.stop();
    }

    @BeforeEach
    void setUp() {
        RestAssuredMockMvc.webAppContextSetup(webApplicationContext);
        RestAssured.port = port;

    }

    @Test
    void authInit_EmptyParameter() {
        given()
                .param("loginChallenge", "")
                .when()
                .get("/auth/init")
                .then()
                .assertThat()
                .statusCode(400)
                .body("message", equalTo("Validation failed for object='requestParameters'. Error count: 1"))
                .body("error", equalTo("Bad Request"))
                .body("errors", equalTo("Parameter 'loginChallenge': value must not be null or empty"))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    void authInit_MissingParameter() {
        given()
                .when()
                .get("/auth/init")
                .then()
                .assertThat()
                .statusCode(400)
                .body("message", equalTo("Validation failed for object='requestParameters'. Error count: 1"))
                .body("error", equalTo("Bad Request"))
                .body("errors", equalTo("Parameter 'loginChallenge': value must not be null or empty"))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    void authInit_loginChallenge_InvalidValue() {
        given()
                .param("loginChallenge", "......")
                .when()
                .get("/auth/init")
                .then()
                .assertThat()
                .statusCode(400)
                .body("message", equalTo("Validation failed for object='requestParameters'. Error count: 1"))
                .body("error", equalTo("Bad Request"))
                .body("errors", equalTo("Parameter 'loginChallenge[0]': only characters and numbers allowed"))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    void authInit_loginChallenge_InvalidLength() {
        given()
                .param("loginChallenge", "123456789012345678901234567890123456789012345678900")
                .when()
                .get("/auth/init")
                .then()
                .assertThat()
                .statusCode(400)
                .body("message", equalTo("Validation failed for object='requestParameters'. Error count: 1"))
                .body("error", equalTo("Bad Request"))
                .body("errors", equalTo("Parameter 'loginChallenge[0]': size must be between 0 and 50"))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
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
                .statusCode(400)
                .body("message", equalTo("Validation failed for object='requestParameters'. Error count: 1"))
                .body("error", equalTo("Bad Request"))
                .body("errors", equalTo("Parameter 'loginChallenge': multiple instances not supported"))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    void authInit_loginChallenge() {
        mockOidcServer.stubFor(get(urlEqualTo("/oauth2/auth/requests/login?login_challenge=" + TEST_LOGIN_CHALLENGE))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json; charset=UTF-8")
                        .withBodyFile("mock_responses/mock_response.json")));

        given()
                .param("loginChallenge", TEST_LOGIN_CHALLENGE)
                .when()
                .get("/auth/init")
                .then()
                .assertThat()
                .statusCode(200)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML_VALUE +
                        ";charset=UTF-8").body("html.body.p", equalTo("Hello, nipitiri!"))
                .cookie("JSESSIONID", matchesPattern("[A-Z0-9]{32,32}"));
    }

    @Test
    void authInit_whenSessionExistsItIsReset() {
        mockOidcServer.stubFor(get(urlEqualTo("/oauth2/auth/requests/login?login_challenge=" + TEST_LOGIN_CHALLENGE))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json; charset=UTF-8")
                        .withBodyFile("mock_responses/mock_response.json")));

        String cookie = given()
                .param("loginChallenge", TEST_LOGIN_CHALLENGE)
                .when()
                .get("/auth/init")
                .then()
                .assertThat()
                .statusCode(200)
                .cookie("JSESSIONID", matchesPattern("[A-Z0-9]{32,32}"))
                .extract().cookie("JSESSIONID");

        given()
                .param("loginChallenge", TEST_LOGIN_CHALLENGE)
                .cookie("JSESSIONID", cookie)
                .when()
                .get("/auth/init")
                .then()
                .assertThat()
                .statusCode(200)
                .cookie("JSESSIONID", not(equalTo(cookie)));
    }

    protected static void configureRestAssured() {
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
    }

}
