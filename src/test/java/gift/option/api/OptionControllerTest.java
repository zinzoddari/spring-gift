package gift.option.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import gift.auth.AuthenticationResolver;
import gift.option.Option;
import gift.option.dto.OptionRequest;
import gift.option.repository.OptionRepository;
import gift.product.domain.Product;
import gift.product.repository.ProductRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@DisplayName("OptionController")
@WebMvcTest(OptionController.class)
class OptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticationResolver authenticationResolver;

    @MockitoBean
    private OptionRepository optionRepository;

    @MockitoBean
    private ProductRepository productRepository;

    private Product product() {
        final Product product = mock(Product.class);
        given(product.getId()).willReturn(1L);
        return product;
    }

    private Option option(final Product product) {
        final Option option = mock(Option.class);
        given(option.getId()).willReturn(1L);
        given(option.getName()).willReturn("옵션A");
        given(option.getQuantity()).willReturn(10);
        given(option.getProduct()).willReturn(product);
        return option;
    }

    @Nested
    @DisplayName("옵션 목록을 조회할 때,")
    class GetOptions {

        @Nested
        @DisplayName("성공하면,")
        class WhenSuccess {

            @Test
            @DisplayName("상품의 옵션 여러 개를 반환한다.")
            void returnsOptionList() throws Exception {
                // given
                final Product product = product();
                final Option optionA = option(product);
                final Option optionB = mock(Option.class);
                given(optionB.getId()).willReturn(2L);
                given(optionB.getName()).willReturn("옵션B");
                given(optionB.getQuantity()).willReturn(5);
                given(productRepository.findById(1L)).willReturn(Optional.of(product));
                given(optionRepository.findByProductId(1L)).willReturn(List.of(optionA, optionB));

                // when & then
                mockMvc.perform(get("/api/products/1/options"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].name").value("옵션A"))
                    .andExpect(jsonPath("$[1].name").value("옵션B"));
            }
        }

        @Nested
        @DisplayName("실패하면,")
        class WhenFailed {

            @Test
            @DisplayName("상품이 없으면 404를 반환한다.")
            void returnsNotFoundWhenProductNotFound() throws Exception {
                // given
                given(productRepository.findById(99L)).willReturn(Optional.empty());

                // when & then
                mockMvc.perform(get("/api/products/99/options"))
                    .andExpect(status().isNotFound());
            }
        }
    }

    @Nested
    @DisplayName("옵션을 생성할 때,")
    class CreateOption {

        @Nested
        @DisplayName("성공하면,")
        class WhenSuccess {

            @Test
            @DisplayName("생성된 옵션과 201을 반환한다.")
            void returnsCreatedOption() throws Exception {
                // given
                final Product product = product();
                final Option option = option(product);
                given(productRepository.findById(1L)).willReturn(Optional.of(product));
                given(optionRepository.existsByProductIdAndName(1L, "옵션A")).willReturn(false);
                given(optionRepository.save(any(Option.class))).willReturn(option);

                // when & then
                mockMvc.perform(post("/api/products/1/options")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"name": "옵션A", "quantity": 10}
                            """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name").value("옵션A"))
                    .andExpect(jsonPath("$.quantity").value(10));
            }
        }

        @Nested
        @DisplayName("실패하면,")
        class WhenFailed {

            @Test
            @DisplayName("상품이 없으면 404를 반환한다.")
            void returnsNotFoundWhenProductNotFound() throws Exception {
                // given
                given(productRepository.findById(1L)).willReturn(Optional.empty());

                // when & then
                mockMvc.perform(post("/api/products/1/options")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"name": "옵션A", "quantity": 10}
                            """))
                    .andExpect(status().isNotFound());
            }

            @Test
            @DisplayName("이미 존재하는 옵션명이면 400을 반환한다.")
            void returnsBadRequestWhenDuplicateName() throws Exception {
                // given
                final Product product = product();
                given(productRepository.findById(1L)).willReturn(Optional.of(product));
                given(optionRepository.existsByProductIdAndName(1L, "옵션A")).willReturn(true);

                // when & then
                mockMvc.perform(post("/api/products/1/options")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"name": "옵션A", "quantity": 10}
                            """))
                    .andExpect(status().isBadRequest());
            }

            @Test
            @DisplayName("허용되지 않는 특수문자가 포함된 이름이면 400을 반환한다.")
            void returnsBadRequestWhenInvalidName() throws Exception {
                // when & then
                mockMvc.perform(post("/api/products/1/options")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"name": "옵션@이름", "quantity": 10}
                            """))
                    .andExpect(status().isBadRequest());
            }
        }
    }

    @Nested
    @DisplayName("옵션을 삭제할 때,")
    class DeleteOption {

        @Nested
        @DisplayName("성공하면,")
        class WhenSuccess {

            @Test
            @DisplayName("옵션을 삭제하고 204를 반환한다.")
            void returnsNoContent() throws Exception {
                // given
                final Product product = product();
                final Option option = option(product);
                final Option otherOption = mock(Option.class);
                given(productRepository.findById(1L)).willReturn(Optional.of(product));
                given(optionRepository.findByProductId(1L)).willReturn(List.of(option, otherOption));
                given(optionRepository.findById(1L)).willReturn(Optional.of(option));
                willDoNothing().given(optionRepository).delete(option);

                // when & then
                mockMvc.perform(delete("/api/products/1/options/1"))
                    .andExpect(status().isNoContent());
            }
        }

        @Nested
        @DisplayName("실패하면,")
        class WhenFailed {

            @Test
            @DisplayName("상품이 없으면 404를 반환한다.")
            void returnsNotFoundWhenProductNotFound() throws Exception {
                // given
                given(productRepository.findById(1L)).willReturn(Optional.empty());

                // when & then
                mockMvc.perform(delete("/api/products/1/options/1"))
                    .andExpect(status().isNotFound());
            }

            @Test
            @DisplayName("옵션이 1개뿐이면 400을 반환한다.")
            void returnsBadRequestWhenLastOption() throws Exception {
                // given
                final Product product = product();
                final Option option = option(product);
                given(productRepository.findById(1L)).willReturn(Optional.of(product));
                given(optionRepository.findByProductId(1L)).willReturn(List.of(option));

                // when & then
                mockMvc.perform(delete("/api/products/1/options/1"))
                    .andExpect(status().isBadRequest());
            }

            @Test
            @DisplayName("옵션이 없으면 404를 반환한다.")
            void returnsNotFoundWhenOptionNotFound() throws Exception {
                // given
                final Product product = product();
                final Option otherOption = mock(Option.class);
                given(productRepository.findById(1L)).willReturn(Optional.of(product));
                given(optionRepository.findByProductId(1L)).willReturn(List.of(otherOption, mock(Option.class)));
                given(optionRepository.findById(99L)).willReturn(Optional.empty());

                // when & then
                mockMvc.perform(delete("/api/products/1/options/99"))
                    .andExpect(status().isNotFound());
            }
        }
    }
}
