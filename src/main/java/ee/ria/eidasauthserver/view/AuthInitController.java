package ee.ria.eidasauthserver.view;

import ee.ria.eidasauthserver.config.EidasAuthConfigurationProperties;
import ee.ria.eidasauthserver.session.AuthSession;
import ee.ria.eidasauthserver.session.AuthState;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpSession;
import javax.validation.constraints.*;
import java.util.List;
import java.util.Map;

@Validated
@Controller
@Slf4j
class AuthInitController {

    @Autowired
    private EidasAuthConfigurationProperties eidasAuthConfigurationProperties;

    @Autowired
    private RestTemplate hydraService;

    @GetMapping(value = "/auth/init", produces = MediaType.TEXT_HTML_VALUE)
    public String authInit(@Validated RequestParameters loginChallenge, HttpSession session, Model model) {

        if (session.getAttribute("session") != null) {
            session.removeAttribute("session");
            log.warn("session has been reset");
        }

        createSession(loginChallenge, session);

        String url = eidasAuthConfigurationProperties.getHydraServiceLoginUrl() + "?login_challenge=" + getStringParameterValue(loginChallenge.getLoginChallenge());
        Map<String, Object> response = hydraService.getForObject(url, Map.class);
        log.info("sample response: " + response);

        model.addAttribute("name", "nipitiri");
        return "hello";
    }

    private void createSession(RequestParameters loginChallenge, HttpSession session) {
        AuthSession newSession = new AuthSession();
        newSession.setLoginChallenge(getStringParameterValue(loginChallenge.getLoginChallenge()));
        newSession.setState(AuthState.INIT_AUTH_PROCESS);
        session.setAttribute("session", newSession);
    }

    private static String getStringParameterValue(List<String> param) {
        return param != null ? param.get(0) : null;
    }

    @Data
    public static class RequestParameters {
        @NotEmpty(message = "value must not be null or empty")
        @Size(max = 1, message = "multiple instances not supported")
        List<@NotBlank @Size(max = 50) @Pattern(regexp = "[A-Za-z0-9]{1,}", message = "only characters and numbers allowed") String> loginChallenge;
    }
}
