package gift.member.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import gift.infra.jwt.JwtProvider;
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

@DisplayName("MemberService")
@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private JwtProvider jwtProvider;

    @InjectMocks
    private MemberService memberService;

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
