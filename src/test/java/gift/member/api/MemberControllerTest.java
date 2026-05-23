package gift.member.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import gift.auth.AuthenticationResolver;
import gift.member.dto.MemberRequest;
import gift.member.service.MemberService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@DisplayName("MemberController")
@WebMvcTest(MemberController.class)
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticationResolver authenticationResolver;

    @MockitoBean
    private MemberService memberService;

    @Nested
    @DisplayName("POST /api/members/register 를 호출할 때,")
    class Register {

        @Nested
        @DisplayName("성공하면,")
        class WhenSuccess {

            @Test
            @DisplayName("201과 JWT를 반환한다.")
            void returnsCreatedWithToken() throws Exception {
                // given
                given(memberService.register(any(MemberRequest.class))).willReturn("jwt-token");

                // when & then
                mockMvc.perform(post("/api/members/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"email": "user@example.com", "password": "password123"}
                            """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.token").value("jwt-token"));
            }
        }

        @Nested
        @DisplayName("실패하면,")
        class WhenFailed {

            @Test
            @DisplayName("이미 등록된 이메일이면 400을 반환한다.")
            void returnsBadRequestWhenEmailAlreadyExists() throws Exception {
                // given
                given(memberService.register(any(MemberRequest.class)))
                    .willThrow(new IllegalArgumentException("Email is already registered."));

                // when & then
                mockMvc.perform(post("/api/members/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"email": "user@example.com", "password": "password123"}
                            """))
                    .andExpect(status().isBadRequest());
            }

            @Test
            @DisplayName("이메일 형식이 잘못되면 400을 반환한다.")
            void returnsBadRequestWhenInvalidEmail() throws Exception {
                // when & then
                mockMvc.perform(post("/api/members/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"email": "not-an-email", "password": "password123"}
                            """))
                    .andExpect(status().isBadRequest());
            }
        }
    }

    @Nested
    @DisplayName("POST /api/members/login 을 호출할 때,")
    class Login {

        @Nested
        @DisplayName("성공하면,")
        class WhenSuccess {

            @Test
            @DisplayName("200과 JWT를 반환한다.")
            void returnsOkWithToken() throws Exception {
                // given
                given(memberService.login(any(MemberRequest.class))).willReturn("jwt-token");

                // when & then
                mockMvc.perform(post("/api/members/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"email": "user@example.com", "password": "password123"}
                            """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").value("jwt-token"));
            }
        }

        @Nested
        @DisplayName("실패하면,")
        class WhenFailed {

            @Test
            @DisplayName("존재하지 않는 이메일이면 400을 반환한다.")
            void returnsBadRequestWhenEmailNotFound() throws Exception {
                // given
                given(memberService.login(any(MemberRequest.class)))
                    .willThrow(new IllegalArgumentException("Invalid email or password."));

                // when & then
                mockMvc.perform(post("/api/members/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"email": "unknown@example.com", "password": "password123"}
                            """))
                    .andExpect(status().isBadRequest());
            }

            @Test
            @DisplayName("비밀번호가 틀리면 400을 반환한다.")
            void returnsBadRequestWhenWrongPassword() throws Exception {
                // given
                given(memberService.login(any(MemberRequest.class)))
                    .willThrow(new IllegalArgumentException("Invalid email or password."));

                // when & then
                mockMvc.perform(post("/api/members/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"email": "user@example.com", "password": "wrong-password"}
                            """))
                    .andExpect(status().isBadRequest());
            }
        }
    }
}
