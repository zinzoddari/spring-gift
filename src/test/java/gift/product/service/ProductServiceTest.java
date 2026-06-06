package gift.product.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.verify;

import gift.category.domain.Category;
import gift.category.repository.CategoryRepository;
import gift.common.dto.PageResponse;
import gift.product.domain.Product;
import gift.product.dto.ProductRequest;
import gift.product.dto.ProductResponse;
import gift.product.repository.ProductRepository;
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

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductService productService;

    private Category category() {
        return new Category("선물", "#FF0000", "http://img.jpg", "선물 카테고리");
    }

    private Product product(Category category) {
        return new Product("상품A", 10_000, "http://img.jpg", category);
    }

    @Nested
    @DisplayName("상품들을 조회할 때,")
    class GetProducts {

        @Nested
        @DisplayName("성공하면,")
        class WhenSuccess {

            @Test
            @DisplayName("상품 목록 페이지를 반환한다.")
            void returnsProductPage() {
                // given
                final Category category = category();
                final Product product = product(category);
                given(productRepository.findAll(any(Pageable.class)))
                    .willReturn(new PageImpl<>(List.of(product)));

                // when
                final PageResponse<ProductResponse> result = productService.getProducts(Pageable.unpaged());

                // then
                assertSoftly(softly -> {
                    softly.assertThat(result.content()).hasSize(1);
                    softly.assertThat(result.content().get(0).name()).isEqualTo("상품A");
                });
            }
        }
    }

    @Nested
    @DisplayName("상품을 조회할 때,")
    class GetProduct {

        @Nested
        @DisplayName("성공하면,")
        class WhenSuccess {

            @Test
            @DisplayName("상품을 반환한다.")
            void returnsProduct() {
                // given
                final Category category = category();
                final Product product = product(category);
                given(productRepository.findById(1L)).willReturn(Optional.of(product));

                // when
                final ProductResponse result = productService.findProduct(1L);

                // then
                assertThat(result.name()).isEqualTo("상품A");
            }
        }

        @Nested
        @DisplayName("실패하면,")
        class WhenFailed {

            @Test
            @DisplayName("존재하지 않는 id면 예외가 발생한다.")
            void throwsWhenNotFound() {
                // given
                given(productRepository.findById(99L)).willReturn(Optional.empty());

                // when & then
                assertThatThrownBy(() -> productService.findProduct(99L))
                    .isInstanceOf(NoSuchElementException.class);
            }
        }
    }

    @Nested
    @DisplayName("상품을 저장할 때,")
    class CreateProduct {

        @Nested
        @DisplayName("성공하면,")
        class WhenSuccess {

            @Test
            @DisplayName("저장된 상품을 반환한다.")
            void returnsCreatedProduct() {
                // given
                final Category category = category();
                final Product product = product(category);
                final ProductRequest request = new ProductRequest("상품A", 10_000, "http://img.jpg", 1L);
                given(categoryRepository.findById(1L)).willReturn(Optional.of(category));
                given(productRepository.save(any(Product.class))).willReturn(product);

                // when
                final ProductResponse result = productService.createProduct(request);

                // then
                assertThat(result.name()).isEqualTo("상품A");
            }
        }

        @Nested
        @DisplayName("실패하면,")
        class WhenFailed {

            @Test
            @DisplayName("카테고리가 없으면 예외가 발생한다.")
            void throwsWhenCategoryNotFound() {
                // given
                final ProductRequest request = new ProductRequest("상품A", 10_000, "http://img.jpg", 99L);
                given(categoryRepository.findById(99L)).willReturn(Optional.empty());

                // when & then
                assertThatThrownBy(() -> productService.createProduct(request))
                    .isInstanceOf(NoSuchElementException.class);
            }

            @Test
            @DisplayName("허용되지 않는 상품명이면 예외가 발생한다.")
            void throwsWhenInvalidName() {
                // given
                final ProductRequest request = new ProductRequest("카카오상품", 10_000, "http://img.jpg", 1L);

                // when & then
                assertThatThrownBy(() -> productService.createProduct(request))
                    .isInstanceOf(IllegalArgumentException.class);
            }
        }
    }

    @Nested
    @DisplayName("상품 정보를 수정할 때,")
    class UpdateProduct {

        @Nested
        @DisplayName("성공하면,")
        class WhenSuccess {

            @Test
            @DisplayName("수정된 상품을 반환한다.")
            void returnsUpdatedProduct() {
                // given
                final Category category = category();
                final Product product = product(category);
                final ProductRequest request = new ProductRequest("수정상품", 20_000, "http://img.jpg", 1L);
                given(categoryRepository.findById(1L)).willReturn(Optional.of(category));
                given(productRepository.findById(1L)).willReturn(Optional.of(product));
                given(productRepository.save(any(Product.class))).willReturn(product);

                // when
                final ProductResponse result = productService.updateProduct(1L, request);

                // then
                assertThat(result.name()).isEqualTo("수정상품");
            }
        }

        @Nested
        @DisplayName("실패하면,")
        class WhenFailed {

            @Test
            @DisplayName("상품이 없으면 예외가 발생한다.")
            void throwsWhenProductNotFound() {
                // given
                final Category category = category();
                final ProductRequest request = new ProductRequest("수정상품", 20_000, "http://img.jpg", 1L);
                given(categoryRepository.findById(1L)).willReturn(Optional.of(category));
                given(productRepository.findById(99L)).willReturn(Optional.empty());

                // when & then
                assertThatThrownBy(() -> productService.updateProduct(99L, request))
                    .isInstanceOf(NoSuchElementException.class);
            }
        }
    }

    @Nested
    @DisplayName("상품을 삭제할 때,")
    class DeleteProduct {

        @Nested
        @DisplayName("성공하면,")
        class WhenSuccess {

            @Test
            @DisplayName("삭제를 호출한다.")
            void callsDelete() {
                // given
                willDoNothing().given(productRepository).deleteById(1L);

                // when
                productService.deleteProduct(1L);

                // then
                verify(productRepository).deleteById(1L);
            }
        }
    }
}
