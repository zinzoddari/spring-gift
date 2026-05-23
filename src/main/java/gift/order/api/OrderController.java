package gift.order.api;

import gift.infra.kakao.KakaoMessageAdapter;
import gift.order.Order;
import gift.order.dto.OrderRequest;
import gift.order.dto.OrderResponse;
import gift.order.repository.OrderRepository;
import gift.member.domain.Member;
import gift.member.dto.MemberInfo;
import gift.member.repository.MemberRepository;
import java.util.NoSuchElementException;
import gift.option.Option;
import gift.option.repository.OptionRepository;
import gift.wish.repository.WishRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderRepository orderRepository;
    private final OptionRepository optionRepository;
    private final WishRepository wishRepository;
    private final MemberRepository memberRepository;
    private final KakaoMessageAdapter kakaoMessageClient;

    public OrderController(
        final OrderRepository orderRepository,
        final OptionRepository optionRepository,
        final WishRepository wishRepository,
        final MemberRepository memberRepository,
        final KakaoMessageAdapter kakaoMessageClient
    ) {
        this.orderRepository = orderRepository;
        this.optionRepository = optionRepository;
        this.wishRepository = wishRepository;
        this.memberRepository = memberRepository;
        this.kakaoMessageClient = kakaoMessageClient;
    }

    @GetMapping
    public ResponseEntity<?> getOrders(
        final MemberInfo memberInfo,
        final Pageable pageable
    ) {
        final org.springframework.data.domain.Page<OrderResponse> orders =
            orderRepository.findByMemberId(memberInfo.id(), pageable).map(OrderResponse::from);
        return ResponseEntity.ok(orders);
    }

    // order flow:
    // 1. validate option
    // 2. subtract stock
    // 3. deduct points
    // 4. save order
    // 5. cleanup wish
    // 6. send kakao notification
    @PostMapping
    public ResponseEntity<?> createOrder(
        final MemberInfo memberInfo,
        @Valid @RequestBody final OrderRequest request
    ) {
        // validate option
        final Option option = optionRepository.findById(request.optionId()).orElse(null);
        if (option == null) {
            return ResponseEntity.notFound().build();
        }

        // subtract stock
        option.subtractQuantity(request.quantity());
        optionRepository.save(option);

        // deduct points
        final Member member = memberRepository.findById(memberInfo.id())
            .orElseThrow(() -> new NoSuchElementException("회원을 찾을 수 없습니다."));
        final int price = option.getProduct().getPrice() * request.quantity();
        member.deductPoint(price);
        memberRepository.save(member);

        // save order
        final Order saved = orderRepository.save(new Order(option, memberInfo.id(), request.quantity(), request.message()));

        // best-effort kakao notification
        sendKakaoMessageIfPossible(member, saved, option);
        return ResponseEntity.created(URI.create("/api/orders/" + saved.getId()))
            .body(OrderResponse.from(saved));
    }

    private void sendKakaoMessageIfPossible(final Member member, final Order order, final Option option) {
        if (member.getKakaoAccessToken() == null) {
            return;
        }
        try {
            kakaoMessageClient.sendToMe(member.getKakaoAccessToken(), order, option.getProduct());
        } catch (Exception ignored) {
        }
    }
}
