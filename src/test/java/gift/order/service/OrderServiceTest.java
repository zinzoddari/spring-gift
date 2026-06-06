package gift.order.service;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import gift.option.domain.Option;
import gift.order.domain.Order;
import gift.order.dto.OrderResponse;
import gift.common.dto.PageResponse;
import gift.order.repository.OrderRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    private Order order(final Option option) {
        final Order order = mock(Order.class);
        given(order.getId()).willReturn(1L);
        given(order.getOption()).willReturn(option);
        given(order.getQuantity()).willReturn(2);
        given(order.getMessage()).willReturn("선물입니다");
        given(order.getOrderDateTime()).willReturn(LocalDateTime.of(2026, 5, 24, 12, 0));
        return order;
    }

    @Nested
    @DisplayName("주문 목록을 조회할 때,")
    class GetOrders {

        @Nested
        @DisplayName("성공하면,")
        class WhenSuccess {

            @Test
            @DisplayName("페이지네이션된 주문 목록을 반환한다.")
            void returnsOrderPage() {
                // given
                final Option option = mock(Option.class);
                given(option.getId()).willReturn(1L);
                final Order order = order(option);
                given(orderRepository.findByMemberId(eq(1L), any(Pageable.class)))
                    .willReturn(new PageImpl<>(List.of(order)));

                // when
                final PageResponse<OrderResponse> result = orderService.getOrders(1L, Pageable.unpaged());

                // then
                assertSoftly(softly -> {
                    softly.assertThat(result.content()).hasSize(1);
                    softly.assertThat(result.content().get(0).id()).isEqualTo(1L);
                    softly.assertThat(result.content().get(0).optionId()).isEqualTo(1L);
                    softly.assertThat(result.content().get(0).quantity()).isEqualTo(2);
                });
            }
        }
    }

    @Nested
    @DisplayName("주문을 저장할 때,")
    class Save {

        @Nested
        @DisplayName("성공하면,")
        class WhenSuccess {

            @Test
            @DisplayName("저장된 주문을 반환한다.")
            void returnsSavedOrder() {
                // given
                final Option option = mock(Option.class);
                final Order order = mock(Order.class);
                given(order.getId()).willReturn(1L);
                given(order.getQuantity()).willReturn(2);
                given(orderRepository.save(any(Order.class))).willReturn(order);

                // when
                final Order result = orderService.save(option, 1L, 2, "선물입니다");

                // then
                verify(orderRepository).save(any(Order.class));
                assertSoftly(softly -> {
                    softly.assertThat(result.getId()).isEqualTo(1L);
                    softly.assertThat(result.getQuantity()).isEqualTo(2);
                });
            }
        }
    }
}
