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

## 2026-05-23

### 작업: application.properties → application.yaml 전환
- 요청: properties 파일을 yaml로 전환
- 결과: `src/main/resources/application.yaml` 생성, `application.properties` 삭제

### 작업: KakaoClientProvider 도입 및 KakaoPath enum 추가
- 요청: KakaoClient를 base URL별로 Bean으로 분리하는 Provider/Factory 방식 도입
- 결과:
    - `KakaoClient`: `@Component` 제거, `RestClient.Builder` 대신 `RestClient` 직접 주입받도록 변경
    - `KakaoClientProvider`: `@Configuration` — `ObjectProvider<RestClient.Builder>`로 fresh 빌더를 가져와 `kakaoAuthClient`(kauth.kakao.com), `kakaoApiClient`(kapi.kakao.com) 두 Bean 등록
    - `KakaoPath`: URL 경로 enum — `OAUTH_TOKEN`, `USER_ME`, `SEND_MESSAGE`
    - `KakaoLoginClient`: `@Qualifier`로 authClient/apiClient 주입, `KakaoPath` 사용
    - 테스트: `RestClient.builder().baseUrl(mockWebServer.url("/"))` 패턴으로 업데이트
- 근거: 클라이언트 코드에서 base URL 조합 책임 제거, 경로 상수 중앙화

### 작업: KakaoClient ObjectMapper 제거
- 요청: RestClient 자체 역직렬화 기능 활용
- 결과: `ObjectMapper` 및 `deserialize()` 완전 제거, `TypeReference` → `ParameterizedTypeReference`로 교체해 RestClient가 직접 역직렬화

### 작업: KakaoAuthFacade 계층 추가
- 요청: KakaoAuthService에 혼재된 Adapter/Repository/JwtProvider 의존 분리
- 결과:
    - `KakaoAuthFacade`: 오케스트레이션 담당 — KakaoLoginAdapter + KakaoAuthService + JwtProvider + KakaoLoginProperties 조합, `loginUrl()` / `login(code)` 제공
    - `KakaoAuthService`: 회원 도메인만 — `findOrRegister(email, kakaoToken)`, MemberRepository만 의존
    - `KakaoAuthController`: KakaoAuthFacade 주입으로 교체
    - `KakaoAuthFacadeTest` (신규), `KakaoAuthServiceTest` (단순화), `KakaoAuthControllerTest` (Facade mock으로 교체)
- 근거: 오케스트레이션과 도메인 로직 분리

### 작업: KakaoAuthController 서비스 계층 추출 및 테스트
- 요청: KakaoAuthController 비즈니스 로직을 KakaoAuthService로 분리, 테스트 작성
- 결과:
    - `KakaoAuthService`: `loginUrl()`, `login(code)` — KakaoLoginProperties/KakaoLoginAdapter/MemberRepository/JwtProvider 의존
    - `KakaoAuthController`: KakaoAuthService만 주입, 컨트롤러는 HTTP 레이어만 담당
    - `KakaoAuthControllerTest`: KakaoAuthService 단일 mock으로 단순화
    - `KakaoAuthServiceTest`: `@ExtendWith(MockitoExtension)` 순수 단위 테스트, loginUrl/login 신규·기존 회원 케이스

### 작업: KakaoAuthController 테스트 작성
- 요청: KakaoAuthController 테스트 코드 작성 (BDD 스타일, KakaoClientTest 형식)
- 결과: `gift/auth/api/KakaoAuthControllerTest.java` — `@WebMvcTest` + `@MockitoBean`, `@TestConfiguration`으로 `KakaoLoginProperties` 제공, `/login` 리다이렉트 / `/callback` 신규·기존 회원 케이스

### 작업: KakaoLoginClient → KakaoLoginAdapter 이름 변경
- 요청: KakaoLoginClient를 KakaoLoginAdapter로 rename
- 결과: `KakaoLoginClient.java` → `KakaoLoginAdapter.java`, `KakaoLoginClientTest.java` → `KakaoLoginAdapterTest.java`, `KakaoAuthController` import 수정

