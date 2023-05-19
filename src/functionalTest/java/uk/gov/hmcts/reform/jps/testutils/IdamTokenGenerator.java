package uk.gov.hmcts.reform.jps.testutils;

import feign.Feign;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.jps.config.PropertiesReader;

import java.util.Base64;

@Service
public class IdamTokenGenerator {

    PropertiesReader propertiesReader = new PropertiesReader("src/functionalTest/resources/test-config.properties");
    String idamApiUrl = propertiesReader.getProperty("idam.api.url");
    String clientId = propertiesReader.getProperty("client.id");
    String redirectUri = propertiesReader.getProperty("idam.redirect_uri");
    String clientSecret = propertiesReader.getProperty("idam.client.secret");

    private final IdamApi idamApi;

    @Autowired
    public IdamTokenGenerator() {
        idamApi = Feign.builder()
            .encoder(new JacksonEncoder())
            .decoder(new JacksonDecoder())
            .target(IdamApi.class, idamApiUrl);
    }

    public String authenticateUser(String username, String password) {
        String authorisation = username + ":" + password;
        String base64Authorisation = Base64.getEncoder().encodeToString(authorisation.getBytes());

        IdamApi.AuthenticateUserResponse authenticateUserResponse = idamApi.authenticateUser(
            "Basic " + base64Authorisation,
            "code",
            clientId,
            redirectUri
        );

        IdamApi.TokenExchangeResponse tokenExchangeResponse = idamApi.exchangeCode(
            authenticateUserResponse.getCode(),
            "authorization_code",
            clientId,
            clientSecret,
            redirectUri
        );

        return "Bearer " + tokenExchangeResponse.getAccessToken();
    }
}
