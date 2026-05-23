package gift.infra.client.kakao;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
class KakaoClientProvider {

    @Bean
    KakaoClient kakaoAuthClient(ObjectProvider<RestClient.Builder> builders, KakaoLoginProperties properties) {
        return new KakaoClient(builders.getObject().baseUrl(properties.authBaseUrl()).build());
    }

    @Bean
    KakaoClient kakaoApiClient(ObjectProvider<RestClient.Builder> builders, KakaoLoginProperties properties) {
        return new KakaoClient(builders.getObject().baseUrl(properties.apiBaseUrl()).build());
    }
}
