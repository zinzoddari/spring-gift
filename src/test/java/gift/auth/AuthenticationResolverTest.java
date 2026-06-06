package gift.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import gift.infra.jwt.JwtProvider;
import gift.member.domain.Member;
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
class AuthenticationResolverTest {

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private AuthenticationResolver authenticationResolver;

    @Nested
    @DisplayName("인증된 회원을 추출할 때,")
    class ExtractMember {

        @Nested
        @DisplayName("성공하면,")
        class WhenSuccess {

            @Test
            @DisplayName("유효한 토큰이면 회원을 반환한다.")
            void returnsMemberForValidToken() {
                // given
                final Member member = mock(Member.class);
                given(jwtProvider.getEmail("valid-token")).willReturn("user@example.com");
                given(memberRepository.findByEmail("user@example.com")).willReturn(Optional.of(member));

                // when
                final Member result = authenticationResolver.extractMember("Bearer valid-token");

                // then
                assertThat(result).isEqualTo(member);
            }
        }

        @Nested
        @DisplayName("실패하면,")
        class WhenFailed {

            @Test
            @DisplayName("토큰에 해당하는 회원이 없으면 null을 반환한다.")
            void returnsNullWhenMemberNotFound() {
                // given
                given(jwtProvider.getEmail("valid-token")).willReturn("unknown@example.com");
                given(memberRepository.findByEmail("unknown@example.com")).willReturn(Optional.empty());

                // when
                final Member result = authenticationResolver.extractMember("Bearer valid-token");

                // then
                assertThat(result).isNull();
            }

            @Test
            @DisplayName("유효하지 않은 토큰이면 null을 반환한다.")
            void returnsNullForInvalidToken() {
                // given
                given(jwtProvider.getEmail("invalid-token")).willThrow(new RuntimeException("Invalid token"));

                // when
                final Member result = authenticationResolver.extractMember("Bearer invalid-token");

                // then
                assertThat(result).isNull();
            }

            @Test
            @DisplayName("Authorization 헤더가 null이면 null을 반환한다.")
            void returnsNullForNullAuthorization() {
                // when
                final Member result = authenticationResolver.extractMember(null);

                // then
                assertThat(result).isNull();
            }
        }
    }
}
