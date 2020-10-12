package ee.ria.eidasauthserver.view;

import ee.ria.eidasauthserver.session.AuthSession;
import ee.ria.eidasauthserver.session.AuthState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

@Validated
@RestController
@Slf4j
public class MockAuthController {

    @PostMapping(value = "/mockauth", produces = MediaType.APPLICATION_JSON_VALUE)
    public String mockAuth(HttpSession session) {
        AuthSession authSession = (AuthSession) session.getAttribute("session");
        authSession.setState(AuthState.AUTHENTICATION_SUCCESS);
        session.setAttribute("session", authSession);
        log.info("edited session " + session.getAttribute("session"));
        log.info("with id " + session.getId());
        return "done";
    }

    @PostMapping(value = "/createmocksession", produces = MediaType.APPLICATION_JSON_VALUE)
    public String createMockAuth(HttpSession session) {
        AuthSession newSession = new AuthSession();
        newSession.setLoginChallenge("abcdefg098AAdsCC");
        newSession.setState(AuthState.AUTHENTICATION_SUCCESS);
        newSession.setAcr("test_acr");
        newSession.setSubject("test_subject");
        session.setAttribute("session", newSession);
        return "done";
    }

}
