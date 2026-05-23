# ADR-010: API 서비스와 뷰(어드민) 서비스 분리

## 날짜
2026-05-24

## 상태
`결정됨`

---

## 상황

`AdminMemberController`(Thymeleaf 기반 어드민 뷰)를 서비스 계층으로 분리할 때,
기존 `MemberService`를 그대로 재사용할 수 있는지 검토가 필요했다.

`MemberService`는 API 요청을 처리하기 위해 `JwtProvider`에 의존하고 있으며,
`register`, `login`, `deductPoint` 등 JWT 토큰 발급·검증 흐름을 중심으로 설계되어 있다.
어드민 뷰에서 필요한 `findAll`, `update`, `chargePoint` 등과 목적이 달라 함께 두기 어렵다.

---

## 선택지

| 선택지 | 장점 | 단점 |
|--------|------|------|
| A. 기존 `MemberService`에 어드민 메서드 추가 | 클래스 수 최소화 | JWT 불필요한 어드민 메서드와 섞임, 의존성 오염 |
| B. 어드민 전용 서비스(`AdminMemberService`) 별도 생성 | 책임 명확, 의존성 오염 없음 | 클래스 수 증가 |

---

## 결정

> **B를 선택한다. API용 서비스와 뷰(어드민)용 서비스를 분리한다.**

이유: API 서비스는 인증(JWT) 흐름을 포함하고, 뷰 서비스는 단순 CRUD + 도메인 로직 호출만 필요하다.
두 역할을 한 클래스에 두면 `JwtProvider` 의존이 어드민 흐름에도 끌려오고,
테스트 시 불필요한 mock이 늘어난다.

---

## 트레이드오프

감수하는 것: 도메인마다 서비스 클래스가 둘로 늘어날 수 있음  
얻는 것: 각 서비스의 의존성이 최소화되고, 테스트 setup이 단순해짐

---

## 결과

- `MemberService` — JWT 기반 인증(register, login, deductPoint)만 담당
- `AdminMemberService` — 어드민 뷰 전용 CRUD(getMembers, getMember, createMember, updateMember, chargePoint, deleteMember)
- `AdminMemberController`는 `AdminMemberService`만 주입받고, `MemberRepository`를 직접 참조하지 않음
- 다른 도메인도 어드민 뷰 컨트롤러가 생길 경우 동일한 패턴을 따른다
