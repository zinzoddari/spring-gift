package gift.order.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import gift.auth.AuthenticationResolver;
import gift.infra.client.kakao.KakaoMessageAdapter;
import gift.member.domain.Member;
import gift.member.repository.MemberRepository;
import gift.option.domain.Option;
import gift.option.repository.OptionRepository;
import gift.order.Order;
import gift.order.repository.OrderRepository;
import gift.product.domain.Product;
import gift.wish.repository.WishRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@DisplayName("OrderController")
@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticationResolver authenticationResolver;

    @MockitoBean
    private OrderRepository orderRepository;

    @MockitoBean
    private OptionRepository optionRepository;

    @MockitoBean
    private WishRepository wishRepository;

    @MockitoBean
    private MemberRepository memberRepository;

    @MockitoBean
    private KakaoMessageAdapter kakaoMessageAdapter;

    private Member member() {
        final Member member = mock(Member.class);
        given(member.getId()).willReturn(1L);
        given(member.getKakaoAccessToken()).willReturn(null);
        return member;
    }

    private Option option() {
        final Option option = mock(Option.class);
        given(option.getId()).willReturn(1L);
        given(option.getProduct()).willReturn(mock(Product.class));
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
            void returnsOrderPage() throws Exception {
                // given
                final Member member = member();
                given(authenticationResolver.extractMember(any())).willReturn(member);
                final Option option = option();
                final Order order = order(option);
                given(orderRepository.findByMemberId(any(), any(Pageable.class)))
                    .willReturn(new PageImpl<>(List.of(order)));

                // when & then
                mockMvc.perform(get("/api/orders")
                        .header("Authorization", "Bearer token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(1))
                    .andExpect(jsonPath("$.content[0].optionId").value(1))
                    .andExpect(jsonPath("$.content[0].quantity").value(2));
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
            @DisplayName("생성된 주문과 201을 반환한다.")
            void returnsCreatedOrder() throws Exception {
                // given
                final Member member = member();
                given(authenticationResolver.extractMember(any())).willReturn(member);
                final Option option = option();
                given(optionRepository.findById(1L)).willReturn(Optional.of(option));
                given(optionRepository.save(option)).willReturn(option);
                given(memberRepository.findById(1L)).willReturn(Optional.of(member));
                given(memberRepository.save(member)).willReturn(member);
                final Order order = order(option);
                given(orderRepository.save(any(Order.class))).willReturn(order);

                // when & then
                mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"optionId": 1, "quantity": 2, "message": "선물입니다"}
                            """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.optionId").value(1))
                    .andExpect(jsonPath("$.quantity").value(2));
            }
        }

        @Nested
        @DisplayName("실패하면,")
        class WhenFailed {

            @Test
            @DisplayName("옵션이 없으면 404를 반환한다.")
            void returnsNotFoundWhenOptionNotFound() throws Exception {
                // given
                final Member member = member();
                given(authenticationResolver.extractMember(any())).willReturn(member);
                given(optionRepository.findById(99L)).willReturn(Optional.empty());

                // when & then
                mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"optionId": 99, "quantity": 2, "message": "선물입니다"}
                            """))
                    .andExpect(status().isNotFound());
            }
        }
    }
}
