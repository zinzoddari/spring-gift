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

## 2026-05-24

### 작업: 주문 생성 시 위시리스트 자동 제거
- 요청: `OrderFacade.createOrder`의 TODO 구현
- 결과:
    - `WishRepository.deleteByMemberIdAndProductId()` Spring Data derived delete 추가
    - `WishService.removeWishByProduct(memberId, productId)` 추가 — 위시 없어도 예외 없이 통과
    - `OrderFacade`: `WishService` 주입, TODO 교체
- 근거: 주문 완료 시 해당 상품 위시 자동 정리

### 작업: CategoryService 계층 분리 및 테스트
- 요청: CategoryController에서 Repository 직접 참조 제거, CategoryService 분리
- 결과:
    - `CategoryService`: `getCategories` / `createCategory` / `updateCategory` / `deleteCategory` — dirty checking으로 `updateCategory` 내 `save()` 제거
    - `CategoryController`: CategoryService만 의존
    - `CategoryServiceTest`: MockitoExtension 순수 단위 테스트

### 작업: AdminMemberController 리팩토링 및 테스트
- 요청: AdminMemberController에서 Repository 직접 참조 제거, 서비스 분리, 테스트 작성
- 결과:
    - `AdminMemberService`: `getMembers` / `getMember` / `createMember` / `updateMember` / `chargePoint` / `deleteMember` — dirty checking 적용
    - `AdminMemberResponse` DTO: `id`, `email`, `password`, `point` — edit.html `th:value="${member.password}"` 호환
    - `AdminMemberController`: AdminMemberService만 의존, `create()` 이메일 중복 시 try-catch로 폼 재렌더링
    - `AdminMemberControllerTest`: `@WebMvcTest` + `@MockitoBean`
    - `AdminMemberServiceTest`: MockitoExtension 순수 단위 테스트
- 결정: API 서비스(`MemberService`, JwtProvider 의존)와 뷰 서비스(`AdminMemberService`, CRUD 전용) 분리 → ADR-010

### 작업: AdminProductController 리팩토링 및 테스트
- 요청: AdminProductController에서 Repository 직접 참조 제거, 서비스 분리, 테스트 작성
- 결과:
    - `AdminProductService`: `getProducts` / `getCategories` / `getProduct` / `createProduct` / `updateProduct` / `deleteProduct` — private `findProduct()` 헬퍼 분리
    - `AdminProductController`: AdminProductService만 의존, `ProductNameValidator`는 컨트롤러에 유지 (순수 함수, 뷰 라우팅 결정)
    - `AdminProductControllerTest`: `@WebMvcTest` + `@MockitoBean`

### 작업: Entity @Column / @Table 명시
- 요청: V1 SQL 스키마 참조하여 모든 엔티티에 컬럼명, 타입(length), 제약(nullable, unique) 명시
- 결과: `Category` / `Product` / `Member` / `Wish` / `Order` — `@Table(name)`, `@Column(name, nullable, length, unique)` 전면 추가, `Option`은 기존 유지

### 작업: OptionFacadeTest / AuthenticationResolverTest 작성
- 요청: 각 클래스 단위 테스트 작성
- 결과:
    - `OptionFacadeTest`: getOptions / createOption / deleteOption 성공·실패 케이스
    - `AuthenticationResolverTest`: 유효 토큰 / 회원 없음 / 유효하지 않은 토큰 / null Authorization 케이스

### 작업: 재고 동시성 제어 — 낙관적 락 (@Version)
- 요청: 동시 주문 시 재고 음수 방지
- 결과:
    - `Option` 엔티티에 `@Version Long version` 추가
    - `V3__Add_version_to_options.sql`: `version bigint not null default 0` 컬럼 추가
    - `GlobalExceptionHandler`: `OptimisticLockException` / `ObjectOptimisticLockingFailureException` → 409 처리 (Spring이 JPA 예외를 래핑하므로 둘 다 필요)
    - `OptionOptimisticLockTest`: `@DataJpaTest` + `@Transactional(NOT_SUPPORTED)` + `TransactionTemplate`으로 충돌 시나리오 재현, `ObjectOptimisticLockingFailureException` 발생 검증
- 결정: ADR-011 — 낙관적 락 선택 (충돌 빈도 낮고 DB 락 오버헤드 없음)

### 작업: wish 테이블 유니크 제약 추가
- 요청: 동시 요청으로 인한 위시리스트 중복 저장 방지
- 결과: `V4__Add_unique_constraint_to_wish.sql` — `(member_id, product_id)` 복합 유니크 제약
- 결정: 엔티티 `@UniqueConstraint`는 추가하지 않음 — Flyway 환경에서는 마이그레이션이 진실의 원천

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
