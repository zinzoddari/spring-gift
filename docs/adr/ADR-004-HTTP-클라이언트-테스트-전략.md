# ADR-004: HTTP 클라이언트 테스트 전략

## 날짜
2026-05-14

## 상태
`결정됨`

---

## 상황

`KakaoLoginClient`, `KakaoMessageClient`는 RestClient로 외부 HTTP API를 호출한다.
이 클래스들을 테스트하려면 실제 Kakao 서버에 요청하지 않고 HTTP 응답을 제어할 수단이 필요하다.

두 가지 방식이 대표적이다.

---

## 선택지

| 선택지 | 설명 | 장점 | 단점 |
|--------|------|------|------|
| A. Mockito로 RestClient 목킹 | RestClient의 체이닝 API를 단계별로 `@Mock`으로 교체 | 추가 의존성 없음 | RestClient 체인(post→uri→header→body→retrieve→body)을 모두 스터빙해야 함. 내부 구현이 바뀌면 목 설정도 함께 깨짐 |
| B. MockWebServer (OkHttp) | 실제 HTTP 서버를 로컬에 띄워 요청·응답을 제어 | 실제 HTTP 레벨에서 검증. 클라이언트 구현 방식(RestClient/WebClient 등)이 바뀌어도 테스트 유지. 요청 URL·헤더·바디까지 검증 가능 | `com.squareup.okhttp3:mockwebserver` 의존성 1개 추가 필요 |

---

## 결정

**B를 선택한다. MockWebServer로 실제 HTTP 레벨에서 테스트한다.**

A 방식은 RestClient 내부 체이닝을 모두 스터빙해야 하므로 코드가 장황하고,
클라이언트를 RestClient → WebClient 등으로 교체하면 목 설정 전체가 무너진다.
B 방식은 "어떤 HTTP 클라이언트를 쓰든 이 URL로 이 요청을 보내면 이 응답이 온다"는 수준에서 검증하므로,
구현 변경에 강하고 요청 내용(헤더, 바디)까지 검증할 수 있다.

```groovy
// build.gradle.kts 추가
testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
```

---

## 트레이드오프

감수하는 것: 테스트 의존성 1개 추가, setUp/tearDown에서 서버 시작·종료 코드 필요

얻는 것: HTTP 요청 전체(URL, 메서드, 헤더, 바디)를 블랙박스로 검증 가능,
클라이언트 구현 교체 시 테스트 재작성 불필요, 실제 HTTP 오류 응답(4xx/5xx) 시뮬레이션 가능

---

## 결과

- `build.gradle.kts`에 `mockwebserver` 의존성 추가
- `KakaoLoginClient`의 하드코딩된 Kakao URL을 `KakaoLoginProperties`로 이동
  — MockWebServer URL로 교체할 수 있어야 테스트 가능
- 이후 `KakaoMessageClient` 테스트도 동일한 방식 적용
