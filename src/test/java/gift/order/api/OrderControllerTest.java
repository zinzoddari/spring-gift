package gift.order.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import gift.auth.AuthenticationResolver;
import gift.common.dto.PageResponse;
import gift.member.domain.Member;
import gift.order.dto.OrderRequest;
import gift.order.dto.OrderResponse;
import gift.order.service.OrderFacade;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
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
    private OrderFacade orderFacade;

    private Member member() {
        final Member member = mock(Member.class);
        given(member.getId()).willReturn(1L);
        return member;
    }

    private OrderResponse orderResponse() {
        return new OrderResponse(1L, 1L, 2, LocalDateTime.of(2026, 5, 24, 12, 0), "선물입니다");
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
                given(orderFacade.getOrders(eq(1L), any(Pageable.class)))
                    .willReturn(PageResponse.from(new PageImpl<>(List.of(orderResponse()))));

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
                given(orderFacade.createOrder(eq(1L), any(OrderRequest.class)))
                    .willReturn(orderResponse());

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
                given(orderFacade.createOrder(eq(1L), any(OrderRequest.class)))
                    .willThrow(new NoSuchElementException());

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
