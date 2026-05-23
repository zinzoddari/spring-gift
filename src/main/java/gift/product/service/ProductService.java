package gift.product.service;

import gift.category.Category;
import gift.category.repository.CategoryRepository;
import gift.product.ProductNameValidator;
import gift.product.domain.Product;
import gift.product.dto.ProductRequest;
import gift.product.dto.ProductResponse;
import gift.product.repository.ProductRepository;
import gift.common.dto.PageResponse;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(final ProductRepository productRepository, final CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> getProducts(final Pageable pageable) {
        return PageResponse.from(productRepository.findAll(pageable).map(ProductResponse::from));
    }

    @Transactional(readOnly = true)
    public ProductResponse getProduct(final Long id) {
        final Product product = productRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("상품을 찾을 수 없습니다."));
        return ProductResponse.from(product);
    }

    @Transactional
    public ProductResponse createProduct(final ProductRequest request) {
        validateName(request.name());
        final Category category = categoryRepository.findById(request.categoryId())
            .orElseThrow(() -> new NoSuchElementException("카테고리를 찾을 수 없습니다."));
        final Product saved = productRepository.save(request.toEntity(category));
        return ProductResponse.from(saved);
    }

    @Transactional
    public ProductResponse updateProduct(final Long id, final ProductRequest request) {
        validateName(request.name());
        final Category category = categoryRepository.findById(request.categoryId())
            .orElseThrow(() -> new NoSuchElementException("카테고리를 찾을 수 없습니다."));
        final Product product = productRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("상품을 찾을 수 없습니다."));
        product.update(request.name(), request.price(), request.imageUrl(), category);
        return ProductResponse.from(productRepository.save(product));
    }

    @Transactional
    public void deleteProduct(final Long id) {
        productRepository.deleteById(id);
    }

    private void validateName(final String name) {
        final List<String> errors = ProductNameValidator.validate(name);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join(", ", errors));
        }
    }
}
