# spring-gift 아키텍처 문서

## 프로젝트 개요

카카오 OAuth 인증 기반의 선물 쇼핑 플랫폼 REST API.
포인트로 상품을 구매하고, 주문 완료 시 카카오톡 알림을 전송한다.

- **Java 21**, **Spring Boot 3.5.9**
- **DB**: H2 (개발) / MySQL (운영), Flyway 마이그레이션
- **인증**: Kakao OAuth2 + JWT (Stateless)

---

## 패키지 구조

```
src/main/java/gift/
├── Application.java
├── auth/           # 인증 (JWT + Kakao OAuth)
├── member/         # 회원 관리
├── category/       # 상품 카테고리
├── product/        # 상품
├── option/         # 상품 옵션 (재고 단위)
├── wish/           # 위시리스트
└── order/          # 주문 처리
```

---

## 패키지별 역할

### `auth` — 인증

| 클래스 | 역할 |
|--------|------|
| `JwtProvider` | JWT 생성/파싱 (subject = email) |
| `AuthenticationResolver` | Authorization 헤더 → Member 객체 변환 |
| `KakaoAuthController` | Kakao OAuth 리다이렉트 + 콜백 처리 |
| `KakaoLoginClient` | Kakao API 호출 (토큰 발급, 사용자 정보 조회) |
| `KakaoLoginProperties` | Kakao 설정값 바인딩 (`kakao.login.*`) |
| `TokenResponse` | JWT 응답 DTO |

### `member` — 회원

| 클래스 | 역할 |
|--------|------|
| `Member` | 회원 엔티티. 포인트 충전/차감 도메인 로직 포함 |
| `MemberController` | 회원가입(`/register`), 로그인(`/login`) REST API |
| `AdminMemberController` | 관리자 회원 관리 (HTML 뷰) |
| `MemberRepository` | `findByEmail`, `existsByEmail` |
| `MemberRequest` / | 요청/응답 DTO |

### `category` — 카테고리

| 클래스 | 역할 |
|--------|------|
| `Category` | 카테고리 엔티티 (name, color, imageUrl, description) |
| `CategoryController` | CRUD REST API |
| `CategoryRepository` | JPA Repository |
| `CategoryRequest` / `CategoryResponse` | 요청/응답 DTO |

### `product` — 상품

| 클래스 | 역할 |
|--------|------|
| `Product` | 상품 엔티티. Category FK, Option 컬렉션 소유 |
| `ProductController` | CRUD + 페이징 REST API |
| `AdminProductController` | 관리자 상품 관리 (HTML 뷰) |
| `ProductRepository` | JPA Repository |
| `ProductNameValidator` | 이름 규칙 검증 (15자, 금지어, 허용 문자) |
| `ProductRequest` / `ProductResponse` | 요청/응답 DTO |

### `option` — 옵션 (재고 단위)

| 클래스 | 역할 |
|--------|------|
| `Option` | 옵션 엔티티. 재고(quantity) 관리, `subtractQuantity()` |
| `OptionController` | 상품별 옵션 CRUD (`/api/products/{id}/options`) |
| `OptionRepository` | `findByProductId`, `existsByProductIdAndName` |
| `OptionNameValidator` | 이름 규칙 검증 (50자, 허용 문자) |
| `OptionRequest` / `OptionResponse` | 요청/응답 DTO |

### `wish` — 위시리스트

| 클래스 | 역할 |
|--------|------|
| `Wish` | 위시 엔티티. memberId(Long FK), Product FK |
| `WishController` | 위시 조회/추가/삭제 REST API (인증 필요) |
| `WishRepository` | `findByMemberId`, `findByMemberIdAndProductId` |
| `WishRequest` / `WishResponse` | 요청/응답 DTO (상품 정보 비정규화) |

### `order` — 주문

| 클래스 | 역할 |
|--------|------|
| `Order` | 주문 엔티티. Option FK, memberId(Long FK) |
| `OrderController` | 주문 조회/생성 REST API (인증 필요) |
| `OrderRepository` | `findByMemberId` |
| `KakaoMessageClient` | 주문 완료 후 카카오톡 나에게 보내기 |
| `OrderRequest` / `OrderResponse` | 요청/응답 DTO |

---

## 엔티티 관계도

```
┌──────────┐       ┌──────────┐       ┌──────────┐
│ Category │ 1---N │ Product  │ 1---N │  Option  │
└──────────┘       └──────────┘       └──────────┘
                        │                   │
                        │ 1                 │ 1
                        N                   N
                   ┌──────────┐       ┌──────────┐
                   │   Wish   │       │  Order   │
                   └──────────┘       └──────────┘
                        │                   │
                     memberId           memberId
                        │                   │
                        └────────┬──────────┘
                                 │
                          ┌──────────┐
                          │  Member  │
                          └──────────┘
```

**JPA 관계 메모:**
- `Product` → `Option`: `@OneToMany cascade=ALL, orphanRemoval=true` (상품 삭제 시 옵션 자동 삭제)
- `Option` → `Product`: `@ManyToOne`
- `Product` → `Category`: `@ManyToOne` (cascade 없음)
- `Wish.memberId`, `Order.memberId`: Long 원시 FK (엔티티 참조 없음)

---

## API 엔드포인트

### 인증
| Method | Path | 설명 | 인증 |
|--------|------|------|------|
| GET | `/api/auth/kakao/login` | Kakao OAuth 리다이렉트 | - |
| GET | `/api/auth/kakao/callback` | OAuth 콜백 → JWT 반환 | - |
| POST | `/api/members/register` | 이메일 회원가입 → JWT | - |
| POST | `/api/members/login` | 이메일 로그인 → JWT | - |

