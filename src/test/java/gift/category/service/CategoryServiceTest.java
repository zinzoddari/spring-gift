package gift.category.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import gift.category.Category;
import gift.category.dto.CategoryRequest;
import gift.category.dto.CategoryResponse;
import gift.category.repository.CategoryRepository;
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

@DisplayName("CategoryService")
@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category category() {
        final Category category = mock(Category.class);
        given(category.getId()).willReturn(1L);
        given(category.getName()).willReturn("카테고리A");
        given(category.getColor()).willReturn("#FF0000");
        given(category.getImageUrl()).willReturn("http://img.jpg");
        given(category.getDescription()).willReturn("설명");
        return category;
    }

    private CategoryRequest request() {
        return new CategoryRequest("카테고리A", "#FF0000", "http://img.jpg", "설명");
    }

    @Nested
    @DisplayName("카테고리 목록을 조회할 때,")
    class GetCategories {

        @Nested
        @DisplayName("성공하면,")
        class WhenSuccess {

            @Test
            @DisplayName("전체 카테고리 목록을 반환한다.")
            void returnsCategoryList() {
                // given
                final Category category = category();
                given(categoryRepository.findAll()).willReturn(List.of(category));

                // when
                final List<CategoryResponse> result = categoryService.getCategories();

                // then
                assertSoftly(softly -> {
                    softly.assertThat(result).hasSize(1);
                    softly.assertThat(result.get(0).id()).isEqualTo(1L);
                    softly.assertThat(result.get(0).name()).isEqualTo("카테고리A");
                });
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
            @DisplayName("저장된 카테고리를 반환한다.")
            void returnsSavedCategory() {
                // given
                final Category category = category();
                given(categoryRepository.save(any(Category.class))).willReturn(category);

                // when
                final CategoryResponse result = categoryService.createCategory(request());

                // then
                assertSoftly(softly -> {
                    softly.assertThat(result.id()).isEqualTo(1L);
                    softly.assertThat(result.name()).isEqualTo("카테고리A");
                    softly.assertThat(result.color()).isEqualTo("#FF0000");
                });
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
            void returnsUpdatedCategory() {
                // given
                final Category category = category();
                given(categoryRepository.findById(1L)).willReturn(Optional.of(category));

                // when
                final CategoryResponse result = categoryService.updateCategory(1L, request());

                // then
                assertThat(result.id()).isEqualTo(1L);
            }
        }

        @Nested
        @DisplayName("실패하면,")
        class WhenFailed {

            @Test
            @DisplayName("카테고리가 없으면 예외가 발생한다.")
            void throwsWhenCategoryNotFound() {
                // given
                given(categoryRepository.findById(99L)).willReturn(Optional.empty());

                // when & then
                assertThatThrownBy(() -> categoryService.updateCategory(99L, request()))
                    .isInstanceOf(NoSuchElementException.class);
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
            @DisplayName("삭제를 호출한다.")
            void callsDelete() {
                // given
                willDoNothing().given(categoryRepository).deleteById(1L);

                // when
                categoryService.deleteCategory(1L);

                // then
                verify(categoryRepository).deleteById(1L);
            }
        }
    }
}
