package gift.category.api;

import gift.category.dto.CategoryRequest;
import gift.category.dto.CategoryResponse;
import gift.category.service.CategoryService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(final CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public List<CategoryResponse> getCategories() {
        return categoryService.getCategories();
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody final CategoryRequest request) {
        final CategoryResponse response = categoryService.createCategory(request);
        return ResponseEntity.created(URI.create("/api/categories/" + response.id()))
            .body(response);
    }

    @PutMapping("/{id}")
    public CategoryResponse updateCategory(
        @PathVariable final Long id,
        @Valid @RequestBody final CategoryRequest request
    ) {
        return categoryService.updateCategory(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable final Long id) {
        categoryService.deleteCategory(id);

        return ResponseEntity.noContent().build();
    }
}
