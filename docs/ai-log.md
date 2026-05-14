# AI 활용 로그

---

## 2026-05-14

### 작업: repository/ 서브 패키지 이동
- 요청: 각 도메인 하위 *Repository.java를 repository/ 서브 패키지로 이동
- 결과: `gift/*/repository/*Repository.java` 위치로 이동, import 경로 수정

### 작업: Kakao 클라이언트 infra 패키지 분리 (ADR-002)
- 요청: KakaoLoginClient, KakaoLoginProperties, KakaoMessageClient를 도메인 패키지에서 분리
- 결과: `gift/infra/kakao/` 패키지로 이동, KakaoAuthController·OrderController import 수정

### 작업: dto/ 서브 패키지 이동
- 요청: 각 도메인 api/ 하위 *Request.java, *Response.java를 dto/ 서브 패키지로 이동
- 결과: `gift/*/dto/*Request.java`, `gift/*/dto/*Response.java` 위치로 이동, 컨트롤러 import 경로 수정

### 작업: RestClient 타임아웃 설정
- 요청: infra 패키지에 RestClientConfig 추가
- 결과: `gift/infra/RestClientConfig.java` 생성 — connectTimeout 3초, readTimeout 5초
- 근거:
    - **connectTimeout 3초**: Kakao 서버는 국내 클라우드 기반으로 네트워크 지연이 낮음. 3초 안에 TCP 연결이 맺어지지 않으면 서버 장애로 판단하고 빠르게 실패하는 것이 적절
    - **readTimeout 5초**: 토큰 발급(POST /oauth/token)과 메시지 전송(POST /talk/memo)은 단순 API 호출로 응답이 수백 ms 수준. 5초는 일시적인 지연을 허용하면서도 스레드가 무한정 대기하는 상황을 방지하는 상한
    - `RestClientCustomizer` 빈 방식 사용: Spring Boot 자동 구성된 `RestClient.Builder`에 자동 적용되므로 기존 클라이언트 코드 변경 없이 전역 설정 가능

---

## 2026-05-11

### 작업: ADR 템플릿 작성
- 요청: ADR 양식 초안 생성
- 결과: `docs/adr/ADR-000-template.md` 생성

### 작업: 프로젝트 전체 분석
- 요청: 패키지 구조, 각 클래스 역할, 주요 흐름 분석
- 결과: `docs/ARCHITECTURE.md` 작성 (엔티티 관계도, API 목록, 주문 생성 흐름, 현재 문제점 포함)

### 작업: 패키지 구조 ADR 작성
- 요청: 도메인 하위 api/view 서브 패키지 분리 결정 문서화
- 결정: 도메인 패키지 루트 유지, Controller/DTO는 api/ 또는 view/ 하위로 이동
- 결과: `docs/adr/ADR-001-패키지-구조.md` 작성
