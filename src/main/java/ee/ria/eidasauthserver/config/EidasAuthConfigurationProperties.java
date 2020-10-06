package ee.ria.eidasauthserver.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

@ConfigurationProperties(prefix = "tara")
@Validated
@Data
public class EidasAuthConfigurationProperties {

    @NotBlank
    String hydraServiceLoginUrl;
    @NotBlank
    String hydraServiceLoginAcceptUrl;
}
