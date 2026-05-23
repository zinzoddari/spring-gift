package gift.infra.kakao;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
class KakaoClient {
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    KakaoClient(final RestClient.Builder builder, final ObjectMapper objectMapper) {
        this.restClient = builder.build();
        this.objectMapper = objectMapper;
    }

    public <I, R> R post(final String uri, final I input, final TypeReference<R> returnTypeReference) {
        final String responseBody = restClient.post()
            .uri(uri)
            .body(input)
            .retrieve()
            .body(String.class);
        return deserialize(responseBody, returnTypeReference);
    }

    public <I, R> R post(final String uri, final String bearerToken, final I input, final TypeReference<R> returnTypeReference) {
        final String responseBody = restClient.post()
            .uri(uri)
            .header("Authorization", "Bearer " + bearerToken)
            .body(input)
            .retrieve()
            .body(String.class);
        return deserialize(responseBody, returnTypeReference);
    }

    public <I> void postVoid(final String uri, final String bearerToken, final I input) {
        restClient.post()
            .uri(uri)
            .header("Authorization", "Bearer " + bearerToken)
            .body(input)
            .retrieve()
            .toBodilessEntity();
    }

    public <R> R get(final String uri, final String bearerToken, final TypeReference<R> returnTypeReference) {
        final String responseBody = restClient.get()
            .uri(uri)
            .header("Authorization", "Bearer " + bearerToken)
            .retrieve()
            .body(String.class);
        return deserialize(responseBody, returnTypeReference);
    }

    private <R> R deserialize(final String body, final TypeReference<R> typeReference) {
        try {
            return objectMapper.readValue(body, typeReference);
        } catch (Exception e) {
            throw new RuntimeException("응답 역직렬화 실패: " + e.getMessage(), e);
        }
    }
}
