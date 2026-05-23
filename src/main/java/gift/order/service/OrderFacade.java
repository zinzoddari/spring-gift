package gift.order.service;

import gift.common.dto.PageResponse;
import gift.infra.client.kakao.KakaoMessageAdapter;
import gift.member.domain.Member;
import gift.member.service.MemberService;
import gift.option.domain.Option;
import gift.option.service.OptionService;
import gift.order.domain.Order;
import gift.order.dto.OrderRequest;
import gift.order.dto.OrderResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderFacade {

    private final OptionService optionService;
    private final MemberService memberService;
    private final OrderService orderService;
    private final KakaoMessageAdapter kakaoMessageAdapter;

    public OrderFacade(
        final OptionService optionService,
        final MemberService memberService,
        final OrderService orderService,
        final KakaoMessageAdapter kakaoMessageAdapter
    ) {
        this.optionService = optionService;
        this.memberService = memberService;
        this.orderService = orderService;
        this.kakaoMessageAdapter = kakaoMessageAdapter;
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getOrders(final Long memberId, final Pageable pageable) {
        return orderService.getOrders(memberId, pageable);
    }

    /**
     * 주문 성공 및 내역을 저장합니다.
     *
     * <p>처리 순서:
     * <ol>
     *   <li>옵션 재고 차감 (옵션이 존재하지 않으면 예외 발생)</li>
     *   <li>회원 포인트 차감 (포인트 부족 시 예외 발생)</li>
     *   <li>주문 저장</li>
     *   <li>위시리스트에서 해당 상품 제거 (TODO: 미구현)</li>
     *   <li>카카오 알림 전송 (실패해도 주문에 영향 없음)</li>
     * </ol>
     */
    @Transactional
    public OrderResponse createOrder(final Long memberId, final OrderRequest request) {
        final Option option = optionService.subtractQuantity(request.optionId(), request.quantity());
        final Member member = memberService.deductPoint(memberId, option.calculatePrice(request.quantity()));
        final Order order = orderService.save(option, memberId, request.quantity(), request.message());
        // TODO: 위시리스트에 해당 상품이 있으면 제거
        sendKakaoIfPossible(member, order, option);
        return OrderResponse.from(order);
    }

    private void sendKakaoIfPossible(final Member member, final Order order, final Option option) {
        if (member.getKakaoAccessToken() == null) {
            return;
        }
        try {
            kakaoMessageAdapter.sendToMe(member.getKakaoAccessToken(), order, option.getProduct());
        } catch (Exception ignored) {
        }
    }
}
