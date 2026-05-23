package gift.infra.event;

import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import gift.infra.client.kakao.KakaoMessageAdapter;
import gift.order.event.OrderCreatedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("KakaoNotificationListener")
@ExtendWith(MockitoExtension.class)
class KakaoNotificationListenerTest {

    @Mock
    private KakaoMessageAdapter kakaoMessageAdapter;

    @InjectMocks
    private KakaoNotificationListener listener;

    private OrderCreatedEvent event(final String token) {
        return new OrderCreatedEvent(token, "상품A", 10_000, "옵션A", 2, "선물입니다");
    }

    @Nested
    @DisplayName("주문 생성 이벤트를 처리할 때,")
    class On {

        @Nested
        @DisplayName("성공하면,")
        class WhenSuccess {

            @Test
            @DisplayName("카카오 토큰이 있으면 알림을 전송한다.")
            void sendsNotificationWhenTokenExists() {
                // when
                listener.on(event("kakao-token"));

                // then
                verify(kakaoMessageAdapter).sendToMe("kakao-token", "상품A", 10_000, "옵션A", 2, "선물입니다");
            }

            @Test
            @DisplayName("카카오 토큰이 없으면 알림을 전송하지 않는다.")
            void skipsNotificationWhenTokenIsNull() {
                // when
                listener.on(event(null));

                // then
                verifyNoInteractions(kakaoMessageAdapter);
            }

            @Test
            @DisplayName("알림 전송이 실패해도 예외가 전파되지 않는다.")
            void doesNotPropagateException() {
                // given
                willThrow(new RuntimeException("카카오 오류"))
                    .given(kakaoMessageAdapter).sendToMe("kakao-token", "상품A", 10_000, "옵션A", 2, "선물입니다");

                // when & then (예외가 전파되지 않음)
                listener.on(event("kakao-token"));
            }
        }
    }
}
