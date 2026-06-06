package gift.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import gift.category.domain.Category;
import gift.category.repository.CategoryRepository;
import gift.infra.client.kakao.KakaoMessageAdapter;
import gift.member.domain.Member;
import gift.member.repository.MemberRepository;
import gift.option.domain.Option;
import gift.option.repository.OptionRepository;
import gift.order.dto.OrderRequest;
import gift.order.event.OrderCreatedEvent;
import gift.order.repository.OrderRepository;
import gift.product.domain.Product;
import gift.product.repository.ProductRepository;
import gift.wish.domain.Wish;
import gift.wish.repository.WishRepository;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@RecordApplicationEvents
class OrderFacadeIntegrationTest {

    @Autowired
    private OrderFacade orderFacade;

    @Autowired
    private OptionRepository optionRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private WishRepository wishRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ApplicationEvents applicationEvents;

    @MockitoBean
    private KakaoMessageAdapter kakaoMessageAdapter;

    private Long memberId;
    private Long optionId;
    private Long productId;

    @BeforeEach
    void setUp() {
        final Category category = categoryRepository.save(
            new Category("카테고리", "#FF0000", "http://img.jpg", null)
        );
        final Product product = productRepository.save(
            new Product("상품A", 10_000, "http://img.jpg", category)
        );
        final Option option = optionRepository.save(new Option(product, "옵션A", 10));

        final Member member = new Member("user@test.com");
        memberRepository.save(member);

        wishRepository.save(new Wish(member.getId(), product));

        memberId = member.getId();
        optionId = option.getId();
        productId = product.getId();
    }

    @Nested
    @DisplayName("주문을 생성할 때,")
    class CreateOrder {

        @Nested
        @DisplayName("성공하면,")
        class WhenSuccess {

            @Test
            @DisplayName("재고 차감, 포인트 차감, 주문 저장, 위시리스트 삭제가 모두 반영된다.")
            void createsOrderWithAllSideEffects() {
                // given
                final Member member = memberRepository.findById(memberId).orElseThrow();
                member.chargePoint(100_000);

                // when
                orderFacade.createOrder(memberId, new OrderRequest(optionId, 2, "선물입니다"));

                // then
                assertSoftly(softly -> {
                    softly.assertThat(optionRepository.findById(optionId).orElseThrow().getQuantity())
                        .isEqualTo(8);
                    softly.assertThat(memberRepository.findById(memberId).orElseThrow().getPoint())
                        .isEqualTo(80_000);
                    softly.assertThat(orderRepository.findByMemberId(memberId, Pageable.unpaged()).getTotalElements())
                        .isEqualTo(1);
                    softly.assertThat(wishRepository.findByMemberIdAndProductId(memberId, productId))
                        .isEmpty();
                });
            }

            @Test
            @DisplayName("주문 생성 이벤트가 발행된다.")
            void publishesOrderCreatedEvent() {
                // given
                final Member member = memberRepository.findById(memberId).orElseThrow();
                member.chargePoint(100_000);

                // when
                orderFacade.createOrder(memberId, new OrderRequest(optionId, 1, "선물입니다"));

                // then
                assertThat(applicationEvents.stream(OrderCreatedEvent.class)).hasSize(1);
            }

            @Test
            @DisplayName("위시리스트가 없어도 주문이 성공한다.")
            void createsOrderWithoutWish() {
                // given
                final Member member = memberRepository.findById(memberId).orElseThrow();
                member.chargePoint(100_000);
                wishRepository.deleteByMemberIdAndProductId(memberId, productId);

                // when & then
                assertThatNoException().isThrownBy(() ->
                    orderFacade.createOrder(memberId, new OrderRequest(optionId, 1, "선물입니다"))
                );
            }
        }

        @Nested
        @DisplayName("실패하면,")
        class WhenFailed {

            @Test
            @DisplayName("존재하지 않는 옵션이면 예외가 발생한다.")
            void throwsWhenOptionNotFound() {
                // when & then
                assertThatThrownBy(() ->
                    orderFacade.createOrder(memberId, new OrderRequest(999L, 1, "선물입니다"))
                ).isInstanceOf(NoSuchElementException.class);
            }

            @Test
            @DisplayName("재고가 부족하면 예외가 발생한다.")
            void throwsWhenStockInsufficient() {
                // given
                final Member member = memberRepository.findById(memberId).orElseThrow();
                member.chargePoint(100_000);

                // when & then
                assertThatThrownBy(() ->
                    orderFacade.createOrder(memberId, new OrderRequest(optionId, 100, "선물입니다"))
                ).isInstanceOf(IllegalArgumentException.class);
            }

            @Test
            @DisplayName("포인트가 부족하면 예외가 발생한다.")
            void throwsWhenPointInsufficient() {
                // when & then
                assertThatThrownBy(() ->
                    orderFacade.createOrder(memberId, new OrderRequest(optionId, 1, "선물입니다"))
                ).isInstanceOf(IllegalArgumentException.class);
            }
        }
    }
}
