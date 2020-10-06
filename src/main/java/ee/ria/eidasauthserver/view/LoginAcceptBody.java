package ee.ria.eidasauthserver.view;

import lombok.AllArgsConstructor;

@AllArgsConstructor
class LoginAcceptBody {

    boolean remember;
    String acr;
    String subject;
}
