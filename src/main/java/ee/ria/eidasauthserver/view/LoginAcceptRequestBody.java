package ee.ria.eidasauthserver.view;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;

@AllArgsConstructor
class LoginAcceptRequestBody {

    @JsonProperty("remember")
    boolean remember;
    @JsonProperty("acr")
    String acr;
    @JsonProperty("subject")
    String subject;
}
