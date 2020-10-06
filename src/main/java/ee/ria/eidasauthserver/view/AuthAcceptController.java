package ee.ria.eidasauthserver.view;

import ee.ria.eidasauthserver.config.EidasAuthConfigurationProperties;
import ee.ria.eidasauthserver.error.BadRequestException;
import ee.ria.eidasauthserver.session.AuthSession;
import ee.ria.eidasauthserver.session.AuthenticationState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpSession;

@Validated
@RestController
@Slf4j
class AuthAcceptController {

    @Autowired
    private EidasAuthConfigurationProperties eidasAuthConfigurationProperties;

    @Autowired
    private RestTemplate hydraService;

    @GetMapping("/auth/accept")
    public String authAccept(HttpSession session) {
        AuthSession authSession = ((AuthSession) session.getAttribute("session"));

        if (authSession.getState() != AuthenticationState.AUTHENTICATION_SUCCESS)
            throw new BadRequestException("Session authentication state must be " + AuthenticationState.AUTHENTICATION_SUCCESS);

        HttpEntity<LoginAcceptBody> requestBody =
                new HttpEntity<>(new LoginAcceptBody(false, authSession.getAcr(), authSession.getSubject()));

        String url = eidasAuthConfigurationProperties.getHydraServiceLoginAcceptUrl() + "?login_challenge=" + authSession.getLoginChallenge();
        hydraService.put(url, requestBody);
        return "hello";
    }
}
