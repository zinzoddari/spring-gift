# ADR-003: Kakao 클라이언트 추상화 구조

## 날짜
2026-05-14

## 상태
`결정됨`

---

## 상황

ADR-002에서 Kakao 클라이언트를 `gift.infra.kakao`로 분리했다.
현재 `KakaoLoginClient`와 `KakaoMessageClient`는 HTTP 호출과 도메인 동작(토큰 교환, 메시지 포맷팅)을 한 클래스에서 담당한다.

컨트롤러가 이 클라이언트를 직접 주입받기 때문에:
- HTTP 통신 세부사항과 도메인 동작 로직이 분리되지 않음
- 컨트롤러가 Kakao API 구조(엔드포인트, 파라미터 형식)를 알아야 함
- Kakao를 다른 외부 서비스로 교체하거나 테스트 시 대체하기 어려움

---

## 선택지

| 선택지 | 설명 | 장점 | 단점 |
|--------|------|------|------|
| A. 현행 유지 | KakaoLoginClient, KakaoMessageClient 직접 사용 | 변경 없음 | HTTP 세부사항과 도메인 동작 혼재, 컨트롤러가 Kakao에 강결합 |
| B. KakaoClient + Adapter 분리 | 순수 HTTP 래퍼(KakaoClient) + 동작별 Adapter | 레이어 분리, 구조 명확, 인터페이스 없이 가볍게 구현 가능 | 클래스 수 증가 |
| C. Port 인터페이스 + Adapter | 도메인에 인터페이스(Port) 정의, infra에 구현체 | 테스트 대역 교체 용이, 완전한 의존성 역전 | 현재 규모 대비 과도한 추상화 |

---

## 결정

**B를 선택한다. KakaoClient(HTTP 래퍼)와 Adapter(도메인 동작)로 분리한다.**

인터페이스 없이 구체 클래스로 구성하며, 테스트 필요성이 생기면 그때 Port 인터페이스를 도입한다.

```
gift/infra/kakao/
  KakaoClient.java          # HTTP만 담당: post(), get() — RestClient 래퍼
  KakaoLoginAdapter.java    # 로그인 동작: exchangeToken(), getUserInfo()
  KakaoMessageAdapter.java  # 메시지 동작: sendOrderNotification()
```

**의존 방향:**
```
KakaoAuthController  →  KakaoLoginAdapter  →  KakaoClient
OrderController      →  KakaoMessageAdapter →  KakaoClient
```

KakaoClient는 인증 헤더, Content-Type 등 Kakao 공통 HTTP 규약만 처리한다.
Adapter는 KakaoClient를 주입받아 구체적인 API 호출과 응답 매핑을 담당한다.

---

## 트레이드오프

감수하는 것: 클래스 수 증가(2개 → 3개), KakaoLoginAdapter·KakaoMessageAdapter가 여전히 구체 클래스에 의존

얻는 것: HTTP 세부사항이 KakaoClient에 캡슐화됨, Adapter가 도메인 동작 단위로 응집됨,
컨트롤러가 Kakao API 구조를 직접 알 필요 없음, 나중에 Port 인터페이스 도입 시 Adapter만 implements 추가하면 됨

---

## 결과

- `KakaoLoginClient` → `KakaoClient` + `KakaoLoginAdapter`로 분해
- `KakaoMessageClient` → `KakaoClient` + `KakaoMessageAdapter`로 분해
- `KakaoAuthController`가 주입받는 대상: `KakaoLoginClient` → `KakaoLoginAdapter`
- `OrderController`가 주입받는 대상: `KakaoMessageClient` → `KakaoMessageAdapter`
- `KakaoLoginProperties`는 `KakaoClient` 또는 `KakaoLoginAdapter`가 보유 (엔드포인트·인증 정보)
