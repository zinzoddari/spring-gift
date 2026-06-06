package gift.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import gift.infra.jwt.JwtProvider;
import java.util.NoSuchElementException;
import gift.member.domain.Member;
import gift.member.dto.MemberRequest;
import gift.member.repository.MemberRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private JwtProvider jwtProvider;

    @InjectMocks
    private MemberService memberService;

    @Nested
    @DisplayName("포인트를 차감할 때,")
    class DeductPoint {

        @Nested
        @DisplayName("성공하면,")
        class WhenSuccess {

            @Test
            @DisplayName("포인트를 차감하고 회원을 반환한다.")
            void deductsPointAndReturnsMember() {
                // given
                final Member member = mock(Member.class);
                given(memberRepository.findById(1L)).willReturn(Optional.of(member));

                // when
                final Member result = memberService.deductPoint(1L, 5_000);

                // then
                assertThat(result).isEqualTo(member);
                verify(member).deductPoint(5_000);
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
                assertThatThrownBy(() -> memberService.deductPoint(99L, 5_000))
                    .isInstanceOf(NoSuchElementException.class);
            }

            @Test
            @DisplayName("포인트가 부족하면 예외가 발생한다.")
            void throwsWhenInsufficientPoint() {
                // given
                final Member member = mock(Member.class);
                given(memberRepository.findById(1L)).willReturn(Optional.of(member));
                willThrow(new IllegalArgumentException("포인트가 부족합니다."))
                    .given(member).deductPoint(5_000);

                // when & then
                assertThatThrownBy(() -> memberService.deductPoint(1L, 5_000))
                    .isInstanceOf(IllegalArgumentException.class);
            }
        }
    }

    @Nested
    @DisplayName("회원 가입을 할 때,")
    class Register {

        @Nested
        @DisplayName("성공하면,")
        class WhenSuccess {

            @Test
            @DisplayName("회원을 저장하고 JWT를 반환한다.")
            void savesAndReturnsToken() {
                // given
                final MemberRequest request = new MemberRequest("user@example.com", "password123");
                given(memberRepository.existsByEmail("user@example.com")).willReturn(false);
                given(memberRepository.save(any(Member.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));
                given(jwtProvider.createToken("user@example.com")).willReturn("jwt-token");

                // when
                final String token = memberService.register(request);

                // then
                assertSoftly(softly -> softly.assertThat(token).isEqualTo("jwt-token"));
            }
        }

        @Nested
        @DisplayName("실패하면,")
        class WhenFailed {

            @Test
            @DisplayName("이미 등록된 이메일이면 예외가 발생한다.")
            void throwsWhenEmailAlreadyExists() {
                // given
                final MemberRequest request = new MemberRequest("user@example.com", "password123");
                given(memberRepository.existsByEmail("user@example.com")).willReturn(true);

                // when & then
                assertThatThrownBy(() -> memberService.register(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Email is already registered.");
            }
        }
    }

    @Nested
    @DisplayName("로그인을 할 때,")
    class Login {

        @Nested
        @DisplayName("성공하면,")
        class WhenSuccess {

            @Test
            @DisplayName("JWT를 반환한다.")
            void returnsToken() {
                // given
                final MemberRequest request = new MemberRequest("user@example.com", "password123");
                final Member member = new Member("user@example.com", "password123");
                given(memberRepository.findByEmail("user@example.com")).willReturn(Optional.of(member));
                given(jwtProvider.createToken("user@example.com")).willReturn("jwt-token");

                // when
                final String token = memberService.login(request);

                // then
                assertSoftly(softly -> softly.assertThat(token).isEqualTo("jwt-token"));
            }
        }

        @Nested
        @DisplayName("실패하면,")
        class WhenFailed {

            @Test
            @DisplayName("존재하지 않는 이메일이면 예외가 발생한다.")
            void throwsWhenEmailNotFound() {
                // given
                final MemberRequest request = new MemberRequest("unknown@example.com", "password123");
                given(memberRepository.findByEmail("unknown@example.com")).willReturn(Optional.empty());

                // when & then
                assertThatThrownBy(() -> memberService.login(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid email or password.");
            }

            @Test
            @DisplayName("비밀번호가 틀리면 예외가 발생한다.")
            void throwsWhenWrongPassword() {
                // given
                final MemberRequest request = new MemberRequest("user@example.com", "wrong-password");
                final Member member = new Member("user@example.com", "correct-password");
                given(memberRepository.findByEmail("user@example.com")).willReturn(Optional.of(member));

                // when & then
                assertThatThrownBy(() -> memberService.login(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid email or password.");
            }
        }
    }
}
