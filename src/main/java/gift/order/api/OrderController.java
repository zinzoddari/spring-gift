package gift.order.api;

import gift.common.dto.PageResponse;
import gift.member.dto.MemberInfo;
import gift.order.dto.OrderRequest;
import gift.order.dto.OrderResponse;
import gift.order.service.OrderFacade;
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

    private final OrderFacade orderFacade;

    public OrderController(final OrderFacade orderFacade) {
        this.orderFacade = orderFacade;
    }

    @GetMapping
    public PageResponse<OrderResponse> getOrders(
        final MemberInfo memberInfo,
        final Pageable pageable
    ) {
        return orderFacade.getOrders(memberInfo.id(), pageable);
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
        final MemberInfo memberInfo,
        @Valid @RequestBody final OrderRequest request
    ) {
        final OrderResponse response = orderFacade.createOrder(memberInfo.id(), request);
        return ResponseEntity.created(URI.create("/api/orders/" + response.id()))
            .body(response);
    }
}
