package gift.option.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import gift.auth.AuthenticationResolver;
import gift.option.dto.OptionRequest;
import gift.option.dto.OptionResponse;
import gift.option.service.OptionFacade;
import java.util.List;
import java.util.NoSuchElementException;
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
    private OptionFacade optionFacade;

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
                given(optionFacade.getOptions(1L)).willReturn(List.of(
                    new OptionResponse(1L, "옵션A", 10),
                    new OptionResponse(2L, "옵션B", 5)
                ));

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
                given(optionFacade.getOptions(99L)).willThrow(new NoSuchElementException());

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
                given(optionFacade.createOption(eq(1L), any(OptionRequest.class)))
                    .willReturn(new OptionResponse(1L, "옵션A", 10));

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
                given(optionFacade.createOption(eq(1L), any(OptionRequest.class)))
                    .willThrow(new NoSuchElementException());

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
                given(optionFacade.createOption(eq(1L), any(OptionRequest.class)))
                    .willThrow(new IllegalArgumentException("이미 존재하는 옵션명입니다."));

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
                // given
                given(optionFacade.createOption(eq(1L), any(OptionRequest.class)))
                    .willThrow(new IllegalArgumentException("허용되지 않는 특수문자가 포함되어 있습니다."));

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
                willDoNothing().given(optionFacade).deleteOption(1L, 1L);

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
                willThrow(new NoSuchElementException()).given(optionFacade).deleteOption(1L, 1L);

                // when & then
                mockMvc.perform(delete("/api/products/1/options/1"))
                    .andExpect(status().isNotFound());
            }

            @Test
            @DisplayName("옵션이 1개뿐이면 400을 반환한다.")
            void returnsBadRequestWhenLastOption() throws Exception {
                // given
                willThrow(new IllegalArgumentException("옵션이 1개인 상품은 옵션을 삭제할 수 없습니다."))
                    .given(optionFacade).deleteOption(1L, 1L);

                // when & then
                mockMvc.perform(delete("/api/products/1/options/1"))
                    .andExpect(status().isBadRequest());
            }

            @Test
            @DisplayName("옵션이 없으면 404를 반환한다.")
            void returnsNotFoundWhenOptionNotFound() throws Exception {
                // given
                willThrow(new NoSuchElementException()).given(optionFacade).deleteOption(1L, 99L);

                // when & then
                mockMvc.perform(delete("/api/products/1/options/99"))
                    .andExpect(status().isNotFound());
            }
        }
    }
}
