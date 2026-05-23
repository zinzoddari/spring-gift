package gift.order.service;

import gift.infra.client.kakao.KakaoMessageAdapter;
import gift.member.domain.Member;
import gift.member.repository.MemberRepository;
import gift.option.domain.Option;
import gift.option.repository.OptionRepository;
import gift.order.Order;
import gift.order.dto.OrderRequest;
import gift.order.dto.OrderResponse;
import gift.common.dto.PageResponse;
import gift.order.repository.OrderRepository;
import java.util.NoSuchElementException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OptionRepository optionRepository;
    private final MemberRepository memberRepository;
    private final KakaoMessageAdapter kakaoMessageAdapter;

    public OrderService(
        final OrderRepository orderRepository,
        final OptionRepository optionRepository,
        final MemberRepository memberRepository,
        final KakaoMessageAdapter kakaoMessageAdapter
    ) {
        this.orderRepository = orderRepository;
        this.optionRepository = optionRepository;
        this.memberRepository = memberRepository;
        this.kakaoMessageAdapter = kakaoMessageAdapter;
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getOrders(final Long memberId, final Pageable pageable) {
        return PageResponse.from(orderRepository.findByMemberId(memberId, pageable).map(OrderResponse::from));
    }

    // order flow:
    // 1. validate option
    // 2. subtract stock
    // 3. deduct points
    // 4. save order
    // 5. cleanup wish
    // 6. send kakao notification
    @Transactional
    public OrderResponse createOrder(final Long memberId, final OrderRequest request) {
        // validate option
        final Option option = optionRepository.findById(request.optionId())
            .orElseThrow(() -> new NoSuchElementException("옵션을 찾을 수 없습니다."));

        // subtract stock
        option.subtractQuantity(request.quantity());
        optionRepository.save(option);

        // deduct points
        final Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new NoSuchElementException("회원을 찾을 수 없습니다."));
        final int price = option.getProduct().getPrice() * request.quantity();
        member.deductPoint(price);
        memberRepository.save(member);

        // save order
        final Order saved = orderRepository.save(new Order(option, memberId, request.quantity(), request.message()));

        // best-effort kakao notification
        sendKakaoMessageIfPossible(member, saved, option);
        return OrderResponse.from(saved);
    }

    private void sendKakaoMessageIfPossible(final Member member, final Order order, final Option option) {
        if (member.getKakaoAccessToken() == null) {
            return;
        }
        try {
            kakaoMessageAdapter.sendToMe(member.getKakaoAccessToken(), order, option.getProduct());
        } catch (Exception ignored) {
        }
    }
}
