# PLAN

## 지금
- [ ] KakaoLoginAdapter, KakaoMessageAdapter 작성 (ADR-003)
- [ ] 기존 KakaoLoginClient, KakaoMessageClient 제거 및 컨트롤러 교체
- [ ] KakaoLoginAdapterTest, KakaoMessageAdapterTest 작성

## 다음
- [ ] Resolver를 별도 클래스로 할지, Validate를 이용할지 고민하고 적용하기

## 할 것
- [ ] 테스트 환경 설정
- [ ] 스타일 정리
- [ ] 서비스 계층 추출
- [ ] 트랜잭션 경계 추가
- [ ] 누락 작동 구현
- [ ] 도메인 클래스 doamin 패키지로 이동하기

## 완료
- [x] ADR 템플릿 작성
- [x] ARCHITECTURE.md 작성
- [x] ADR-001 패키지 구조 결정
- [x] 패키지 구조 변경 (api/view 서브 패키지, ADR-001)
- [x] repository/ 서브 패키지 이동
- [x] dto/ 서브 패키지 이동 (*Request, *Response)
- [x] Client 역할 infra로 분리하기 (gift.infra.kakao, ADR-002)
- [x] RestClient 타임아웃 설정 (RestClientConfig — connectTimeout 3s / readTimeout 5s)
- [x] ADR-003 Kakao 클라이언트 추상화 구조 결정
- [x] ADR-004 HTTP 클라이언트 테스트 전략 결정 (MockWebServer)
- [x] KakaoClient 작성 — TypeReference 기반 제네릭 HTTP 래퍼, 테스트 포함
- [x] KakaoLoginClient, KakaoMessageClient 테스트 작성
