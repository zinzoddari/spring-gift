package gift.infra.client.kakao;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;

@Component
public class KakaoMessageAdapter {

    private final KakaoClient kakaoClient;

    public KakaoMessageAdapter(@Qualifier("kakaoApiClient") final KakaoClient kakaoClient) {
        this.kakaoClient = kakaoClient;
    }

    public void sendToMe(
        final String accessToken,
        final String productName,
        final int productPrice,
        final String optionName,
        final int quantity,
        final String message
    ) {
        final LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("template_object", buildTemplate(productName, productPrice, optionName, quantity, message));
        kakaoClient.postVoid(KakaoPath.SEND_MESSAGE.path(), accessToken, params);
    }

    private String buildTemplate(
        final String productName,
        final int productPrice,
        final String optionName,
        final int quantity,
        final String message
    ) {
        final String totalPrice = String.format("%,d", productPrice * quantity);
        final String messageText = message != null && !message.isBlank()
            ? "\\n\\n💌 " + message
            : "";
        return """
            {
                "object_type": "text",
                "text": "🎁 선물이 도착했어요!\\n\\n%s (%s)\\n수량: %d개\\n금액: %s원%s",
                "link": {},
                "button_title": "선물 확인하기"
            }
            """.formatted(productName, optionName, quantity, totalPrice, messageText);
    }
}
