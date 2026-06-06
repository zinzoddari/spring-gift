package gift.option.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import gift.product.domain.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class OptionTest {

    @Nested
    @DisplayName("가격을 계산할 때,")
    class CalculatePrice {

        @Nested
        @DisplayName("성공하면,")
        class WhenSuccess {

            @Test
            @DisplayName("수량만큼 곱한 총 가격을 반환한다.")
            void returnsCalculatedPrice() {
                // given
                final Product product = mock(Product.class);
                given(product.getPrice()).willReturn(10_000);
                final Option option = new Option(product, "옵션A", 10);

                // when
                final int result = option.calculatePrice(3);

                // then
                assertThat(result).isEqualTo(30_000);
            }
        }
    }

    @Nested
    @DisplayName("재고를 차감할 때,")
    class SubtractQuantity {

        @Nested
        @DisplayName("성공하면,")
        class WhenSuccess {

            @Test
            @DisplayName("차감량이 재고보다 적으면 재고가 줄어든다.")
            void subtractsQuantity() {
                // given
                final Option option = new Option(mock(Product.class), "옵션A", 10);

                // when
                option.subtractQuantity(3);

                // then
                assertThat(option.getQuantity()).isEqualTo(7);
            }

            @Test
            @DisplayName("차감량이 재고와 같으면 재고가 0이 된다.")
            void subtractsToZero() {
                // given
                final Option option = new Option(mock(Product.class), "옵션A", 10);

                // when
                option.subtractQuantity(10);

                // then
                assertThat(option.getQuantity()).isEqualTo(0);
            }
        }

        @Nested
        @DisplayName("실패하면,")
        class WhenFailed {

            @Test
            @DisplayName("차감량이 재고보다 많으면 예외가 발생한다.")
            void throwsWhenAmountExceedsStock() {
                // given
                final Option option = new Option(mock(Product.class), "옵션A", 10);

                // when & then
                assertThatThrownBy(() -> option.subtractQuantity(11))
                    .isInstanceOf(IllegalArgumentException.class);
            }
        }
    }
}
