package ee.ria.eidasauthserver.view;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginAcceptResponseBody {

    @JsonProperty("redirect_to")
    String redirectUrl;
}
