package ee.ria.eidasauthserver.view;

import ee.ria.eidasauthserver.config.EidasAuthConfigurationProperties;
import ee.ria.eidasauthserver.session.AuthSession;
import ee.ria.eidasauthserver.session.AuthenticationState;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpSession;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Map;

@Validated
@RestController
@Slf4j
class AuthInitController {

    @Autowired
    private EidasAuthConfigurationProperties eidasAuthConfigurationProperties;

    @Autowired
    private RestTemplate hydraService;

    @GetMapping("/auth/init")
    public String authInit(@Validated RequestParameters loginChallenge, HttpSession session) {

        if (session.getAttribute("session") != null) {
            session.removeAttribute("session");
            log.warn("session has been reset");
        }

        createSession(loginChallenge, session);

        String url = eidasAuthConfigurationProperties.getHydraServiceLoginUrl() + "?login_challenge=" + getStringParameterValue(loginChallenge.getLoginChallenge());
        Map<String, Object> response = hydraService.getForObject(url, Map.class);
        log.info("sample response: " + response);

        return "hello";
    }

    private void createSession(RequestParameters loginChallenge, HttpSession session) {
        AuthSession newSession = new AuthSession();
        newSession.setLoginChallenge(getStringParameterValue(loginChallenge.getLoginChallenge()));
        newSession.setState(AuthenticationState.INIT_AUTH_PROCESS);
        session.setAttribute("session", newSession);
    }

    private static String getStringParameterValue(List<String> param) {
        return param != null ? param.get(0) : null;
    }

    @Data
    public static class RequestParameters {
        @NotNull
        @NotEmpty
        @Size(max = 1, message = "multiple instances not supported")
        List<@Size(max = 50) @Pattern(regexp = "[A-Za-z0-9]{1,}", message = "only characters and numbers allowed") String> loginChallenge;
    }
}
