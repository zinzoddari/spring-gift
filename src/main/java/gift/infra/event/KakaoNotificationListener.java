package gift.infra.event;

import gift.infra.client.kakao.KakaoMessageAdapter;
import gift.order.event.OrderCreatedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class KakaoNotificationListener {

    private final KakaoMessageAdapter kakaoMessageAdapter;

    public KakaoNotificationListener(final KakaoMessageAdapter kakaoMessageAdapter) {
        this.kakaoMessageAdapter = kakaoMessageAdapter;
    }

    @TransactionalEventListener
    public void on(final OrderCreatedEvent event) {
        if (event.kakaoAccessToken() == null) {
            return;
        }

        try {
            kakaoMessageAdapter.sendToMe(
                event.kakaoAccessToken(),
                event.productName(),
                event.productPrice(),
                event.optionName(),
                event.quantity(),
                event.message()
            );
        } catch (Exception ignored) {
        }
    }
}
