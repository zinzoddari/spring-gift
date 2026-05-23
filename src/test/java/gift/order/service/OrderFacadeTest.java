package gift.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import gift.member.domain.Member;
import gift.member.service.MemberService;
import gift.option.domain.Option;
import gift.option.service.OptionService;
import gift.order.domain.Order;
import gift.order.dto.OrderRequest;
import gift.common.dto.PageResponse;
import gift.order.dto.OrderResponse;
import gift.order.event.OrderCreatedEvent;
import gift.product.domain.Product;
import gift.wish.service.WishService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@DisplayName("OrderFacade")
@ExtendWith(MockitoExtension.class)
class OrderFacadeTest {

    @Mock
    private OptionService optionService;

    @Mock
    private MemberService memberService;

    @Mock
    private OrderService orderService;

    @Mock
    private WishService wishService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private OrderFacade orderFacade;

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
            @DisplayName("OrderService에 위임하여 목록을 반환한다.")
            void delegatesToOrderService() {
                // given
                final OrderResponse orderResponse = new OrderResponse(1L, 1L, 2, LocalDateTime.of(2026, 5, 24, 12, 0), "선물입니다");
                given(orderService.getOrders(eq(1L), any(Pageable.class)))
                    .willReturn(PageResponse.from(new PageImpl<>(List.of(orderResponse))));

                // when
                final PageResponse<OrderResponse> result = orderFacade.getOrders(1L, Pageable.unpaged());

                // then
                assertThat(result.content()).hasSize(1);
            }
        }
    }

    @Nested
    @DisplayName("주문을 생성할 때,")
    class CreateOrder {

        @Nested
        @DisplayName("성공하면,")
        class WhenSuccess {

            @Test
            @DisplayName("주문을 저장하고 OrderCreatedEvent를 발행한다.")
            void savesOrderAndPublishesEvent() {
                // given
                final Product product = mock(Product.class);
                given(product.getId()).willReturn(1L);
                given(product.getName()).willReturn("상품A");
                given(product.getPrice()).willReturn(10_000);
                final Option option = mock(Option.class);
                given(option.getId()).willReturn(1L);
                given(option.calculatePrice(2)).willReturn(20_000);
                given(option.getName()).willReturn("옵션A");
                given(option.getProduct()).willReturn(product);
                final Member member = mock(Member.class);
                given(member.getKakaoAccessToken()).willReturn("kakao-token");
                final Order order = order(option);
                final OrderRequest request = new OrderRequest(1L, 2, "선물입니다");
                given(optionService.subtractQuantity(1L, 2)).willReturn(option);
                given(memberService.deductPoint(1L, 20_000)).willReturn(member);
                given(orderService.save(option, 1L, 2, "선물입니다")).willReturn(order);

                // when
                final OrderResponse result = orderFacade.createOrder(1L, request);

                // then
                assertSoftly(softly -> {
                    softly.assertThat(result.id()).isEqualTo(1L);
                    softly.assertThat(result.optionId()).isEqualTo(1L);
                    softly.assertThat(result.quantity()).isEqualTo(2);
                });
                verify(wishService).removeWishByProduct(1L, 1L);
                verify(eventPublisher).publishEvent(any(OrderCreatedEvent.class));
            }
        }

        @Nested
        @DisplayName("실패하면,")
        class WhenFailed {

            @Test
            @DisplayName("옵션이 없으면 예외가 발생한다.")
            void throwsWhenOptionNotFound() {
                // given
                final OrderRequest request = new OrderRequest(99L, 2, "선물입니다");
                given(optionService.subtractQuantity(99L, 2))
                    .willThrow(new NoSuchElementException("옵션을 찾을 수 없습니다."));

                // when & then
                assertThatThrownBy(() -> orderFacade.createOrder(1L, request))
                    .isInstanceOf(NoSuchElementException.class);
            }

            @Test
            @DisplayName("재고가 부족하면 예외가 발생한다.")
            void throwsWhenInsufficientStock() {
                // given
                final OrderRequest request = new OrderRequest(1L, 100, "선물입니다");
                given(optionService.subtractQuantity(1L, 100))
                    .willThrow(new IllegalArgumentException("차감할 수량이 현재 재고보다 많습니다."));

                // when & then
                assertThatThrownBy(() -> orderFacade.createOrder(1L, request))
                    .isInstanceOf(IllegalArgumentException.class);
            }

            @Test
            @DisplayName("회원이 없으면 예외가 발생한다.")
            void throwsWhenMemberNotFound() {
                // given
                final Option option = mock(Option.class);
                given(option.calculatePrice(2)).willReturn(20_000);
                final OrderRequest request = new OrderRequest(1L, 2, "선물입니다");
                given(optionService.subtractQuantity(1L, 2)).willReturn(option);
                given(memberService.deductPoint(1L, 20_000))
                    .willThrow(new NoSuchElementException("회원을 찾을 수 없습니다."));

                // when & then
                assertThatThrownBy(() -> orderFacade.createOrder(1L, request))
                    .isInstanceOf(NoSuchElementException.class);
            }
        }
    }
}
