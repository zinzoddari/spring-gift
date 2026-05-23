package gift.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import gift.member.domain.Member;
import gift.member.dto.AdminMemberResponse;
import gift.member.repository.MemberRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("AdminMemberService")
@ExtendWith(MockitoExtension.class)
class AdminMemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private AdminMemberService adminMemberService;

    private Member member() {
        final Member member = mock(Member.class);
        given(member.getId()).willReturn(1L);
        given(member.getEmail()).willReturn("test@test.com");
        given(member.getPoint()).willReturn(1_000);
        return member;
    }

    @Nested
    @DisplayName("회원 목록을 조회할 때,")
    class GetMembers {

        @Nested
        @DisplayName("성공하면,")
        class WhenSuccess {

            @Test
            @DisplayName("전체 회원 목록을 반환한다.")
            void returnsMemberList() {
                // given
                final Member member = member();
                given(memberRepository.findAll()).willReturn(List.of(member));

                // when
                final List<AdminMemberResponse> result = adminMemberService.getMembers();

                // then
                assertSoftly(softly -> {
                    softly.assertThat(result).hasSize(1);
                    softly.assertThat(result.get(0).id()).isEqualTo(1L);
                    softly.assertThat(result.get(0).email()).isEqualTo("test@test.com");
                });
            }
        }
    }

    @Nested
    @DisplayName("회원을 단건 조회할 때,")
    class GetMember {

        @Nested
        @DisplayName("성공하면,")
        class WhenSuccess {

            @Test
            @DisplayName("회원을 반환한다.")
            void returnsMember() {
                // given
                final Member member = member();
                given(memberRepository.findById(1L)).willReturn(Optional.of(member));

                // when
                final AdminMemberResponse result = adminMemberService.getMember(1L);

                // then
                assertThat(result.id()).isEqualTo(1L);
            }
        }

        @Nested
        @DisplayName("실패하면,")
        class WhenFailed {

            @Test
            @DisplayName("회원이 없으면 예외가 발생한다.")
            void throwsWhenMemberNotFound() {
                // given
                given(memberRepository.findById(99L)).willReturn(Optional.empty());

                // when & then
                assertThatThrownBy(() -> adminMemberService.getMember(99L))
                    .isInstanceOf(IllegalArgumentException.class);
            }
        }
    }

    @Nested
    @DisplayName("회원을 등록할 때,")
    class CreateMember {

        @Nested
        @DisplayName("성공하면,")
        class WhenSuccess {

            @Test
            @DisplayName("회원을 저장한다.")
            void savesMember() {
                // given
                given(memberRepository.existsByEmail("test@test.com")).willReturn(false);

                // when
                adminMemberService.createMember("test@test.com", "password");

                // then
                verify(memberRepository).save(any(Member.class));
            }
        }

        @Nested
        @DisplayName("실패하면,")
        class WhenFailed {

            @Test
            @DisplayName("이미 등록된 이메일이면 예외가 발생한다.")
            void throwsWhenEmailDuplicated() {
                // given
                given(memberRepository.existsByEmail("test@test.com")).willReturn(true);

                // when & then
                assertThatThrownBy(() -> adminMemberService.createMember("test@test.com", "password"))
                    .isInstanceOf(IllegalArgumentException.class);
            }
        }
    }

    @Nested
    @DisplayName("회원을 수정할 때,")
    class UpdateMember {

        @Nested
        @DisplayName("성공하면,")
        class WhenSuccess {

            @Test
            @DisplayName("회원 정보를 수정한다.")
            void updatesMember() {
                // given
                final Member member = mock(Member.class);
                given(memberRepository.findById(1L)).willReturn(Optional.of(member));

                // when
                adminMemberService.updateMember(1L, "new@test.com", "newpassword");

                // then
                verify(member).update("new@test.com", "newpassword");
            }
        }

        @Nested
        @DisplayName("실패하면,")
        class WhenFailed {

            @Test
            @DisplayName("회원이 없으면 예외가 발생한다.")
            void throwsWhenMemberNotFound() {
                // given
                given(memberRepository.findById(99L)).willReturn(Optional.empty());

                // when & then
                assertThatThrownBy(() -> adminMemberService.updateMember(99L, "new@test.com", "newpassword"))
                    .isInstanceOf(IllegalArgumentException.class);
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
            @DisplayName("포인트를 충전한다.")
            void chargesPoint() {
                // given
                final Member member = mock(Member.class);
                given(memberRepository.findById(1L)).willReturn(Optional.of(member));

                // when
                adminMemberService.chargePoint(1L, 5_000);

                // then
                verify(member).chargePoint(5_000);
            }
        }

        @Nested
        @DisplayName("실패하면,")
        class WhenFailed {

            @Test
            @DisplayName("회원이 없으면 예외가 발생한다.")
            void throwsWhenMemberNotFound() {
                // given
                given(memberRepository.findById(99L)).willReturn(Optional.empty());

                // when & then
                assertThatThrownBy(() -> adminMemberService.chargePoint(99L, 5_000))
                    .isInstanceOf(IllegalArgumentException.class);
            }
        }
    }

    @Nested
    @DisplayName("회원을 삭제할 때,")
    class DeleteMember {

        @Nested
        @DisplayName("성공하면,")
        class WhenSuccess {

            @Test
            @DisplayName("삭제를 호출한다.")
            void callsDelete() {
                // given
                willDoNothing().given(memberRepository).deleteById(1L);

                // when
                adminMemberService.deleteMember(1L);

                // then
                verify(memberRepository).deleteById(1L);
            }
        }
    }
}
