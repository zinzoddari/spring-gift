package gift.infra.client.kakao;

import gift.order.domain.Order;
import gift.product.domain.Product;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;

@Component
public class KakaoMessageAdapter {
    private final KakaoClient kakaoClient;

    public KakaoMessageAdapter(@Qualifier("kakaoApiClient") final KakaoClient kakaoClient) {
        this.kakaoClient = kakaoClient;
    }

    public void sendToMe(String accessToken, Order order, Product product) {
        LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("template_object", buildTemplate(order, product));

        kakaoClient.postVoid(KakaoPath.SEND_MESSAGE.path(), accessToken, params);
    }

    private String buildTemplate(Order order, Product product) {
        String totalPrice = String.format("%,d", product.getPrice() * order.getQuantity());
        String message = order.getMessage() != null && !order.getMessage().isBlank()
            ? "\\n\\n💌 " + order.getMessage()
            : "";
        return """
            {
                "object_type": "text",
                "text": "🎁 선물이 도착했어요!\\n\\n%s (%s)\\n수량: %d개\\n금액: %s원%s",
                "link": {},
                "button_title": "선물 확인하기"
            }
            """.formatted(
            product.getName(),
            order.getOption().getName(),
            order.getQuantity(),
            totalPrice,
            message
        );
    }
}
