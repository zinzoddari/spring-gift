# ADR-005: Spring Security 도입 여부

## 날짜
2026-05-23

## 상태
`결정됨` (2026-05-24 구현 완료)

---

## 상황

현재 인증은 `AuthenticationResolver`가 `Authorization` 헤더에서 JWT를 직접 파싱해 `Member`를 반환하는 방식으로 구현되어 있다.
각 컨트롤러 메서드에서 `member == null` 체크를 직접 수행하고 있어, 인증 로직이 HTTP 레이어 전반에 분산되어 있다.
Spring Security 도입을 통해 이 책임을 필터 체인으로 일원화할 수 있는지 검토한다.

---

## 선택지

| 선택지 | 장점 | 단점 |
|--------|------|------|
| A. Spring Security 도입 | 인증/인가 로직 필터 체인 일원화, `@PreAuthorize` 등 선언적 인가, CSRF·세션·CORS 설정 표준화, Spring 생태계(OAuth2 Client 등) 연동 용이 | 러닝커브, 설정 복잡도 증가, 현재 JWT 방식을 `SecurityContextHolder` 기반으로 전면 재설계 필요 |
| B. 현행 유지 (`AuthenticationResolver` + 컨트롤러 직접 체크) | 단순하고 코드 범위가 작음, 외부 의존 없음 | 인증 누락 가능성(컨트롤러마다 null 체크 반복), 인가 로직 추가 시 확장 어려움, 공통 처리(`HandlerInterceptor` 등) 직접 구현 필요 |
| C. `HandlerMethodArgumentResolver` 기반 자체 인증 | Security 없이 인증을 한 곳에서 처리 가능, 컨트롤러 메서드 시그니처가 깔끔해짐 | 인가·CORS 등 부가 기능은 여전히 직접 구현, 결국 Security의 부분 재구현이 됨 |

---

## 결정

**C안 채택 — `MemberArgumentResolver` 구현**

현재 API 범위가 작고 인가 요구사항(역할 기반 접근 제어 등)이 명확하지 않아, C안으로 컨트롤러 null 체크를 먼저 제거한다.
인가 요구사항이 생기는 시점에 A안(Spring Security)으로 전환한다.

---

## 트레이드오프

감수하는 것: 당장 Security를 도입하지 않으면 인가 기능 추가 시 재작업 발생
얻는 것: 현재 복잡도를 낮게 유지하고, 요구사항이 확정된 시점에 올바른 범위로 도입 가능

---

## 구현 내용 (2026-05-24)

- `MemberArgumentResolver` 추가: `HandlerMethodArgumentResolver` 구현체, `Authorization` 헤더에서 `Member`를 resolve하고 인증 실패 시 `UnauthorizedException` throw
- `WebMvcConfig` 추가: `MemberArgumentResolver`를 Spring MVC에 등록
- `UnauthorizedException` 추가: `GlobalExceptionHandler`에서 401 응답으로 매핑
- `WishController`, `OrderController`에서 `@RequestHeader Authorization`, `extractMember()` 호출, null 체크 제거 → 메서드 파라미터 `Member member`로 대체

## 결과

- 단기: ✅ `MemberArgumentResolver`로 인증 처리 일원화, 컨트롤러 null 체크 제거 완료
- 장기: 역할 기반 인가가 필요해지는 시점에 Spring Security + `JwtAuthenticationFilter` 방식으로 전환
