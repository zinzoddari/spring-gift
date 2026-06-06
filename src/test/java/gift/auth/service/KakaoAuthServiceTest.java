package gift.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import gift.member.domain.Member;

import gift.infra.client.kakao.KakaoLoginAdapter;
import gift.infra.client.kakao.KakaoLoginProperties;
import gift.infra.jwt.JwtProvider;
import gift.member.repository.MemberRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class KakaoAuthServiceTest {

    @Mock
    private KakaoLoginProperties properties;

    @Mock
    private KakaoLoginAdapter kakaoLoginAdapter;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private JwtProvider jwtProvider;

    @InjectMocks
    private KakaoAuthService kakaoAuthService;

    @Nested
    @DisplayName("카카오 로그인 URL을 요청할 때,")
    class LoginUrl {

        @Nested
        @DisplayName("성공하면,")
        class WhenSuccess {

            @Test
            @DisplayName("카카오 인증 URL을 반환한다.")
            void returnsKakaoAuthUrl() {
                // given
                given(properties.authBaseUrl()).willReturn("https://kauth.kakao.com");
                given(properties.clientId()).willReturn("test-client-id");
                given(properties.redirectUri()).willReturn("http://localhost/callback");

                // when
                final String url = kakaoAuthService.loginUrl();

                // then
                assertThat(url).contains("https://kauth.kakao.com/oauth/authorize");
                assertThat(url).contains("client_id=test-client-id");
                assertThat(url).contains("redirect_uri=");
            }
        }
    }

    @Nested
    @DisplayName("카카오 인증 코드로 로그인할 때,")
    class Login {

        @BeforeEach
        void setUp() {
            given(kakaoLoginAdapter.requestAccessToken("auth-code"))
                .willReturn(new KakaoLoginAdapter.KakaoTokenResponse("kakao-token"));
            given(kakaoLoginAdapter.requestUserInfo("kakao-token"))
                .willReturn(new KakaoLoginAdapter.KakaoUserResponse(
                    new KakaoLoginAdapter.KakaoUserResponse.KakaoAccount("user@kakao.com")));
            given(memberRepository.save(any(Member.class)))
                .willAnswer(invocation -> invocation.getArgument(0));
            given(jwtProvider.createToken("user@kakao.com"))
                .willReturn("service-jwt");
        }

        @Nested
        @DisplayName("신규 회원이면,")
        class WhenNewMember {

            @Test
            @DisplayName("회원을 등록하고 JWT를 발급한다.")
            void registersAndReturns() {
                // given
                given(memberRepository.findByEmail("user@kakao.com"))
                    .willReturn(Optional.empty());

                // when
                final String jwt = kakaoAuthService.login("auth-code");

                // then
                assertThat(jwt).isEqualTo("service-jwt");
            }
        }

        @Nested
        @DisplayName("기존 회원이면,")
        class WhenExistingMember {

            @Test
            @DisplayName("카카오 토큰을 갱신하고 JWT를 발급한다.")
            void updatesTokenAndReturns() {
                // given
                final Member existing = new Member("user@kakao.com");
                given(memberRepository.findByEmail("user@kakao.com"))
                    .willReturn(Optional.of(existing));

                // when
                final String jwt = kakaoAuthService.login("auth-code");

                // then
                assertThat(jwt).isEqualTo("service-jwt");
                assertThat(existing.getKakaoAccessToken()).isEqualTo("kakao-token");
            }
        }
    }
}
