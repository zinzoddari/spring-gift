package gift.infra.kakao;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@DisplayName("KakaoClient")
class KakaoClientTest {

    private MockWebServer mockWebServer;
    private KakaoClient kakaoClient;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        kakaoClient = new KakaoClient(RestClient.builder(), new ObjectMapper());
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    record TestResponse(String value) {
    }

    @Nested
    @DisplayName("post()를 호출할 때,")
    class Post {

        @Nested
        @DisplayName("인증 없이 성공하면,")
        class WhenSuccessWithoutAuth {

            @Test
            @DisplayName("응답 바디를 역직렬화해 반환한다.")
            void returnsDeserializedBody() throws InterruptedException {
                // given
                mockWebServer.enqueue(new MockResponse()
                    .setBody("{\"value\": \"hello\"}")
                    .addHeader("Content-Type", "application/json"));

                final String uri = mockWebServer.url("/oauth/token").toString();
                final MultiValueMap<String, String> input = new LinkedMultiValueMap<>();
                input.add("grant_type", "authorization_code");
                input.add("code", "auth-code");

                // when
                final TestResponse response = kakaoClient.post(uri, input, new TypeReference<>() {});

                // then
                final RecordedRequest request = mockWebServer.takeRequest();
                assertSoftly(softly -> {
                    softly.assertThat(response.value()).isEqualTo("hello");
                    softly.assertThat(request.getMethod()).isEqualTo("POST");
                    softly.assertThat(request.getPath()).isEqualTo("/oauth/token");
                    softly.assertThat(request.getBody().readUtf8()).contains("code=auth-code");
                });
            }
        }

        @Nested
        @DisplayName("인증 포함 성공하면,")
        class WhenSuccessWithAuth {

            @Test
            @DisplayName("Authorization 헤더를 포함해 응답 바디를 반환한다.")
            void returnsDeserializedBodyWithAuth() throws InterruptedException {
                // given
                mockWebServer.enqueue(new MockResponse()
                    .setBody("{\"value\": \"hello\"}")
                    .addHeader("Content-Type", "application/json"));

                final String uri = mockWebServer.url("/test/path").toString();
                final MultiValueMap<String, String> input = new LinkedMultiValueMap<>();
                input.add("key", "val");

                // when
                final TestResponse response = kakaoClient.post(uri, "token123", input, new TypeReference<>() {});

                // then
                final RecordedRequest request = mockWebServer.takeRequest();
                assertSoftly(softly -> {
                    softly.assertThat(response.value()).isEqualTo("hello");
                    softly.assertThat(request.getHeader("Authorization")).isEqualTo("Bearer token123");
                });
            }
        }

        @Nested
        @DisplayName("실패하면,")
        class WhenFailed {

            @Test
            @DisplayName("4xx 응답이 오면 예외가 발생한다.")
            void throwsExceptionOn4xx() {
                // given
                mockWebServer.enqueue(new MockResponse().setResponseCode(400));
                final String uri = mockWebServer.url("/test/path").toString();

                // when & then
                assertThatThrownBy(() -> kakaoClient.post(uri, new LinkedMultiValueMap<>(), new TypeReference<TestResponse>() {}))
                    .isInstanceOf(Exception.class);
            }

            @Test
            @DisplayName("5xx 응답이 오면 예외가 발생한다.")
            void throwsExceptionOn5xx() {
                // given
                mockWebServer.enqueue(new MockResponse().setResponseCode(500));
                final String uri = mockWebServer.url("/test/path").toString();

                // when & then
                assertThatThrownBy(() -> kakaoClient.post(uri, new LinkedMultiValueMap<>(), new TypeReference<TestResponse>() {}))
                    .isInstanceOf(Exception.class);
            }
        }
    }

    @Nested
    @DisplayName("postVoid()를 호출할 때,")
    class PostVoid {

        @Nested
        @DisplayName("성공하면,")
        class WhenSuccess {

            @Test
            @DisplayName("Authorization 헤더를 포함해 요청을 전송한다.")
            void sendsRequestWithAuth() throws InterruptedException {
                // given
                mockWebServer.enqueue(new MockResponse().setResponseCode(200));

                final String uri = mockWebServer.url("/talk/memo/send").toString();
                final MultiValueMap<String, String> input = new LinkedMultiValueMap<>();
                input.add("template_object", "{}");

                // when
                kakaoClient.postVoid(uri, "token123", input);

                // then
                final RecordedRequest request = mockWebServer.takeRequest();
                assertSoftly(softly -> {
                    softly.assertThat(request.getMethod()).isEqualTo("POST");
                    softly.assertThat(request.getPath()).isEqualTo("/talk/memo/send");
                    softly.assertThat(request.getHeader("Authorization")).isEqualTo("Bearer token123");
                });
            }
        }

        @Nested
        @DisplayName("실패하면,")
        class WhenFailed {

            @Test
            @DisplayName("4xx 응답이 오면 예외가 발생한다.")
            void throwsExceptionOn4xx() {
                // given
                mockWebServer.enqueue(new MockResponse().setResponseCode(401));
                final String uri = mockWebServer.url("/talk/memo/send").toString();

                // when & then
                assertThatThrownBy(() -> kakaoClient.postVoid(uri, "bad-token", new LinkedMultiValueMap<>()))
                    .isInstanceOf(Exception.class);
            }

            @Test
            @DisplayName("5xx 응답이 오면 예외가 발생한다.")
            void throwsExceptionOn5xx() {
                // given
                mockWebServer.enqueue(new MockResponse().setResponseCode(500));
                final String uri = mockWebServer.url("/talk/memo/send").toString();

                // when & then
                assertThatThrownBy(() -> kakaoClient.postVoid(uri, "token", new LinkedMultiValueMap<>()))
                    .isInstanceOf(Exception.class);
            }
        }
    }

    @Nested
    @DisplayName("get()을 호출할 때,")
    class Get {

        @Nested
        @DisplayName("성공하면,")
        class WhenSuccess {

            @Test
            @DisplayName("Authorization 헤더를 포함해 응답 바디를 반환한다.")
            void returnsDeserializedBody() throws InterruptedException {
                // given
                mockWebServer.enqueue(new MockResponse()
                    .setBody("{\"value\": \"world\"}")
                    .addHeader("Content-Type", "application/json"));

                final String uri = mockWebServer.url("/v2/user/me").toString();

                // when
                final TestResponse response = kakaoClient.get(uri, "token456", new TypeReference<>() {});

                // then
                final RecordedRequest request = mockWebServer.takeRequest();
                assertSoftly(softly -> {
                    softly.assertThat(response.value()).isEqualTo("world");
                    softly.assertThat(request.getMethod()).isEqualTo("GET");
                    softly.assertThat(request.getPath()).isEqualTo("/v2/user/me");
                    softly.assertThat(request.getHeader("Authorization")).isEqualTo("Bearer token456");
                });
            }
        }

        @Nested
        @DisplayName("실패하면,")
        class WhenFailed {

            @Test
            @DisplayName("4xx 응답이 오면 예외가 발생한다.")
            void throwsExceptionOn4xx() {
                // given
                mockWebServer.enqueue(new MockResponse().setResponseCode(401));
                final String uri = mockWebServer.url("/v2/user/me").toString();

                // when & then
                assertThatThrownBy(() -> kakaoClient.get(uri, "bad-token", new TypeReference<TestResponse>() {}))
                    .isInstanceOf(Exception.class);
            }
        }
    }
}
