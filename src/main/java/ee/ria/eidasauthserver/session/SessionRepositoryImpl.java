package ee.ria.eidasauthserver.session;

import org.springframework.stereotype.Component;

@Component
class SessionRepositoryImpl implements SessionRepository {
    @Override
    public AuthSession getSession(String sessionId) {
        return null;
    }

    @Override
    public String postSession(AuthSession session) {
        return null;
    }

    @Override
    public String deleteSession(String sessionId) {
        return null;
    }
}
