package gift.order.api;

import gift.common.dto.PageResponse;
import gift.member.dto.MemberInfo;
import gift.order.dto.OrderRequest;
import gift.order.dto.OrderResponse;
import gift.order.service.OrderService;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
class OrderController {

    private final OrderService orderService;

    public OrderController(final OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<OrderResponse>> getOrders(
        final MemberInfo memberInfo,
        final Pageable pageable
    ) {
        return ResponseEntity.ok(orderService.getOrders(memberInfo.id(), pageable));
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
        final MemberInfo memberInfo,
        @Valid @RequestBody final OrderRequest request
    ) {
        final OrderResponse response = orderService.createOrder(memberInfo.id(), request);
        return ResponseEntity.created(URI.create("/api/orders/" + response.id()))
            .body(response);
    }
}
