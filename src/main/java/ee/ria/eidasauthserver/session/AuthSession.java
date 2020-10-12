package ee.ria.eidasauthserver.session;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthSession implements Serializable {
    private AuthState state;
    @JsonProperty("login_challenge")
    private String loginChallenge;
    @JsonProperty("acr")
    private String acr;
    @JsonProperty("subject")
    private String subject;
}
