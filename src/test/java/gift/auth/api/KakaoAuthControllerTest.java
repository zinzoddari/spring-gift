package gift.auth.api;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import gift.auth.AuthenticationResolver;
import gift.auth.service.KakaoAuthFacade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(KakaoAuthController.class)
class KakaoAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticationResolver authenticationResolver;

    @MockitoBean
    private KakaoAuthFacade kakaoAuthFacade;

    @Nested
    @DisplayName("카카오 로그인을 요청할 때,")
    class Login {

        @Nested
        @DisplayName("성공하면,")
        class WhenSuccess {

            @Test
            @DisplayName("카카오 인증 URL로 리다이렉트한다.")
            void redirectsToLoginUrl() throws Exception {
                // given
                given(kakaoAuthFacade.loginUrl())
                    .willReturn("https://kauth.kakao.com/oauth/authorize?client_id=test");

                // when & then
                mockMvc.perform(get("/api/auth/kakao/login"))
                    .andExpect(status().isFound())
                    .andExpect(header().string(HttpHeaders.LOCATION,
                        containsString("https://kauth.kakao.com/oauth/authorize")));
            }
        }
    }

    @Nested
    @DisplayName("카카오 인증 콜백을 처리할 때,")
    class Callback {

        @Nested
        @DisplayName("성공하면,")
        class WhenSuccess {

            @Test
            @DisplayName("JWT 토큰을 응답한다.")
            void returnsJwt() throws Exception {
                // given
                given(kakaoAuthFacade.login("auth-code"))
                    .willReturn("service-jwt");

                // when & then
                mockMvc.perform(get("/api/auth/kakao/callback").param("code", "auth-code"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").value("service-jwt"));
            }
        }
    }
}
