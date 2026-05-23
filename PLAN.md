# PLAN

## 지금

## 완료
- [x] CategoryController 리팩토링 (CategoryService 계층 분리, 트랜잭션 경계)
- [x] 주문 생성 시 위시리스트 자동 제거 (WishService.removeWishByProduct, deleteByMemberIdAndProductId)
- [x] 주문 카카오 알림 이벤트 기반 처리 (@TransactionalEventListener, OrderCreatedEvent, KakaoNotificationListener)
- [x] ADR-009 카카오 알림 트랜잭션 외부 처리 결정 (C안: @TransactionalEventListener)
- [x] OrderController 서비스 계층 분리 (OrderService, OrderFacade, 트랜잭션 경계)
- [x] OrderFacade.createOrder Javadoc 한글 작성
- [x] OptionService / OptionFacade 계층 분리 (Controller → Facade → Service, ADR-008)
- [x] OptionController 리팩토링 (OptionFacade 위임, final, 코드 정리)
- [x] OptionControllerTest / OptionServiceTest 작성
- [x] ADR-008 서비스 간 직접 참조 금지 — Facade로 조합
- [x] ProductService.findProduct() 통합 (getProduct 제거, ProductResponse 반환)
- [x] WishController 리팩토링 (WishService 계층 분리, PageResponse, 트랜잭션 경계)
- [x] MemberArgumentResolver 도입 (HandlerMethodArgumentResolver 기반 인증 일원화, ADR-005)
- [x] MemberInfo DTO 추가 (엔티티 대신 DTO를 ArgumentResolver에서 반환)
- [x] GlobalExceptionHandler 예외 핸들러 확장 (SecurityException → 403, UnauthorizedException → 401)
- [x] KakaoAuthFacade 계층 추가 (KakaoAuthService 오케스트레이션/도메인 분리)
- [x] ProductController 리팩토링 (ProductService 계층 분리, PageResponse<T> DTO, 트랜잭션 경계 추가)
- [x] PageResponse<T> — Page JSON 구조 완전 일치 (pageable, sort, numberOfElements 포함)
- [x] MemberController 리팩토링 (MemberService 계층 분리, 글로벌 예외 핸들러 추출, 트랜잭션 경계 추가)
- [x] Member 팩토리 메서드 추가 (withEmail, withCredentials), matchesPassword() 도입
- [x] GlobalExceptionHandler gift.infra.exception으로 분리 (ADR-006)
- [x] JwtProvider gift.infra.jwt 패키지로 이동, @Autowired 제거
- [x] KakaoAuthController 리팩토링 (KakaoAuthFacade + KakaoAuthService 계층 분리)
- [x] KakaoMessageClient → KakaoMessageAdapter 이름 변경 및 KakaoClient 의존으로 리팩토링
- [x] Member.updateKakaoAccessToken → applyKakaoToken 이름 변경
- [x] KakaoAuthService.login() @Transactional 추가, String(email) 반환으로 변경
- [x] application.properties → application.yaml 전환
- [x] KakaoClientProvider 도입 — base URL 별 KakaoClient Bean 분리 (kakaoAuthClient / kakaoApiClient)
- [x] KakaoPath enum 추가 — 경로 상수 관리
- [x] KakaoLoginClient → authClient / apiClient 주입
- [x] KakaoClient — ObjectMapper 제거, ParameterizedTypeReference로 RestClient 직접 역직렬화
- [x] KakaoLoginClient → KakaoLoginAdapter 이름 변경 및 KakaoAuthController 의존 수정
- [x] KakaoLoginClient → KakaoClient 의존으로 리팩토링
- [x] ADR 템플릿 작성
- [x] ARCHITECTURE.md 작성
- [x] ADR-001 패키지 구조 결정
- [x] 패키지 구조 변경 (api/view 서브 패키지, ADR-001)
- [x] repository/ 서브 패키지 이동
- [x] dto/ 서브 패키지 이동 (*Request, *Response)
- [x] Client 역할 infra로 분리하기 (gift.infra.client.kakao, ADR-002)
- [x] RestClient 타임아웃 설정 (RestClientConfig — connectTimeout 3s / readTimeout 5s)
- [x] ADR-003 Kakao 클라이언트 추상화 구조 결정
- [x] ADR-004 HTTP 클라이언트 테스트 전략 결정 (MockWebServer)
- [x] KakaoClient 작성 — TypeReference 기반 제네릭 HTTP 래퍼, 테스트 포함
- [x] KakaoLoginClient, KakaoMessageClient 테스트 작성
