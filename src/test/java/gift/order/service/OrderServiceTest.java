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
import static org.mockito.Mockito.verifyNoInteractions;

import gift.infra.client.kakao.KakaoMessageAdapter;
import gift.member.domain.Member;
import gift.member.repository.MemberRepository;
import gift.option.domain.Option;
import gift.option.repository.OptionRepository;
import gift.order.Order;
import gift.order.dto.OrderRequest;
import gift.order.dto.OrderResponse;
import gift.order.repository.OrderRepository;
import gift.product.domain.Product;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@DisplayName("OrderService")
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OptionRepository optionRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private KakaoMessageAdapter kakaoMessageAdapter;

    @InjectMocks
    private OrderService orderService;

    private Product product() {
        final Product product = mock(Product.class);
        given(product.getPrice()).willReturn(10_000);
        return product;
    }

    private Option option(final Product product) {
        final Option option = mock(Option.class);
        given(option.getId()).willReturn(1L);
        given(option.getProduct()).willReturn(product);
        return option;
    }

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
                final Page<OrderResponse> result = orderService.getOrders(1L, Pageable.unpaged());

                // then
                assertSoftly(softly -> {
                    softly.assertThat(result.getContent()).hasSize(1);
                    softly.assertThat(result.getContent().get(0).id()).isEqualTo(1L);
                    softly.assertThat(result.getContent().get(0).optionId()).isEqualTo(1L);
                    softly.assertThat(result.getContent().get(0).quantity()).isEqualTo(2);
                });
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
            @DisplayName("카카오 토큰이 없으면 알림 없이 주문을 생성한다.")
            void createsOrderWithoutNotification() {
                // given
                final Product product = product();
                final Option option = option(product);
                final Member member = mock(Member.class);
                given(member.getKakaoAccessToken()).willReturn(null);
                final Order order = order(option);
                final OrderRequest request = new OrderRequest(1L, 2, "선물입니다");
                given(optionRepository.findById(1L)).willReturn(Optional.of(option));
                given(memberRepository.findById(1L)).willReturn(Optional.of(member));
                given(orderRepository.save(any(Order.class))).willReturn(order);

                // when
                final OrderResponse result = orderService.createOrder(1L, request);

                // then
                assertSoftly(softly -> {
                    softly.assertThat(result.id()).isEqualTo(1L);
                    softly.assertThat(result.optionId()).isEqualTo(1L);
                    softly.assertThat(result.quantity()).isEqualTo(2);
                });
                verifyNoInteractions(kakaoMessageAdapter);
            }

            @Test
            @DisplayName("카카오 토큰이 있으면 주문 생성 후 카카오 알림을 전송한다.")
            void sendsKakaoNotificationWhenTokenExists() {
                // given
                final Product product = product();
                final Option option = option(product);
                final Member member = mock(Member.class);
                given(member.getKakaoAccessToken()).willReturn("kakao-token");
                final Order order = order(option);
                final OrderRequest request = new OrderRequest(1L, 2, "선물입니다");
                given(optionRepository.findById(1L)).willReturn(Optional.of(option));
                given(memberRepository.findById(1L)).willReturn(Optional.of(member));
                given(orderRepository.save(any(Order.class))).willReturn(order);

                // when
                orderService.createOrder(1L, request);

                // then
                verify(kakaoMessageAdapter).sendToMe("kakao-token", order, product);
            }

            @Test
            @DisplayName("카카오 알림 전송이 실패해도 주문은 생성된다.")
            void succeedsEvenWhenKakaoFails() {
                // given
                final Product product = product();
                final Option option = option(product);
                final Member member = mock(Member.class);
                given(member.getKakaoAccessToken()).willReturn("kakao-token");
                final Order order = order(option);
                final OrderRequest request = new OrderRequest(1L, 2, "선물입니다");
                given(optionRepository.findById(1L)).willReturn(Optional.of(option));
                given(memberRepository.findById(1L)).willReturn(Optional.of(member));
                given(orderRepository.save(any(Order.class))).willReturn(order);
                willThrow(new RuntimeException("카카오 오류"))
                    .given(kakaoMessageAdapter).sendToMe(any(), any(), any());

                // when
                final OrderResponse result = orderService.createOrder(1L, request);

                // then
                assertThat(result.id()).isEqualTo(1L);
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
                given(optionRepository.findById(99L)).willReturn(Optional.empty());

                // when & then
                assertThatThrownBy(() -> orderService.createOrder(1L, request))
                    .isInstanceOf(NoSuchElementException.class);
            }

            @Test
            @DisplayName("재고가 부족하면 예외가 발생한다.")
            void throwsWhenInsufficientStock() {
                // given
                final Option option = mock(Option.class);
                final OrderRequest request = new OrderRequest(1L, 100, "선물입니다");
                given(optionRepository.findById(1L)).willReturn(Optional.of(option));
                willThrow(new IllegalArgumentException("차감할 수량이 현재 재고보다 많습니다."))
                    .given(option).subtractQuantity(100);

                // when & then
                assertThatThrownBy(() -> orderService.createOrder(1L, request))
                    .isInstanceOf(IllegalArgumentException.class);
            }

            @Test
            @DisplayName("회원이 없으면 예외가 발생한다.")
            void throwsWhenMemberNotFound() {
                // given
                final Option option = mock(Option.class);
                final OrderRequest request = new OrderRequest(1L, 2, "선물입니다");
                given(optionRepository.findById(1L)).willReturn(Optional.of(option));
                given(memberRepository.findById(1L)).willReturn(Optional.empty());

                // when & then
                assertThatThrownBy(() -> orderService.createOrder(1L, request))
                    .isInstanceOf(NoSuchElementException.class);
            }
        }
    }
}
