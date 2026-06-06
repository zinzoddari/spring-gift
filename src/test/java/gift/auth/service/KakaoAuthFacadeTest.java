package gift.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import gift.infra.jwt.JwtProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class KakaoAuthFacadeTest {

    @Mock
    private KakaoAuthService kakaoAuthService;

    @Mock
    private JwtProvider jwtProvider;

    @InjectMocks
    private KakaoAuthFacade kakaoAuthFacade;

    @Nested
    @DisplayName("카카오 로그인 URL을 요청할 때,")
    class LoginUrl {

        @Nested
        @DisplayName("성공하면,")
        class WhenSuccess {

            @Test
            @DisplayName("카카오 인증 URL을 반환한다.")
            void delegatesToService() {
                // given
                given(kakaoAuthService.loginUrl())
                    .willReturn("https://kauth.kakao.com/oauth/authorize?client_id=test");

                // when
                final String url = kakaoAuthFacade.loginUrl();

                // then
                assertThat(url).contains("https://kauth.kakao.com/oauth/authorize");
            }
        }
    }

    @Nested
    @DisplayName("카카오 인증 코드로 로그인할 때,")
    class Login {

        @Nested
        @DisplayName("성공하면,")
        class WhenSuccess {

            @Test
            @DisplayName("JWT 토큰을 발급한다.")
            void returnsJwt() {
                // given
                given(kakaoAuthService.login("auth-code"))
                    .willReturn("user@kakao.com");
                given(jwtProvider.createToken("user@kakao.com"))
                    .willReturn("service-jwt");

                // when
                final String jwt = kakaoAuthFacade.login("auth-code");

                // then
                assertThat(jwt).isEqualTo("service-jwt");
            }
        }
    }
}
