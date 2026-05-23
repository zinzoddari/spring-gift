package gift.product.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import gift.category.Category;
import gift.category.repository.CategoryRepository;
import gift.product.Product;
import gift.product.repository.ProductRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

@DisplayName("ProductController")
@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductRepository productRepository;

    @MockitoBean
    private CategoryRepository categoryRepository;

    private Category category() {
        return new Category("선물", "#FF0000", "http://img.jpg", "선물 카테고리");
    }

    private Product product(Category category) {
        return new Product("상품A", 10_000, "http://img.jpg", category);
    }

    @Nested
    @DisplayName("상품 조회를 할 때,")
    class GetProducts {

        @Nested
        @DisplayName("성공하면,")
        class WhenSuccess {

            @Test
            @DisplayName("상품 목록을 페이지로 반환한다.")
            void returnsProductPage() throws Exception {
                // given
                final Category category = category();
                final Product product = product(category);
                given(productRepository.findAll(any(Pageable.class)))
                    .willReturn(new PageImpl<>(List.of(product)));

                // when & then
                mockMvc.perform(get("/api/products"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].name").value("상품A"))
                    .andExpect(jsonPath("$.content[0].price").value(10_000));
            }
        }
    }

    @Nested
    @DisplayName("특정 상품을 조회할 때,")
    class GetProduct {

        @Nested
        @DisplayName("성공하면,")
        class WhenSuccess {

            @Test
            @DisplayName("상품을 반환한다.")
            void returnsProduct() throws Exception {
                // given
                final Category category = category();
                final Product product = product(category);
                given(productRepository.findById(1L)).willReturn(Optional.of(product));

                // when & then
                mockMvc.perform(get("/api/products/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("상품A"));
            }
        }

        @Nested
        @DisplayName("실패하면,")
        class WhenFailed {

            @Test
            @DisplayName("존재하지 않는 id면 404를 반환한다.")
            void returnsNotFound() throws Exception {
                // given
                given(productRepository.findById(99L)).willReturn(Optional.empty());

                // when & then
                mockMvc.perform(get("/api/products/99"))
                    .andExpect(status().isNotFound());
            }
        }
    }

    @Nested
    @DisplayName("상품 정보를 저장할 때,")
    class CreateProduct {

        @Nested
        @DisplayName("성공하면,")
        class WhenSuccess {

            @Test
            @DisplayName("201과 생성된 상품을 반환한다.")
            void returnsCreated() throws Exception {
                // given
                final Category category = category();
                final Product product = product(category);
                given(categoryRepository.findById(1L)).willReturn(Optional.of(category));
                given(productRepository.save(any(Product.class))).willReturn(product);

                // when & then
                mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"name": "상품A", "price": 10000, "imageUrl": "http://img.jpg", "categoryId": 1}
                            """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name").value("상품A"));
            }
        }

        @Nested
        @DisplayName("실패하면,")
        class WhenFailed {

            @Test
            @DisplayName("카테고리가 없으면 404를 반환한다.")
            void returnsNotFoundWhenCategoryMissing() throws Exception {
                // given
                given(categoryRepository.findById(99L)).willReturn(Optional.empty());

                // when & then
                mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"name": "상품A", "price": 10000, "imageUrl": "http://img.jpg", "categoryId": 99}
                            """))
                    .andExpect(status().isNotFound());
            }

            @Test
            @DisplayName("허용되지 않는 상품명이면 400을 반환한다.")
            void returnsBadRequestWhenInvalidName() throws Exception {
                // when & then
                mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"name": "카카오상품", "price": 10000, "imageUrl": "http://img.jpg", "categoryId": 1}
                            """))
                    .andExpect(status().isBadRequest());
            }
        }
    }

    @Nested
    @DisplayName("특정 상품 정보를 수정할 때,")
    class UpdateProduct {

        @Nested
        @DisplayName("성공하면,")
        class WhenSuccess {

            @Test
            @DisplayName("수정된 상품을 반환한다.")
            void returnsUpdatedProduct() throws Exception {
                // given
                final Category category = category();
                final Product product = product(category);
                given(categoryRepository.findById(1L)).willReturn(Optional.of(category));
                given(productRepository.findById(1L)).willReturn(Optional.of(product));
                given(productRepository.save(any(Product.class))).willReturn(product);

                // when & then
                mockMvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"name": "수정상품", "price": 20000, "imageUrl": "http://img.jpg", "categoryId": 1}
                            """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("수정상품"));
            }
        }

        @Nested
        @DisplayName("실패하면,")
        class WhenFailed {

            @Test
            @DisplayName("상품이 없으면 404를 반환한다.")
            void returnsNotFoundWhenProductMissing() throws Exception {
                // given
                final Category category = category();
                given(categoryRepository.findById(1L)).willReturn(Optional.of(category));
                given(productRepository.findById(99L)).willReturn(Optional.empty());

                // when & then
                mockMvc.perform(put("/api/products/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"name": "수정상품", "price": 20000, "imageUrl": "http://img.jpg", "categoryId": 1}
                            """))
                    .andExpect(status().isNotFound());
            }
        }
    }

    @Nested
    @DisplayName("특정 상품을 삭제할 때,")
    class DeleteProduct {

        @Nested
        @DisplayName("성공하면,")
        class WhenSuccess {

            @Test
            @DisplayName("204를 반환한다.")
            void returnsNoContent() throws Exception {
                // given
                willDoNothing().given(productRepository).deleteById(1L);

                // when & then
                mockMvc.perform(delete("/api/products/1"))
                    .andExpect(status().isNoContent());
            }
        }
    }
}
