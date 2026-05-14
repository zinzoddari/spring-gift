# AI 활용 로그

---

## 2026-05-14

### 작업: repository/ 서브 패키지 이동
- 요청: 각 도메인 하위 *Repository.java를 repository/ 서브 패키지로 이동
- 결과: `gift/*/repository/*Repository.java` 위치로 이동, import 경로 수정

### 작업: dto/ 서브 패키지 이동
- 요청: 각 도메인 api/ 하위 *Request.java, *Response.java를 dto/ 서브 패키지로 이동
- 결과: `gift/*/dto/*Request.java`, `gift/*/dto/*Response.java` 위치로 이동, 컨트롤러 import 경로 수정

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
