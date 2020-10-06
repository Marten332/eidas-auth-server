package ee.ria.eidasauthserver.view;

import ee.ria.eidasauthserver.config.EidasAuthConfigurationProperties;
import ee.ria.eidasauthserver.error.BadRequestException;
import ee.ria.eidasauthserver.session.AuthSession;
import ee.ria.eidasauthserver.session.AuthenticationState;
import ee.ria.eidasauthserver.session.SessionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.constraints.NotNull;
import java.util.Map;

@Validated
@RestController
@Slf4j
class AuthAcceptController {

    @Autowired
    private EidasAuthConfigurationProperties eidasAuthConfigurationProperties;

    @Autowired
    private RestTemplate hydraService;

    @GetMapping("/auth/accept")
    public String authInit(@CookieValue("SESSION") HttpSession session) {
        AuthSession authSession = ((AuthSession) session.getAttribute("session"));

        if (authSession.getState() != AuthenticationState.AUTHENTICATION_SUCCESS)
            throw new BadRequestException("Session authentication state must be " + AuthenticationState.AUTHENTICATION_SUCCESS);

        String url = eidasAuthConfigurationProperties.getHydraServiceLoginAcceptUrl() + "?login_challenge=" + authSession.getLoginChallenge();
        hydraService.put(url, Map.class);
        return "hello";
    }
}
