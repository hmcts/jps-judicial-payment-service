package uk.gov.hmcts.reform.jps.testUtil;

import com.fasterxml.jackson.annotation.JsonProperty;
import feign.Body;
import feign.Headers;
import feign.Param;
import feign.RequestLine;
import lombok.Data;

public interface IdamApi {

    @RequestLine("POST /oauth2/authorize")
    @Headers({"Authorization: {authorization}", "Content-Type: application/x-www-form-urlencoded"})
    @Body("response_type={response_type}&redirect_uri={redirect_uri}&client_id={client_id}")
    AuthenticateUserResponse authenticateUser(@Param("authorization") String authorization,
                                              @Param("response_type") String responseType,
                                              @Param("client_id") String clientId,
                                              @Param("redirect_uri") String redirectUri);

    @RequestLine("POST /oauth2/token")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    @Body("code={code}&grant_type={grant_type}&client_id={client_id}&client_secret={client_secret}"
        + "&redirect_uri={redirect_uri}")
    TokenExchangeResponse exchangeCode(@Param("code") String code,
                                       @Param("grant_type") String grantType,
                                       @Param("client_id") String clientId,
                                       @Param("client_secret") String clientSecret,
                                       @Param("redirect_uri") String redirectUri);

    @Data
    class AuthenticateUserResponse {
        @JsonProperty("code")
        private String code;

        public String getCode() {
            return code;
        }
    }

    @Data
    class TokenExchangeResponse {
        @JsonProperty("access_token")
        private String accessToken;

        public String getAccessToken() {
            return accessToken;
        }
    }
}
