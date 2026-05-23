package gift.auth.api;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import gift.auth.JwtProvider;
import gift.infra.kakao.KakaoLoginAdapter;
import gift.infra.kakao.KakaoLoginProperties;
import gift.member.Member;
import gift.member.repository.MemberRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@DisplayName("KakaoAuthController")
@WebMvcTest(KakaoAuthController.class)
@Import(KakaoAuthControllerTest.TestConfig.class)
class KakaoAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private KakaoLoginAdapter kakaoLoginAdapter;

    @MockitoBean
    private MemberRepository memberRepository;

    @MockitoBean
    private JwtProvider jwtProvider;

    @TestConfiguration
    static class TestConfig {
        @Bean
        KakaoLoginProperties kakaoLoginProperties() {
            return new KakaoLoginProperties(
                "test-client-id",
                "test-client-secret",
                "http://localhost/callback",
                "https://kauth.kakao.com",
                "https://kapi.kakao.com"
            );
        }
    }

    @Nested
    @DisplayName("GET /api/auth/kakao/login 을 호출할 때,")
    class Login {

        @Nested
        @DisplayName("성공하면,")
        class WhenSuccess {

            @Test
            @DisplayName("Kakao 인증 페이지로 리다이렉트한다.")
            void redirectsToKakaoAuthPage() throws Exception {
                // when & then
                mockMvc.perform(get("/api/auth/kakao/login"))
                    .andExpect(status().isFound())
                    .andExpect(header().string(HttpHeaders.LOCATION,
                        containsString("https://kauth.kakao.com/oauth/authorize")))
                    .andExpect(header().string(HttpHeaders.LOCATION,
                        containsString("client_id=test-client-id")))
                    .andExpect(header().string(HttpHeaders.LOCATION,
                        containsString("redirect_uri=")));
            }
        }
    }

    @Nested
    @DisplayName("GET /api/auth/kakao/callback 을 호출할 때,")
    class Callback {

        @Nested
        @DisplayName("신규 회원이면,")
        class WhenNewMember {

            @Test
            @DisplayName("회원 가입 후 JWT를 반환한다.")
            void registersAndReturnsToken() throws Exception {
                // given
                given(kakaoLoginAdapter.requestAccessToken("auth-code"))
                    .willReturn(new KakaoLoginAdapter.KakaoTokenResponse("kakao-token"));
                given(kakaoLoginAdapter.requestUserInfo("kakao-token"))
                    .willReturn(new KakaoLoginAdapter.KakaoUserResponse(
                        new KakaoLoginAdapter.KakaoUserResponse.KakaoAccount("user@kakao.com")));
                given(memberRepository.findByEmail("user@kakao.com"))
                    .willReturn(Optional.empty());
                given(memberRepository.save(any(Member.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));
                given(jwtProvider.createToken("user@kakao.com"))
                    .willReturn("service-jwt");

                // when & then
                mockMvc.perform(get("/api/auth/kakao/callback").param("code", "auth-code"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").value("service-jwt"));
            }
        }

        @Nested
        @DisplayName("기존 회원이면,")
        class WhenExistingMember {

            @Test
            @DisplayName("카카오 토큰을 갱신하고 JWT를 반환한다.")
            void updatesTokenAndReturnsJwt() throws Exception {
                // given
                given(kakaoLoginAdapter.requestAccessToken("auth-code"))
                    .willReturn(new KakaoLoginAdapter.KakaoTokenResponse("kakao-token"));
                given(kakaoLoginAdapter.requestUserInfo("kakao-token"))
                    .willReturn(new KakaoLoginAdapter.KakaoUserResponse(
                        new KakaoLoginAdapter.KakaoUserResponse.KakaoAccount("user@kakao.com")));
                given(memberRepository.findByEmail("user@kakao.com"))
                    .willReturn(Optional.of(new Member("user@kakao.com")));
                given(memberRepository.save(any(Member.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));
                given(jwtProvider.createToken("user@kakao.com"))
                    .willReturn("service-jwt");

                // when & then
                mockMvc.perform(get("/api/auth/kakao/callback").param("code", "auth-code"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").value("service-jwt"));
            }
        }
    }
}
