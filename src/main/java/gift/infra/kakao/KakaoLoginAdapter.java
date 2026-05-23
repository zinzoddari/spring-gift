package gift.infra.kakao;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;

@Component
public class KakaoLoginAdapter {
    private final KakaoLoginProperties properties;
    private final KakaoClient authClient;
    private final KakaoClient apiClient;

    public KakaoLoginAdapter(
            final KakaoLoginProperties properties,
        @Qualifier("kakaoAuthClient") final KakaoClient authClient,
        @Qualifier("kakaoApiClient") final KakaoClient apiClient
    ) {
        this.properties = properties;
        this.authClient = authClient;
        this.apiClient = apiClient;
    }

    public KakaoTokenResponse requestAccessToken(String code) {
        LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", properties.clientId());
        params.add("redirect_uri", properties.redirectUri());
        params.add("code", code);
        params.add("client_secret", properties.clientSecret());

        return authClient.post(KakaoPath.OAUTH_TOKEN.path(), params, new ParameterizedTypeReference<>() {});
    }

    public KakaoUserResponse requestUserInfo(String accessToken) {
        return apiClient.get(KakaoPath.USER_ME.path(), accessToken, new ParameterizedTypeReference<>() {});
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
