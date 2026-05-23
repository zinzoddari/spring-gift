package gift.product.service;

import gift.category.domain.Category;
import gift.category.repository.CategoryRepository;
import gift.product.domain.Product;
import gift.product.repository.ProductRepository;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public AdminProductService(
        final ProductRepository productRepository,
        final CategoryRepository categoryRepository
    ) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<Product> getProducts() {
        return productRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Category> getCategories() {
        return categoryRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Product getProduct(final Long id) {
        return findProduct(id);
    }

    @Transactional
    public void createProduct(
        final String name,
        final int price,
        final String imageUrl,
        final Long categoryId
    ) {
        final Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new NoSuchElementException("카테고리가 존재하지 않습니다. id=" + categoryId));

        productRepository.save(new Product(name, price, imageUrl, category));
    }

    @Transactional
    public void updateProduct(
        final Long id,
        final String name,
        final int price,
        final String imageUrl,
        final Long categoryId
    ) {
        final Product product = findProduct(id);
        final Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new NoSuchElementException("카테고리가 존재하지 않습니다. id=" + categoryId));

        product.update(name, price, imageUrl, category);
    }

    @Transactional
    public void deleteProduct(final Long id) {
        productRepository.deleteById(id);
    }

    /**
     * ID를 기준으로 Product를 조회하는 메서드입니다.
     * ProductRepository를 통해 데이터를 검색하며, 존재하지 않을 경우 예외를 발생시킵니다.
     */
    private Product findProduct(final Long id) {
        return productRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("상품이 존재하지 않습니다. id=" + id));
    }
}
