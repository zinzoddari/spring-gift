package gift.infra.client.kakao;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

class KakaoClient {
    private final RestClient restClient;

    KakaoClient(final RestClient restClient) {
        this.restClient = restClient;
    }

    public <I, R> R post(final String uri, final I input, final ParameterizedTypeReference<R> responseType) {
        return restClient.post()
            .uri(uri)
            .body(input)
            .retrieve()
            .body(responseType);
    }

    public <I, R> R post(final String uri, final String bearerToken, final I input, final ParameterizedTypeReference<R> responseType) {
        return restClient.post()
            .uri(uri)
            .header("Authorization", "Bearer " + bearerToken)
            .body(input)
            .retrieve()
            .body(responseType);
    }

    public <I> void postVoid(final String uri, final String bearerToken, final I input) {
        restClient.post()
            .uri(uri)
            .header("Authorization", "Bearer " + bearerToken)
            .body(input)
            .retrieve()
            .toBodilessEntity();
    }

    public <R> R get(final String uri, final String bearerToken, final ParameterizedTypeReference<R> responseType) {
        return restClient.get()
            .uri(uri)
            .header("Authorization", "Bearer " + bearerToken)
            .retrieve()
            .body(responseType);
    }
}