### 작업: KakaoAuthService.login() @Transactional 추가 및 String 반환
- 요청: login()에 @Transactional 추가, Member 대신 email(String) 반환
- 결과: `@Transactional` 적용, `return member.getEmail()`로 변경, `KakaoAuthFacade` / `KakaoAuthFacadeTest` 연동 수정

### 작업: Member.updateKakaoAccessToken → applyKakaoToken 이름 변경
- 요청: update 접두어 대신 의미가 명확한 이름으로 변경
- 결과: `Member.applyKakaoToken()`, `KakaoAuthService` 호출부 수정

### 작업: KakaoMessageClient → KakaoMessageAdapter 이름 변경 및 KakaoClient 리팩토링
- 요청: KakaoMessageClient를 KakaoMessageAdapter로 rename, KakaoLoginAdapter처럼 KakaoClient 사용
- 결과:
    - `KakaoMessageClient.java` → `KakaoMessageAdapter.java`, `KakaoMessageClientTest.java` → `KakaoMessageAdapterTest.java`
    - `KakaoLoginProperties` + `RestClient.Builder` 의존 제거 → `@Qualifier("kakaoApiClient") KakaoClient` 주입
    - `postVoid(KakaoPath.SEND_MESSAGE.path(), ...)` 사용
    - `OrderController` import 수정
    - 테스트: `new KakaoClient(RestClient.builder().baseUrl(baseUrl).build())` 패턴으로 업데이트

### 작업: ProductController 리팩토링 (ProductService 계층 분리)
- 요청: ProductController 비즈니스 로직을 ProductService로 분리, 테스트 코드 작성
- 결과:
    - `ProductService`: `getProducts(Pageable)` / `getProduct(id)` / `createProduct(request)` / `updateProduct(id, request)` / `deleteProduct(id)` — `@Transactional(readOnly = true)` / `@Transactional` 경계 적용
    - `ProductController`: ProductService만 의존, HTTP 레이어만 담당
    - `ProductServiceTest`: `@ExtendWith(MockitoExtension)` 순수 단위 테스트
    - `ProductControllerTest`: `@WebMvcTest` + `@MockitoBean(ProductService)`, 전 CRUD 케이스
- 근거: 서비스 계층 분리, 트랜잭션 경계 명확화

### 작업: PageResponse<T> — Page JSON 구조 완전 일치
- 요청: `ProductService.getProducts()`가 `Page<ProductResponse>` 대신 DTO 반환, Spring의 Page JSON과 완전 동일하게
- 결과:
    - `gift.common.dto.PageResponse<T>`: `content`, `pageable`(PageableResponse), `totalElements`, `totalPages`, `number`, `size`, `numberOfElements`, `sort`(SortResponse), `first`, `last`, `empty` 11개 필드 — Spring `Page` Jackson 직렬화와 동일한 구조
    - `PageableResponse`: `pageNumber`, `pageSize`, `offset`, `sort`, `paged`, `unpaged` — `isUnpaged()` 케이스(offset 등 UnsupportedOperationException) 방어 처리
    - `SortResponse`: `empty`, `sorted`, `unsorted`
    - `PageResponse.from(Page<T>)` 정적 팩토리 메서드
- 근거: 서비스 레이어에서 Spring 타입 노출 방지, 클라이언트 응답 형식 변경 없이 DTO로 교체

### 작업: KakaoLoginClient → KakaoClient 리팩토링
- 요청: KakaoLoginClient가 RestClient를 직접 사용하던 것을 KakaoClient 래퍼로 교체
- 결과:
    - `KakaoLoginClient`: 생성자를 `RestClient.Builder` → `KakaoClient`로 변경, `requestAccessToken`/`requestUserInfo` 내부 RestClient 호출을 `kakaoClient.post()` / `kakaoClient.get()`으로 교체
    - `KakaoLoginClientTest`: setUp에서 `KakaoClient(RestClient.builder(), new ObjectMapper())`를 직접 생성해 주입
- 근거: KakaoClient HTTP 래퍼를 통해 직렬화/역직렬화 정책 일원화. KakaoLoginAdapter 전환 전 중간 단계

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
