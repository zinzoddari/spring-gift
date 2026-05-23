package gift.order.service;

import gift.common.dto.PageResponse;
import gift.option.domain.Option;
import gift.order.domain.Order;
import gift.order.dto.OrderResponse;
import gift.order.repository.OrderRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(final OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getOrders(final Long memberId, final Pageable pageable) {
        return PageResponse.from(orderRepository.findByMemberId(memberId, pageable).map(OrderResponse::from));
    }

    @Transactional
    public Order save(final Option option, final Long memberId, final int quantity, final String message) {
        return orderRepository.save(new Order(option, memberId, quantity, message));
    }
}
