package gift.category.service;

import gift.category.Category;
import gift.category.dto.CategoryRequest;
import gift.category.dto.CategoryResponse;
import gift.category.repository.CategoryRepository;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(final CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategories() {
        return categoryRepository.findAll().stream()
            .map(CategoryResponse::from)
            .toList();
    }

    @Transactional
    public CategoryResponse createCategory(final CategoryRequest request) {
        return CategoryResponse.from(categoryRepository.save(request.toEntity()));
    }

    @Transactional
    public CategoryResponse updateCategory(final Long id, final CategoryRequest request) {
        final Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("카테고리를 찾을 수 없습니다."));

        category.update(request.name(), request.color(), request.imageUrl(), request.description());

        return CategoryResponse.from(category);
    }

    @Transactional
    public void deleteCategory(final Long id) {
        categoryRepository.deleteById(id);
    }
}
