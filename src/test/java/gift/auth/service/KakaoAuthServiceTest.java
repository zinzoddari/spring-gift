package gift.auth.service;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import gift.auth.JwtProvider;
import gift.infra.kakao.KakaoLoginAdapter;
import gift.infra.kakao.KakaoLoginProperties;
import gift.member.Member;
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

@DisplayName("KakaoAuthService")
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
    @DisplayName("loginUrl()을 호출할 때,")
    class LoginUrl {

        @Nested
        @DisplayName("성공하면,")
        class WhenSuccess {

            @Test
            @DisplayName("Kakao 인증 URL을 반환한다.")
            void returnsKakaoAuthUrl() {
                // given
                given(properties.authBaseUrl()).willReturn("https://kauth.kakao.com");
                given(properties.clientId()).willReturn("test-client-id");
                given(properties.redirectUri()).willReturn("http://localhost/callback");

                // when
                final String url = kakaoAuthService.loginUrl();

                // then
                assertSoftly(softly -> {
                    softly.assertThat(url).contains("https://kauth.kakao.com/oauth/authorize");
                    softly.assertThat(url).contains("client_id=test-client-id");
                    softly.assertThat(url).contains("redirect_uri=");
                });
            }
        }
    }

    @Nested
    @DisplayName("login()을 호출할 때,")
    class Login {

        @BeforeEach
        void setUp() {
            given(kakaoLoginAdapter.requestAccessToken("auth-code"))
                .willReturn(new KakaoLoginAdapter.KakaoTokenResponse("kakao-token"));
            given(kakaoLoginAdapter.requestUserInfo("kakao-token"))
                .willReturn(new KakaoLoginAdapter.KakaoUserResponse(
                    new KakaoLoginAdapter.KakaoUserResponse.KakaoAccount("user@kakao.com")));
            given(jwtProvider.createToken("user@kakao.com"))
                .willReturn("service-jwt");
        }

        @Nested
        @DisplayName("신규 회원이면,")
        class WhenNewMember {

            @Test
            @DisplayName("회원을 등록하고 JWT를 반환한다.")
            void registersAndReturnsJwt() {
                // given
                given(memberRepository.findByEmail("user@kakao.com"))
                    .willReturn(Optional.empty());
                given(memberRepository.save(any(Member.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

                // when
                final String jwt = kakaoAuthService.login("auth-code");

                // then
                assertSoftly(softly -> {
                    softly.assertThat(jwt).isEqualTo("service-jwt");
                    then(memberRepository).should().save(any(Member.class));
                });
            }
        }

        @Nested
        @DisplayName("기존 회원이면,")
        class WhenExistingMember {

            @Test
            @DisplayName("카카오 토큰을 갱신하고 JWT를 반환한다.")
            void updatesTokenAndReturnsJwt() {
                // given
                final Member existing = new Member("user@kakao.com");
                given(memberRepository.findByEmail("user@kakao.com"))
                    .willReturn(Optional.of(existing));
                given(memberRepository.save(any(Member.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

                // when
                final String jwt = kakaoAuthService.login("auth-code");

                // then
                assertSoftly(softly -> {
                    softly.assertThat(jwt).isEqualTo("service-jwt");
                    softly.assertThat(existing.getKakaoAccessToken()).isEqualTo("kakao-token");
                });
            }
        }
    }
}
