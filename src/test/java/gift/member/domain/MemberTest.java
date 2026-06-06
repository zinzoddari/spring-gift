package gift.member.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class MemberTest {

    @Nested
    @DisplayName("포인트를 차감할 때,")
    class DeductPoint {

        @Nested
        @DisplayName("성공하면,")
        class WhenSuccess {

            @Test
            @DisplayName("차감량이 잔액보다 적으면 포인트가 줄어든다.")
            void deductsPoint() {
                // given
                final Member member = Member.withEmail("user@example.com");
                member.chargePoint(10_000);

                // when
                member.deductPoint(3_000);

                // then
                assertThat(member.getPoint()).isEqualTo(7_000);
            }

            @Test
            @DisplayName("차감량이 잔액과 같으면 포인트가 0이 된다.")
            void deductsToZero() {
                // given
                final Member member = Member.withEmail("user@example.com");
                member.chargePoint(10_000);

                // when
                member.deductPoint(10_000);

                // then
                assertThat(member.getPoint()).isEqualTo(0);
            }
        }

        @Nested
        @DisplayName("실패하면,")
        class WhenFailed {

            @Test
            @DisplayName("차감량이 0 이하면 예외가 발생한다.")
            void throwsWhenAmountIsNotPositive() {
                // given
                final Member member = Member.withEmail("user@example.com");
                member.chargePoint(10_000);

                // when & then
                assertThatThrownBy(() -> member.deductPoint(0))
                    .isInstanceOf(IllegalArgumentException.class);
            }

            @Test
            @DisplayName("잔액이 부족하면 예외가 발생한다.")
            void throwsWhenInsufficientPoint() {
                // given
                final Member member = Member.withEmail("user@example.com");
                member.chargePoint(10_000);

                // when & then
                assertThatThrownBy(() -> member.deductPoint(10_001))
                    .isInstanceOf(IllegalArgumentException.class);
            }
        }
    }

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
