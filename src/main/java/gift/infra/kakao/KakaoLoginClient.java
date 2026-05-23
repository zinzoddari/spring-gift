package gift.infra.kakao;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;

@Component
public class KakaoLoginClient {
    private final KakaoLoginProperties properties;
    private final KakaoClient kakaoClient;

    public KakaoLoginClient(final KakaoLoginProperties properties, final KakaoClient kakaoClient) {
        this.properties = properties;
        this.kakaoClient = kakaoClient;
    }

    public KakaoTokenResponse requestAccessToken(String code) {
        LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", properties.clientId());
        params.add("redirect_uri", properties.redirectUri());
        params.add("code", code);
        params.add("client_secret", properties.clientSecret());

        return kakaoClient.post(
            properties.authBaseUrl() + "/oauth/token",
            params,
            new TypeReference<>() {}
        );
    }

    public KakaoUserResponse requestUserInfo(String accessToken) {
        return kakaoClient.get(
            properties.apiBaseUrl() + "/v2/user/me",
            accessToken,
            new TypeReference<>() {}
        );
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record KakaoTokenResponse(@JsonProperty("access_token") String accessToken) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record KakaoUserResponse(@JsonProperty("kakao_account") KakaoAccount kakaoAccount) {

        public String email() {
            return kakaoAccount.email();
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public record KakaoAccount(String email) {
        }
    }
}
