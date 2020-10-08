package ee.ria.eidasauthserver.view;

import ee.ria.eidasauthserver.config.EidasAuthConfigurationProperties;
import ee.ria.eidasauthserver.error.BadRequestException;
import ee.ria.eidasauthserver.session.AuthSession;
import ee.ria.eidasauthserver.session.AuthState;
import ee.ria.eidasauthserver.session.SessionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@Validated
@RestController
@Slf4j
class AuthAcceptController {

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private EidasAuthConfigurationProperties eidasAuthConfigurationProperties;

    @Autowired
    private RestTemplate hydraService;

    @GetMapping("/auth/accept")
    public ResponseEntity<String> authAccept(@CookieValue(value = "SESSION") String sessionId) {
        AuthSession session = sessionRepository.getSession(sessionId);

        if (session.getState() != AuthState.AUTHENTICATION_SUCCESS)
            throw new BadRequestException("Authentication state must be " + AuthState.AUTHENTICATION_SUCCESS);

        String url = eidasAuthConfigurationProperties.getHydraServiceLoginAcceptUrl() + "?login_challenge=" + session.getLoginChallenge();
        ResponseEntity<LoginAcceptResponseBody> response = hydraService.exchange(url, HttpMethod.PUT, createRequestBody(session), LoginAcceptResponseBody.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody().getRedirectUrl() != null) {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Location", response.getBody().getRedirectUrl());
            return new ResponseEntity<>(headers, HttpStatus.FOUND);
        } else {
            return null;
        }
    }

    private HttpEntity<LoginAcceptRequestBody> createRequestBody(AuthSession authSession) {
        return new HttpEntity<>(new LoginAcceptRequestBody(false, authSession.getAcr(), authSession.getSubject()));
    }
}
