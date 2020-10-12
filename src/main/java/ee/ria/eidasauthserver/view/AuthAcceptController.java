package ee.ria.eidasauthserver.view;

import com.fasterxml.jackson.annotation.JsonProperty;
import ee.ria.eidasauthserver.config.EidasAuthConfigurationProperties;
import ee.ria.eidasauthserver.error.BadRequestException;
import ee.ria.eidasauthserver.session.AuthSession;
import ee.ria.eidasauthserver.session.AuthState;
import ee.ria.eidasauthserver.session.SessionRepository;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
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
    public ResponseEntity<String> authAccept(HttpSession session, HttpServletResponse res) {

        log.info("has session " + session.getAttribute("session"));
        log.info("with id " + session.getId());

        AuthSession authSession = (AuthSession) session.getAttribute("session");

        if (authSession.getState() != AuthState.AUTHENTICATION_SUCCESS)
            throw new BadRequestException("Authentication state must be " + AuthState.AUTHENTICATION_SUCCESS);

        log.info("login challenge to be accepted: " + authSession.getLoginChallenge());

        String url = eidasAuthConfigurationProperties.getHydraServiceLoginAcceptUrl() + "?login_challenge=" + authSession.getLoginChallenge();
        ResponseEntity<LoginAcceptResponseBody> response = hydraService.exchange(url, HttpMethod.PUT, createRequestBody(authSession), LoginAcceptResponseBody.class);
        log.info("response entity: " + response);
        if (response.getStatusCode() == HttpStatus.OK && response.getBody().getRedirectUrl() != null) {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Location", response.getBody().getRedirectUrl());
            return new ResponseEntity<>(headers, HttpStatus.FOUND);
        } else {
            throw new RuntimeException();
        }
    }

    private HttpEntity<LoginAcceptRequestBody> createRequestBody(AuthSession authSession) {
        return new HttpEntity<>(new LoginAcceptRequestBody(false, authSession.getAcr(), authSession.getSubject()));
    }

    @AllArgsConstructor
    static class LoginAcceptRequestBody {

        @JsonProperty("remember")
        boolean remember;
        @JsonProperty("acr")
        String acr;
        @JsonProperty("subject")
        String subject;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    static class LoginAcceptResponseBody {

        @JsonProperty("redirect_to")
        String redirectUrl;
    }
}
