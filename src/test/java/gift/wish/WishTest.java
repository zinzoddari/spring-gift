package gift.wish;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import gift.product.domain.Product;
import gift.wish.domain.Wish;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Wish")
class WishTest {

    @Nested
    @DisplayName("isOwnedBy()를 호출할 때,")
    class IsOwnedBy {

        @Nested
        @DisplayName("본인의 찜이면,")
        class WhenOwner {

            @Test
            @DisplayName("true를 반환한다.")
            void returnsTrue() {
                // given
                final Wish wish = new Wish(1L, mock(Product.class));

                // when
                final boolean result = wish.isOwnedBy(1L);

                // then
                assertThat(result).isTrue();
            }
        }

        @Nested
        @DisplayName("본인의 찜이 아니면,")
        class WhenNotOwner {

            @Test
            @DisplayName("false를 반환한다.")
            void returnsFalse() {
                // given
                final Wish wish = new Wish(1L, mock(Product.class));

                // when
                final boolean result = wish.isOwnedBy(2L);

                // then
                assertThat(result).isFalse();
            }
        }
    }
}
