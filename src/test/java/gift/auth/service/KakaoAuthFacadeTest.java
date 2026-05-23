package gift.auth.service;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.BDDMockito.given;

import gift.auth.JwtProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("KakaoAuthFacade")
@ExtendWith(MockitoExtension.class)
class KakaoAuthFacadeTest {

    @Mock
    private KakaoAuthService kakaoAuthService;

    @Mock
    private JwtProvider jwtProvider;

    @InjectMocks
    private KakaoAuthFacade kakaoAuthFacade;

    @Nested
    @DisplayName("loginUrl()을 호출할 때,")
    class LoginUrl {

        @Nested
        @DisplayName("성공하면,")
        class WhenSuccess {

            @Test
            @DisplayName("서비스가 반환한 URL을 그대로 반환한다.")
            void delegatesToService() {
                // given
                given(kakaoAuthService.loginUrl())
                    .willReturn("https://kauth.kakao.com/oauth/authorize?client_id=test");

                // when
                final String url = kakaoAuthFacade.loginUrl();

                // then
                assertSoftly(softly ->
                    softly.assertThat(url).contains("https://kauth.kakao.com/oauth/authorize"));
            }
        }
    }

    @Nested
    @DisplayName("login()을 호출할 때,")
    class Login {

        @Nested
        @DisplayName("성공하면,")
        class WhenSuccess {

            @Test
            @DisplayName("서비스가 반환한 회원으로 JWT를 발급한다.")
            void returnsJwt() {
                // given
                given(kakaoAuthService.login("auth-code"))
                    .willReturn("user@kakao.com");
                given(jwtProvider.createToken("user@kakao.com"))
                    .willReturn("service-jwt");

                // when
                final String jwt = kakaoAuthFacade.login("auth-code");

                // then
                assertSoftly(softly -> softly.assertThat(jwt).isEqualTo("service-jwt"));
            }
        }
    }
}
