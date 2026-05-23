package gift.category.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import gift.auth.AuthenticationResolver;
import gift.category.Category;
import gift.category.repository.CategoryRepository;
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

@DisplayName("CategoryController")
@WebMvcTest(CategoryController.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticationResolver authenticationResolver;

    @MockitoBean
    private CategoryRepository categoryRepository;

    private Category category() {
        final Category category = mock(Category.class);
        given(category.getId()).willReturn(1L);
        given(category.getName()).willReturn("카테고리A");
        given(category.getColor()).willReturn("#FF0000");
        given(category.getImageUrl()).willReturn("http://img.jpg");
        given(category.getDescription()).willReturn("설명");
        return category;
    }

    @Nested
    @DisplayName("카테고리 목록을 조회할 때,")
    class GetCategories {

        @Nested
        @DisplayName("성공하면,")
        class WhenSuccess {

            @Test
            @DisplayName("카테고리 목록을 반환한다.")
            void returnsCategoryList() throws Exception {
                // given
                final Category category = category();
                given(categoryRepository.findAll()).willReturn(List.of(category));

                // when & then
                mockMvc.perform(get("/api/categories"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(1L))
                    .andExpect(jsonPath("$[0].name").value("카테고리A"))
                    .andExpect(jsonPath("$[0].color").value("#FF0000"));
            }
        }
    }

    @Nested
    @DisplayName("카테고리를 생성할 때,")
    class CreateCategory {

        @Nested
        @DisplayName("성공하면,")
        class WhenSuccess {

            @Test
            @DisplayName("201과 생성된 카테고리를 반환한다.")
            void returnsCreated() throws Exception {
                // given
                final Category category = category();
                given(categoryRepository.save(any(Category.class))).willReturn(category);

                // when & then
                mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"name": "카테고리A", "color": "#FF0000", "imageUrl": "http://img.jpg", "description": "설명"}
                            """))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", "/api/categories/1"))
                    .andExpect(jsonPath("$.name").value("카테고리A"));
            }
        }

        @Nested
        @DisplayName("실패하면,")
        class WhenFailed {

            @Test
            @DisplayName("이름이 비어있으면 400을 반환한다.")
            void returnsBadRequestWhenNameBlank() throws Exception {
                mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"name": "", "color": "#FF0000", "imageUrl": "http://img.jpg"}
                            """))
                    .andExpect(status().isBadRequest());
            }

            @Test
            @DisplayName("색상이 비어있으면 400을 반환한다.")
            void returnsBadRequestWhenColorBlank() throws Exception {
                mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"name": "카테고리A", "color": "", "imageUrl": "http://img.jpg"}
                            """))
                    .andExpect(status().isBadRequest());
            }

            @Test
            @DisplayName("이미지 URL이 비어있으면 400을 반환한다.")
            void returnsBadRequestWhenImageUrlBlank() throws Exception {
                mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"name": "카테고리A", "color": "#FF0000", "imageUrl": ""}
                            """))
                    .andExpect(status().isBadRequest());
            }
        }
    }

    @Nested
    @DisplayName("카테고리를 수정할 때,")
    class UpdateCategory {

        @Nested
        @DisplayName("성공하면,")
        class WhenSuccess {

            @Test
            @DisplayName("수정된 카테고리를 반환한다.")
            void returnsUpdatedCategory() throws Exception {
                // given
                final Category category = category();
                given(categoryRepository.findById(1L)).willReturn(Optional.of(category));
                given(categoryRepository.save(category)).willReturn(category);

                // when & then
                mockMvc.perform(put("/api/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"name": "카테고리A", "color": "#FF0000", "imageUrl": "http://img.jpg", "description": "설명"}
                            """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.name").value("카테고리A"));
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
                mockMvc.perform(put("/api/categories/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"name": "카테고리A", "color": "#FF0000", "imageUrl": "http://img.jpg"}
                            """))
                    .andExpect(status().isNotFound());
            }
        }
    }

    @Nested
    @DisplayName("카테고리를 삭제할 때,")
    class DeleteCategory {

        @Nested
        @DisplayName("성공하면,")
        class WhenSuccess {

            @Test
            @DisplayName("204를 반환한다.")
            void returnsNoContent() throws Exception {
                // given
                willDoNothing().given(categoryRepository).deleteById(1L);

                // when & then
                mockMvc.perform(delete("/api/categories/1"))
                    .andExpect(status().isNoContent());
            }
        }
    }
}
