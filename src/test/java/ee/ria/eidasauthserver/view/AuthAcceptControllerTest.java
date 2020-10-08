package ee.ria.eidasauthserver.view;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import ee.ria.eidasauthserver.session.AuthSession;
import ee.ria.eidasauthserver.session.AuthState;
import ee.ria.eidasauthserver.session.SessionRepository;
import io.restassured.RestAssured;
import io.restassured.filter.session.SessionFilter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.given;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Slf4j
public class AuthAcceptControllerTest {

    @MockBean
    SessionRepository sessionRepository;

    private static final String TEST_LOGIN_CHALLENGE = "abcdefg098AAdsCC";

    static {
        System.setProperty("javax.net.ssl.trustStore", "src/test/resources/tls-truststore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "changeit");
        System.setProperty("javax.net.ssl.trustStoreType", "jks");
    }

    protected WireMockServer mockOidcServer;

    @LocalServerPort
    protected int port;

    private String sessionId;
    SessionFilter sessionFilter = new SessionFilter();

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
    void authAccept_isSuccessful() {
        when(sessionRepository.getSession("sessionid123"))
                .thenReturn(new AuthSession(AuthState.AUTHENTICATION_SUCCESS, TEST_LOGIN_CHALLENGE, "test acr", "test subject"));

        mockOidcServer.start();
        mockOidcServer.stubFor(put(urlEqualTo("/oauth2/auth/requests/login/accept?login_challenge=" + TEST_LOGIN_CHALLENGE))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json; charset=UTF-8")
                        .withBodyFile("mock_responses/mockLoginAcceptResponse.json")));

        given()
                .cookie("SESSION", "sessionid123")
                .when()
                .get("/auth/accept")
                .then()
                .assertThat()
                .statusCode(200);

        mockOidcServer.stop();
    }

}
