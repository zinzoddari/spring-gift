package gift.member.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Member")
class MemberTest {

    @Nested
    @DisplayName("matchesPassword()를 호출할 때,")
    class MatchesPassword {

        @Nested
        @DisplayName("일치하면,")
        class WhenMatch {

            @Test
            @DisplayName("true를 반환한다.")
            void returnsTrue() {
                // given
                final Member member = Member.withCredentials("user@example.com", "password123");

                // when
                final boolean result = member.matchesPassword("password123");

                // then
                assertThat(result).isTrue();
            }
        }

        @Nested
        @DisplayName("일치하지 않으면,")
        class WhenNotMatch {

            @Test
            @DisplayName("비밀번호가 다르면 false를 반환한다.")
            void returnsFalseWhenDifferent() {
                // given
                final Member member = Member.withCredentials("user@example.com", "password123");

                // when
                final boolean result = member.matchesPassword("wrong-password");

                // then
                assertThat(result).isFalse();
            }

            @Test
            @DisplayName("비밀번호가 null이면 false를 반환한다.")
            void returnsFalseWhenNull() {
                // given
                final Member member = Member.withEmail("user@example.com");

                // when
                final boolean result = member.matchesPassword("password123");

                // then
                assertThat(result).isFalse();
            }
        }
    }
}
