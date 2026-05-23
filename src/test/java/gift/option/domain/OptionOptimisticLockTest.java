package gift.option.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import gift.category.domain.Category;
import gift.category.repository.CategoryRepository;
import gift.option.repository.OptionRepository;
import gift.product.domain.Product;
import gift.product.repository.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@DataJpaTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@DisplayName("Option 낙관적 락")
class OptionOptimisticLockTest {

    @Autowired
    private OptionRepository optionRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private TransactionTemplate txTemplate;
    private Long optionId;
    private Long productId;
    private Long categoryId;

    @BeforeEach
    void setUp() {
        txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.execute(status -> {
            final Category category = categoryRepository.save(
                new Category("테스트카테고리", "#000000", "http://img.jpg", null)
            );
            final Product product = productRepository.save(
                new Product("테스트상품", 10_000, "http://img.jpg", category)
            );
            final Option option = optionRepository.save(new Option(product, "기본옵션", 10));
            optionId = option.getId();
            productId = product.getId();
            categoryId = category.getId();
            return null;
        });
    }

    @AfterEach
    void tearDown() {
        txTemplate.execute(status -> {
            optionRepository.deleteById(optionId);
            productRepository.deleteById(productId);
            categoryRepository.deleteById(categoryId);
            return null;
        });
    }

    @Test
    @DisplayName("동시에 같은 옵션을 수정하면 ObjectOptimisticLockingFailureException이 발생한다.")
    void throwsOnConcurrentModification() {
        // 첫 번째 트랜잭션에서 조회 — 트랜잭션 종료 후 detached (version=0)
        final Option staleOption = txTemplate.execute(status ->
            optionRepository.findById(optionId).orElseThrow()
        );

        // 두 번째 트랜잭션이 먼저 커밋 — DB의 version이 1로 증가
        txTemplate.execute(status -> {
            final Option fresh = optionRepository.findById(optionId).orElseThrow();
            fresh.subtractQuantity(1);
            return null;
        });

        // staleOption(version=0)으로 update 시도 → version mismatch → 예외
        assertThatThrownBy(() -> txTemplate.execute(status -> {
            staleOption.subtractQuantity(1);
            optionRepository.save(staleOption);
            return null;
        })).isInstanceOf(ObjectOptimisticLockingFailureException.class);
    }
}