### 카테고리
| Method | Path | 설명 |
|--------|------|------|
| GET | `/api/categories` | 전체 조회 |
| POST | `/api/categories` | 생성 |
| PUT | `/api/categories/{id}` | 수정 |
| DELETE | `/api/categories/{id}` | 삭제 |

### 상품
| Method | Path | 설명 |
|--------|------|------|
| GET | `/api/products` | 페이징 조회 |
| GET | `/api/products/{id}` | 단건 조회 |
| POST | `/api/products` | 생성 |
| PUT | `/api/products/{id}` | 수정 |
| DELETE | `/api/products/{id}` | 삭제 |

### 옵션
| Method | Path | 설명 |
|--------|------|------|
| GET | `/api/products/{id}/options` | 상품 옵션 목록 |
| POST | `/api/products/{id}/options` | 옵션 추가 |
| DELETE | `/api/products/{id}/options/{optionId}` | 옵션 삭제 |

### 주문 (인증 필요)
| Method | Path | 설명 |
|--------|------|------|
| GET | `/api/orders` | 내 주문 목록 (페이징) |
| POST | `/api/orders` | 주문 생성 |

### 위시리스트 (인증 필요)
| Method | Path | 설명 |
|--------|------|------|
| GET | `/api/wishes` | 내 위시리스트 (페이징) |
| POST | `/api/wishes` | 위시 추가 |
| DELETE | `/api/wishes/{id}` | 위시 삭제 |

---

## 주요 흐름

### 주문 생성 흐름 (`POST /api/orders`)

```
요청 (Authorization + OrderRequest)
  │
  ▼
1. JWT 검증 → Member 조회
  │ 실패 → 401
  ▼
2. Option 조회
  │ 없음 → 404
  ▼
3. option.subtractQuantity(quantity)   ← 재고 차감
  │ 재고 부족 → 400
  ▼
4. optionRepository.save(option)       ← DB 저장 ⚠️ 트랜잭션 없음
  ▼
5. member.deductPoint(price)           ← 포인트 차감
  │ 포인트 부족 → 400  (이때 재고는 이미 차감된 상태)
  ▼
6. memberRepository.save(member)       ← DB 저장
  ▼
7. orderRepository.save(new Order(...))
  ▼
8. 카카오톡 알림 (best-effort, 실패해도 주문 완료)
  ▼
201 Created + OrderResponse
```

> ⚠️ **현재 문제**: 5번(포인트 차감) 실패 시 3번(재고 차감)이 롤백되지 않음.
> `@Transactional` 부재로 각 save가 즉시 커밋됨.

### Kakao OAuth 흐름

```
클라이언트
  │ GET /api/auth/kakao/login
  ▼
KakaoAuthController → 302 redirect to kauth.kakao.com
  │
  │ (사용자가 카카오에서 인가)
  ▼
GET /api/auth/kakao/callback?code=X
  │
  ▼
KakaoLoginClient.requestAccessToken(code)
  → POST kauth.kakao.com/oauth/token
  │
  ▼
KakaoLoginClient.requestUserInfo(accessToken)
  → GET kapi.kakao.com/v2/user/me
  │
  ▼
이메일로 회원 조회
  ├─ 신규: Member 생성 (password=null, kakaoAccessToken 저장)
  └─ 기존: kakaoAccessToken 업데이트
  │
  ▼
JWT 발급 → TokenResponse 반환
```

---

## 환경 설정

| 환경변수 | 기본값 | 설명 |
|----------|--------|------|
| `JWT_SECRET` | `a-string-secret-at-least-256-bits-long` | JWT 서명 키 |
| `JWT_EXPIRATION` | `3600000` | 토큰 만료(ms) |
| `KAKAO_CLIENT_ID` | (없음) | Kakao 앱 ID |
| `KAKAO_CLIENT_SECRET` | (없음) | Kakao 앱 Secret |
| `KAKAO_REDIRECT_URI` | `http://localhost:8080/api/auth/kakao/callback` | OAuth 콜백 URL |

---

## 데이터베이스 스키마 (Flyway V1)

```sql
category  (id, name UNIQUE, color, image_url, description)
product   (id, name VARCHAR(15), price, image_url, category_id FK)
member    (id, email UNIQUE, password NULL, kakao_access_token NULL, point DEFAULT 0)
options   (id, product_id FK, name VARCHAR(50), quantity)
wish      (id, member_id FK, product_id FK)          -- 중복 방지 제약 없음
orders    (id, option_id FK, member_id FK, quantity, message NULL, order_date_time)
```

V2 기본 데이터: 카테고리 3개, 상품 6개, 회원 3개, 위시 4개, 옵션 8개, 주문 4개

---

## 현재 구조의 주요 문제점

| # | 문제 | 영향 |
|---|------|------|
| 1 | `@Transactional` 없음 | 주문 생성 중 실패 시 데이터 불일치 |
| 2 | 비즈니스 로직이 Controller에 있음 | 테스트 어려움, 변경 비용 높음 |
| 3 | 테스트 없음 | 리팩터링 안전망 없음 |
| 4 | wish 테이블에 유니크 제약 없음 | 중복 방지를 코드에만 의존 |
| 5 | 재고 음수 가능성 | `subtractQuantity`는 검증하나 DB 제약 없음 |
