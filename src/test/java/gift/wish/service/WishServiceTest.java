package gift.wish.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import gift.common.dto.PageResponse;
import gift.product.domain.Product;
import gift.product.repository.ProductRepository;
import gift.wish.domain.Wish;
import gift.wish.dto.WishAddResult;
import gift.wish.dto.WishRequest;
import gift.wish.dto.WishResponse;
import gift.wish.repository.WishRepository;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@DisplayName("WishService")
@ExtendWith(MockitoExtension.class)
class WishServiceTest {

    @Mock
    private WishRepository wishRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private WishService wishService;

    private Product product() {
        final Product product = mock(Product.class);
        given(product.getId()).willReturn(1L);
        given(product.getName()).willReturn("상품A");
        given(product.getPrice()).willReturn(10_000);
        given(product.getImageUrl()).willReturn("http://img.jpg");
        return product;
    }

    private Wish wish(final Product product) {
        final Wish wish = mock(Wish.class);
        given(wish.getId()).willReturn(1L);
        given(wish.getProduct()).willReturn(product);
        return wish;
    }

    @Nested
    @DisplayName("위시리스트를 조회할 때,")
    class GetWishes {

        @Nested
        @DisplayName("성공하면,")
        class WhenSuccess {

            @Test
            @DisplayName("찜 목록 페이지를 반환한다.")
            void returnsWishPage() {
                // given
                final Product product = product();
                final Wish wish = wish(product);
                given(wishRepository.findByMemberId(eq(1L), any(Pageable.class)))
                    .willReturn(new PageImpl<>(List.of(wish)));

                // when
                final PageResponse<WishResponse> result = wishService.getWishes(1L, Pageable.unpaged());

                // then
                assertSoftly(softly -> {
                    softly.assertThat(result.content()).hasSize(1);
                    softly.assertThat(result.content().get(0).name()).isEqualTo("상품A");
                });
            }
        }
    }

    @Nested
    @DisplayName("위시리스트를 저장할 때,")
    class AddWish {

        @Nested
        @DisplayName("성공하면,")
        class WhenSuccess {

            @Test
            @DisplayName("신규 찜이면 created=true로 반환한다.")
            void returnsCreatedResult() {
                // given
                final Product product = product();
                final Wish wish = wish(product);
                final WishRequest request = new WishRequest(1L);
                given(productRepository.findById(1L)).willReturn(Optional.of(product));
                given(wishRepository.findByMemberIdAndProductId(1L, 1L)).willReturn(Optional.empty());
                given(wishRepository.save(any(Wish.class))).willReturn(wish);

                // when
                final WishAddResult result = wishService.addWish(1L, request);

                // then
                assertSoftly(softly -> {
                    softly.assertThat(result.created()).isTrue();
                    softly.assertThat(result.response().id()).isEqualTo(1L);
                    softly.assertThat(result.response().name()).isEqualTo("상품A");
                });
            }

            @Test
            @DisplayName("이미 찜한 상품이면 created=false로 반환한다.")
            void returnsDuplicateResult() {
                // given
                final Product product = product();
                final Wish existing = wish(product);
                final WishRequest request = new WishRequest(1L);
                given(productRepository.findById(1L)).willReturn(Optional.of(product));
                given(wishRepository.findByMemberIdAndProductId(1L, 1L)).willReturn(Optional.of(existing));

                // when
                final WishAddResult result = wishService.addWish(1L, request);

                // then
                assertSoftly(softly -> {
                    softly.assertThat(result.created()).isFalse();
                    softly.assertThat(result.response().id()).isEqualTo(1L);
                });
            }
        }

        @Nested
        @DisplayName("실패하면,")
        class WhenFailed {

            @Test
            @DisplayName("상품이 없으면 예외가 발생한다.")
            void throwsWhenProductNotFound() {
                // given
                final WishRequest request = new WishRequest(99L);
                given(productRepository.findById(99L)).willReturn(Optional.empty());

                // when & then
                assertThatThrownBy(() -> wishService.addWish(1L, request))
                    .isInstanceOf(NoSuchElementException.class);
            }
        }
    }

    @Nested
    @DisplayName("위시리스트를 삭제할 때,")
    class RemoveWish {

        @Nested
        @DisplayName("성공하면,")
        class WhenSuccess {

            @Test
            @DisplayName("삭제를 호출한다.")
            void callsDelete() {
                // given
                final Wish wish = mock(Wish.class);
                given(wish.isOwnedBy(1L)).willReturn(true);
                given(wishRepository.findById(1L)).willReturn(Optional.of(wish));
                willDoNothing().given(wishRepository).delete(wish);

                // when
                wishService.removeWish(1L, 1L);

                // then
                verify(wishRepository).delete(wish);
            }
        }

        @Nested
        @DisplayName("실패하면,")
        class WhenFailed {

            @Test
            @DisplayName("찜이 없으면 예외가 발생한다.")
            void throwsWhenWishNotFound() {
                // given
                given(wishRepository.findById(99L)).willReturn(Optional.empty());

                // when & then
                assertThatThrownBy(() -> wishService.removeWish(1L, 99L))
                    .isInstanceOf(NoSuchElementException.class);
            }

            @Test
            @DisplayName("본인의 찜이 아니면 예외가 발생한다.")
            void throwsWhenNotOwner() {
                // given
                final Wish wish = mock(Wish.class);
                given(wish.isOwnedBy(1L)).willReturn(false);
                given(wishRepository.findById(1L)).willReturn(Optional.of(wish));

                // when & then
                assertThatThrownBy(() -> wishService.removeWish(1L, 1L))
                    .isInstanceOf(SecurityException.class);
            }
        }
    }
}
