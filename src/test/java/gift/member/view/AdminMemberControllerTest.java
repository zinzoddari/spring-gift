package gift.member.view;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import gift.auth.AuthenticationResolver;
import gift.member.domain.Member;
import gift.member.service.AdminMemberService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@DisplayName("AdminMemberController")
@WebMvcTest(AdminMemberController.class)
class AdminMemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticationResolver authenticationResolver;

    @MockitoBean
    private AdminMemberService adminMemberService;

    private Member member() {
        final Member member = mock(Member.class);
        given(member.getId()).willReturn(1L);
        given(member.getEmail()).willReturn("test@test.com");
        given(member.getPassword()).willReturn("password");
        given(member.getPoint()).willReturn(1_000);
        return member;
    }

    @Nested
    @DisplayName("회원 목록을 조회할 때,")
    class MemberList {

        @Nested
        @DisplayName("성공하면,")
        class WhenSuccess {

            @Test
            @DisplayName("회원 목록 뷰를 반환한다.")
            void returnsMemberListView() throws Exception {
                // given
                final Member member = member();
                given(adminMemberService.getMembers()).willReturn(List.of(member));

                // when & then
                mockMvc.perform(get("/admin/members"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("member/list"))
                    .andExpect(model().attributeExists("members"));
            }
        }
    }

    @Nested
    @DisplayName("회원 등록 폼을 조회할 때,")
    class NewForm {

        @Nested
        @DisplayName("성공하면,")
        class WhenSuccess {

            @Test
            @DisplayName("회원 등록 뷰를 반환한다.")
            void returnsMemberNewView() throws Exception {
                mockMvc.perform(get("/admin/members/new"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("member/new"));
            }
        }
    }

    @Nested
    @DisplayName("회원을 등록할 때,")
    class Create {

        @Nested
        @DisplayName("성공하면,")
        class WhenSuccess {

            @Test
            @DisplayName("회원 목록으로 리다이렉트한다.")
            void redirectsToMemberList() throws Exception {
                // given
                willDoNothing().given(adminMemberService).createMember("test@test.com", "password");

                // when & then
                mockMvc.perform(post("/admin/members")
                        .param("email", "test@test.com")
                        .param("password", "password"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/admin/members"));
            }
        }

        @Nested
        @DisplayName("실패하면,")
        class WhenFailed {

            @Test
            @DisplayName("이미 등록된 이메일이면 등록 폼으로 돌아간다.")
            void returnsNewFormWhenEmailDuplicated() throws Exception {
                // given
                willThrow(new IllegalArgumentException("Email is already registered."))
                    .given(adminMemberService).createMember("test@test.com", "password");

                // when & then
                mockMvc.perform(post("/admin/members")
                        .param("email", "test@test.com")
                        .param("password", "password"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("member/new"))
                    .andExpect(model().attributeExists("error"));
            }
        }
    }

    @Nested
    @DisplayName("회원 수정 폼을 조회할 때,")
    class EditForm {

        @Nested
        @DisplayName("성공하면,")
        class WhenSuccess {

            @Test
            @DisplayName("회원 수정 뷰를 반환한다.")
            void returnsMemberEditView() throws Exception {
                // given
                final Member member = member();
                given(adminMemberService.getMember(1L)).willReturn(member);

                // when & then
                mockMvc.perform(get("/admin/members/1/edit"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("member/edit"))
                    .andExpect(model().attributeExists("member"));
            }
        }

        @Nested
        @DisplayName("실패하면,")
        class WhenFailed {

            @Test
            @DisplayName("회원이 없으면 400을 반환한다.")
            void returnsBadRequestWhenMemberNotFound() throws Exception {
                // given
                given(adminMemberService.getMember(99L))
                    .willThrow(new IllegalArgumentException("Member not found. id=99"));

                // when & then
                mockMvc.perform(get("/admin/members/99/edit"))
                    .andExpect(status().isBadRequest());
            }
        }
    }

    @Nested
    @DisplayName("회원을 수정할 때,")
    class Update {

        @Nested
        @DisplayName("성공하면,")
        class WhenSuccess {

            @Test
            @DisplayName("회원 목록으로 리다이렉트한다.")
            void redirectsToMemberList() throws Exception {
                // given
                willDoNothing().given(adminMemberService).updateMember(1L, "updated@test.com", "newpassword");

                // when & then
                mockMvc.perform(post("/admin/members/1/edit")
                        .param("email", "updated@test.com")
                        .param("password", "newpassword"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/admin/members"));
            }
        }

        @Nested
        @DisplayName("실패하면,")
        class WhenFailed {

            @Test
            @DisplayName("회원이 없으면 400을 반환한다.")
            void returnsBadRequestWhenMemberNotFound() throws Exception {
                // given
                willThrow(new IllegalArgumentException("Member not found. id=99"))
                    .given(adminMemberService).updateMember(99L, "updated@test.com", "newpassword");

                // when & then
                mockMvc.perform(post("/admin/members/99/edit")
                        .param("email", "updated@test.com")
                        .param("password", "newpassword"))
                    .andExpect(status().isBadRequest());
            }
        }
    }

    @Nested
    @DisplayName("포인트를 충전할 때,")
    class ChargePoint {

        @Nested
        @DisplayName("성공하면,")
        class WhenSuccess {

            @Test
            @DisplayName("회원 목록으로 리다이렉트한다.")
            void redirectsToMemberList() throws Exception {
                // given
                willDoNothing().given(adminMemberService).chargePoint(1L, 5_000);

                // when & then
                mockMvc.perform(post("/admin/members/1/charge-point")
                        .param("amount", "5000"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/admin/members"));
            }
        }

        @Nested
        @DisplayName("실패하면,")
        class WhenFailed {

            @Test
            @DisplayName("회원이 없으면 400을 반환한다.")
            void returnsBadRequestWhenMemberNotFound() throws Exception {
                // given
                willThrow(new IllegalArgumentException("Member not found. id=99"))
                    .given(adminMemberService).chargePoint(99L, 5_000);

                // when & then
                mockMvc.perform(post("/admin/members/99/charge-point")
                        .param("amount", "5000"))
                    .andExpect(status().isBadRequest());
            }
        }
    }

    @Nested
    @DisplayName("회원을 삭제할 때,")
    class Delete {

        @Nested
        @DisplayName("성공하면,")
        class WhenSuccess {

            @Test
            @DisplayName("회원 목록으로 리다이렉트한다.")
            void redirectsToMemberList() throws Exception {
                // given
                willDoNothing().given(adminMemberService).deleteMember(1L);

                // when & then
                mockMvc.perform(post("/admin/members/1/delete"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/admin/members"));
            }
        }
    }
}
