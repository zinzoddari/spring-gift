package gift.order.event;

public record OrderCreatedEvent(
    String kakaoAccessToken,
    String productName,
    int productPrice,
    String optionName,
    int quantity,
    String message
) {
}
