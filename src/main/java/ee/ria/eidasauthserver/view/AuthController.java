package ee.ria.eidasauthserver.view;

import ee.ria.eidasauthserver.config.EidasAuthConfigurationProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Map;

@Slf4j
@Validated
@RestController
class AuthController {

    @Autowired
    private EidasAuthConfigurationProperties eidasAuthConfigurationProperties;

    @Autowired
    private RestTemplate hydraService;


    @GetMapping("/auth/init")
    public String authInit(@RequestParam(name = "login_challenge") @Size(max = 50) @Pattern(regexp = "[A-Za-z0-9]{1,}", message = "only characters and numbers allowed") String loginChallenge) {

        String url = eidasAuthConfigurationProperties.getHydraServiceLoginUrl() + "?login_challenge=" + loginChallenge;

        Map<String, Object> response = hydraService.getForObject(url, Map.class);

        log.info("sample response: " + response);
        return "Hello world!";
    }
}
