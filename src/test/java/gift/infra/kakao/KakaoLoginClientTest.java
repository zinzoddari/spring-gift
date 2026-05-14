package gift.infra.kakao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.io.IOException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

@DisplayName("KakaoLoginClient")
class KakaoLoginClientTest {

    private MockWebServer mockWebServer;
    private KakaoLoginClient kakaoLoginClient;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        final String baseUrl = mockWebServer.url("/").toString();
        final KakaoLoginProperties properties = new KakaoLoginProperties(
            "test-client-id",
            "test-client-secret",
            "http://localhost/callback",
            baseUrl,
            baseUrl
        );

        kakaoLoginClient = new KakaoLoginClient(properties, RestClient.builder());
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Nested
    @DisplayName("액세스 토큰을 요청할 때,")
    class RequestAccessToken {

        @Nested
        @DisplayName("성공하면,")
        class WhenSuccess {

            @Test
            @DisplayName("응답에서 access_token을 파싱해 반환한다.")
            void returnsAccessToken() throws InterruptedException {
                // given
                mockWebServer.enqueue(new MockResponse()
                    .setBody("{\"access_token\": \"kakao-access-token\"}")
                    .addHeader("Content-Type", "application/json"));

                // when
                final KakaoLoginClient.KakaoTokenResponse response =
                    kakaoLoginClient.requestAccessToken("auth-code");

                // then
                final RecordedRequest request = mockWebServer.takeRequest();
                assertSoftly(softly -> {
                    softly.assertThat(response.accessToken()).isEqualTo("kakao-access-token");
                    softly.assertThat(request.getMethod()).isEqualTo("POST");
                    softly.assertThat(request.getPath()).isEqualTo("/oauth/token");
                    softly.assertThat(request.getBody().readUtf8()).contains("code=auth-code");
                });
            }
        }

        @Nested
        @DisplayName("실패하면,")
        class WhenFailed {

            @Test
            @DisplayName("401 응답이 오면 예외가 발생한다.")
            void throwsExceptionOn401() {
                // given
                mockWebServer.enqueue(new MockResponse().setResponseCode(401));

                // when & then
                assertThatThrownBy(() -> kakaoLoginClient.requestAccessToken("invalid-code"))
                    .isInstanceOf(Exception.class);
            }

            @Test
            @DisplayName("500 응답이 오면 예외가 발생한다.")
            void throwsExceptionOn500() {
                // given
                mockWebServer.enqueue(new MockResponse().setResponseCode(500));

                // when & then
                assertThatThrownBy(() -> kakaoLoginClient.requestAccessToken("code"))
                    .isInstanceOf(Exception.class);
            }
        }
    }

    @Nested
    @DisplayName("유저 정보를 요청할 때,")
    class RequestUserInfo {

        @Nested
        @DisplayName("성공하면,")
        class WhenSuccess {

            @Test
            @DisplayName("응답에서 이메일을 파싱해 반환한다.")
            void returnsEmail() throws InterruptedException {
                // given
                mockWebServer.enqueue(new MockResponse()
                    .setBody("{\"kakao_account\": {\"email\": \"test@kakao.com\"}}")
                    .addHeader("Content-Type", "application/json"));

                // when
                final KakaoLoginClient.KakaoUserResponse response =
                    kakaoLoginClient.requestUserInfo("access-token");

                // then
                final RecordedRequest request = mockWebServer.takeRequest();
                assertSoftly(softly -> {
                    softly.assertThat(response.email()).isEqualTo("test@kakao.com");
                    softly.assertThat(request.getMethod()).isEqualTo("GET");
                    softly.assertThat(request.getPath()).isEqualTo("/v2/user/me");
                    softly.assertThat(request.getHeader("Authorization"))
                        .isEqualTo("Bearer access-token");
                });
            }

            @Test
            @DisplayName("응답에 알 수 없는 필드가 있어도 이메일을 정상적으로 파싱한다.")
            void ignoresUnknownFields() {
                // given
                mockWebServer.enqueue(new MockResponse()
                    .setBody("{\"id\": 12345, \"kakao_account\": {\"email\": \"test@kakao.com\", \"is_email_valid\": true}}")
                    .addHeader("Content-Type", "application/json"));

                // when
                final KakaoLoginClient.KakaoUserResponse response =
                    kakaoLoginClient.requestUserInfo("access-token");

                // then
                assertThat(response.email()).isEqualTo("test@kakao.com");
            }
        }

        @Nested
        @DisplayName("실패하면,")
        class WhenFailed {

            @Test
            @DisplayName("401 응답이 오면 예외가 발생한다.")
            void throwsExceptionOn401() {
                // given
                mockWebServer.enqueue(new MockResponse().setResponseCode(401));

                // when & then
                assertThatThrownBy(() -> kakaoLoginClient.requestUserInfo("invalid-token"))
                    .isInstanceOf(Exception.class);
            }
        }
    }
}
