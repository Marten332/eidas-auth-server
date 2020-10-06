package ee.ria.eidasauthserver.session;

public interface SessionRepository {

    AuthSession getSession(String sessionId);

    String postSession(AuthSession session);

    String deleteSession(String sessionId);
}
