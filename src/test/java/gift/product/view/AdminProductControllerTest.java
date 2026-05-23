package gift.product.view;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import gift.auth.AuthenticationResolver;
import gift.category.domain.Category;
import gift.category.repository.CategoryRepository;
import gift.product.domain.Product;
import gift.product.repository.ProductRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@DisplayName("AdminProductController")
@WebMvcTest(AdminProductController.class)
class AdminProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticationResolver authenticationResolver;

    @MockitoBean
    private ProductRepository productRepository;

    @MockitoBean
    private CategoryRepository categoryRepository;

    private Category category() {
        final Category category = mock(Category.class);
        given(category.getId()).willReturn(1L);
        given(category.getName()).willReturn("카테고리A");
        return category;
    }

    private Product product() {
        final Category category = category();
        final Product product = mock(Product.class);
        given(product.getId()).willReturn(1L);
        given(product.getName()).willReturn("상품A");
        given(product.getPrice()).willReturn(10_000);
        given(product.getImageUrl()).willReturn("http://img.jpg");
        given(product.getCategory()).willReturn(category);
        return product;
    }

    @Nested
    @DisplayName("상품 목록을 조회할 때,")
    class ProductList {

        @Nested
        @DisplayName("성공하면,")
        class WhenSuccess {

            @Test
            @DisplayName("상품 목록 뷰를 반환한다.")
            void returnsProductListView() throws Exception {
                // given
                final Product product = product();
                given(productRepository.findAll()).willReturn(List.of(product));

                // when & then
                mockMvc.perform(get("/admin/products"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("product/list"))
                    .andExpect(model().attributeExists("products"));
            }
        }
    }

    @Nested
    @DisplayName("상품 등록 폼을 조회할 때,")
    class NewForm {

        @Nested
        @DisplayName("성공하면,")
        class WhenSuccess {

            @Test
            @DisplayName("상품 등록 뷰를 반환한다.")
            void returnsProductNewView() throws Exception {
                // given
                final Category category = category();
                given(categoryRepository.findAll()).willReturn(List.of(category));

                // when & then
                mockMvc.perform(get("/admin/products/new"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("product/new"))
                    .andExpect(model().attributeExists("categories"));
            }
        }
    }

    @Nested
    @DisplayName("상품을 등록할 때,")
    class Create {

        @Nested
        @DisplayName("성공하면,")
        class WhenSuccess {

            @Test
            @DisplayName("상품 목록으로 리다이렉트한다.")
            void redirectsToProductList() throws Exception {
                // given
                given(categoryRepository.findById(1L)).willReturn(Optional.of(mock(Category.class)));

                // when & then
                mockMvc.perform(post("/admin/products")
                        .param("name", "상품A")
                        .param("price", "10000")
                        .param("imageUrl", "http://img.jpg")
                        .param("categoryId", "1"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/admin/products"));
            }
        }

        @Nested
        @DisplayName("실패하면,")
        class WhenFailed {

            @Test
            @DisplayName("유효하지 않은 상품명이면 등록 폼으로 돌아간다.")
            void returnsNewFormWhenNameInvalid() throws Exception {
                // given
                final Category category = category();
                given(categoryRepository.findAll()).willReturn(List.of(category));

                // when & then
                mockMvc.perform(post("/admin/products")
                        .param("name", "상품@A")
                        .param("price", "10000")
                        .param("imageUrl", "http://img.jpg")
                        .param("categoryId", "1"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("product/new"))
                    .andExpect(model().attributeExists("errors"));
            }

            @Test
            @DisplayName("카테고리가 없으면 404를 반환한다.")
            void returnsNotFoundWhenCategoryMissing() throws Exception {
                // given
                given(categoryRepository.findById(99L)).willReturn(Optional.empty());

                // when & then
                mockMvc.perform(post("/admin/products")
                        .param("name", "상품A")
                        .param("price", "10000")
                        .param("imageUrl", "http://img.jpg")
                        .param("categoryId", "99"))
                    .andExpect(status().isNotFound());
            }
        }
    }

    @Nested
    @DisplayName("상품 수정 폼을 조회할 때,")
    class EditForm {

        @Nested
        @DisplayName("성공하면,")
        class WhenSuccess {

            @Test
            @DisplayName("상품 수정 뷰를 반환한다.")
            void returnsProductEditView() throws Exception {
                // given
                final Product product = product();
                final Category category = category();
                given(productRepository.findById(1L)).willReturn(Optional.of(product));
                given(categoryRepository.findAll()).willReturn(List.of(category));

                // when & then
                mockMvc.perform(get("/admin/products/1/edit"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("product/edit"))
                    .andExpect(model().attributeExists("product"))
                    .andExpect(model().attributeExists("categories"));
            }
        }

        @Nested
        @DisplayName("실패하면,")
        class WhenFailed {

            @Test
            @DisplayName("상품이 없으면 404를 반환한다.")
            void returnsNotFoundWhenProductMissing() throws Exception {
                // given
                given(productRepository.findById(99L)).willReturn(Optional.empty());

                // when & then
                mockMvc.perform(get("/admin/products/99/edit"))
                    .andExpect(status().isNotFound());
            }
        }
    }

    @Nested
    @DisplayName("상품을 수정할 때,")
    class Update {

        @Nested
        @DisplayName("성공하면,")
        class WhenSuccess {

            @Test
            @DisplayName("상품 목록으로 리다이렉트한다.")
            void redirectsToProductList() throws Exception {
                // given
                given(productRepository.findById(1L)).willReturn(Optional.of(mock(Product.class)));
                given(categoryRepository.findById(1L)).willReturn(Optional.of(mock(Category.class)));

                // when & then
                mockMvc.perform(post("/admin/products/1/edit")
                        .param("name", "상품A")
                        .param("price", "10000")
                        .param("imageUrl", "http://img.jpg")
                        .param("categoryId", "1"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/admin/products"));
            }
        }

        @Nested
        @DisplayName("실패하면,")
        class WhenFailed {

            @Test
            @DisplayName("유효하지 않은 상품명이면 수정 폼으로 돌아간다.")
            void returnsEditFormWhenNameInvalid() throws Exception {
                // given
                final Product product = product();
                final Category category = category();
                given(productRepository.findById(1L)).willReturn(Optional.of(product));
                given(categoryRepository.findAll()).willReturn(List.of(category));

                // when & then
                mockMvc.perform(post("/admin/products/1/edit")
                        .param("name", "상품@A")
                        .param("price", "10000")
                        .param("imageUrl", "http://img.jpg")
                        .param("categoryId", "1"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("product/edit"))
                    .andExpect(model().attributeExists("errors"));
            }

            @Test
            @DisplayName("상품이 없으면 404를 반환한다.")
            void returnsNotFoundWhenProductMissing() throws Exception {
                // given
                given(productRepository.findById(99L)).willReturn(Optional.empty());

                // when & then
                mockMvc.perform(post("/admin/products/99/edit")
                        .param("name", "상품A")
                        .param("price", "10000")
                        .param("imageUrl", "http://img.jpg")
                        .param("categoryId", "1"))
                    .andExpect(status().isNotFound());
            }
        }
    }

    @Nested
    @DisplayName("상품을 삭제할 때,")
    class Delete {

        @Nested
        @DisplayName("성공하면,")
        class WhenSuccess {

            @Test
            @DisplayName("상품 목록으로 리다이렉트한다.")
            void redirectsToProductList() throws Exception {
                // given
                willDoNothing().given(productRepository).deleteById(1L);

                // when & then
                mockMvc.perform(post("/admin/products/1/delete"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/admin/products"));
            }
        }
    }
}
