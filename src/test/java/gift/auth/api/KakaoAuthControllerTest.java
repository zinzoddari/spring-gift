package gift.auth.api;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import gift.auth.service.KakaoAuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@DisplayName("KakaoAuthController")
@WebMvcTest(KakaoAuthController.class)
class KakaoAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private KakaoAuthService kakaoAuthService;

    @Nested
    @DisplayName("GET /api/auth/kakao/login 을 호출할 때,")
    class Login {

        @Nested
        @DisplayName("성공하면,")
        class WhenSuccess {

            @Test
            @DisplayName("서비스가 반환한 URL로 리다이렉트한다.")
            void redirectsToLoginUrl() throws Exception {
                // given
                given(kakaoAuthService.loginUrl())
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
    @DisplayName("GET /api/auth/kakao/callback 을 호출할 때,")
    class Callback {

        @Nested
        @DisplayName("성공하면,")
        class WhenSuccess {

            @Test
            @DisplayName("서비스가 반환한 JWT를 응답한다.")
            void returnsJwt() throws Exception {
                // given
                given(kakaoAuthService.login("auth-code"))
                    .willReturn("service-jwt");

                // when & then
                mockMvc.perform(get("/api/auth/kakao/callback").param("code", "auth-code"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").value("service-jwt"));
            }
        }
    }
}
