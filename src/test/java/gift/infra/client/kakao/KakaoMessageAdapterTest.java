package gift.infra.client.kakao;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

class KakaoMessageAdapterTest {

    private MockWebServer mockWebServer;
    private KakaoMessageAdapter kakaoMessageClient;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        final String baseUrl = mockWebServer.url("/").toString();
        final KakaoClient kakaoClient = new KakaoClient(RestClient.builder().baseUrl(baseUrl).build());

        kakaoMessageClient = new KakaoMessageAdapter(kakaoClient);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Nested
    @DisplayName("카카오 메시지를 전송할 때,")
    class SendToMe {

        @Nested
        @DisplayName("성공하면,")
        class WhenSuccess {

            @Test
            @DisplayName("메시지가 있으면, 메시지를 포함해 올바른 요청을 전송한다.")
            void sendsRequestWithMessage() throws InterruptedException {
                // given
                mockWebServer.enqueue(new MockResponse().setResponseCode(200));

                // when
                kakaoMessageClient.sendToMe("access-token", "카카오 선물세트", 10_000, "기본 옵션", 2, "생일 축하해!");

                // then
                final RecordedRequest request = mockWebServer.takeRequest();
                final String body = URLDecoder.decode(request.getBody().readUtf8(), StandardCharsets.UTF_8);
                assertSoftly(softly -> {
                    softly.assertThat(request.getMethod()).isEqualTo("POST");
                    softly.assertThat(request.getPath()).isEqualTo("/v2/api/talk/memo/default/send");
                    softly.assertThat(request.getHeader("Authorization")).isEqualTo("Bearer access-token");
                    softly.assertThat(body).contains("template_object");
                    softly.assertThat(body).contains("카카오 선물세트");
                    softly.assertThat(body).contains("기본 옵션");
                    softly.assertThat(body).contains("생일 축하해!");
                });
            }

            @Test
            @DisplayName("메시지가 없으면, 메시지 없이 올바른 요청을 전송한다.")
            void sendsRequestWithoutMessage() throws InterruptedException {
                // given
                mockWebServer.enqueue(new MockResponse().setResponseCode(200));

                // when
                kakaoMessageClient.sendToMe("access-token", "카카오 선물세트", 10_000, "기본 옵션", 2, null);

                // then
                final RecordedRequest request = mockWebServer.takeRequest();
                final String body = URLDecoder.decode(request.getBody().readUtf8(), StandardCharsets.UTF_8);
                assertSoftly(softly -> {
                    softly.assertThat(request.getMethod()).isEqualTo("POST");
                    softly.assertThat(body).contains("template_object");
                    softly.assertThat(body).doesNotContain("💌");
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
                assertThatThrownBy(() ->
                    kakaoMessageClient.sendToMe("invalid-token", "카카오 선물세트", 10_000, "기본 옵션", 1, null))
                    .isInstanceOf(Exception.class);
            }

            @Test
            @DisplayName("500 응답이 오면 예외가 발생한다.")
            void throwsExceptionOn500() {
                // given
                mockWebServer.enqueue(new MockResponse().setResponseCode(500));

                // when & then
                assertThatThrownBy(() ->
                    kakaoMessageClient.sendToMe("token", "카카오 선물세트", 10_000, "기본 옵션", 1, null))
                    .isInstanceOf(Exception.class);
            }
        }
    }
}
