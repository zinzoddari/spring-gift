package gift.member.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import gift.infra.jwt.JwtProvider;
import gift.member.Member;
import gift.member.repository.MemberRepository;
import java.util.Optional;
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
    private MemberRepository memberRepository;

    @MockitoBean
    private JwtProvider jwtProvider;

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
                given(memberRepository.existsByEmail("user@example.com")).willReturn(false);
                given(memberRepository.save(any(Member.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));
                given(jwtProvider.createToken("user@example.com")).willReturn("jwt-token");

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
                given(memberRepository.existsByEmail("user@example.com")).willReturn(true);

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
                final Member member = new Member("user@example.com", "password123");
                given(memberRepository.findByEmail("user@example.com")).willReturn(Optional.of(member));
                given(jwtProvider.createToken("user@example.com")).willReturn("jwt-token");

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
                given(memberRepository.findByEmail("unknown@example.com")).willReturn(Optional.empty());

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
                final Member member = new Member("user@example.com", "correct-password");
                given(memberRepository.findByEmail("user@example.com")).willReturn(Optional.of(member));

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
