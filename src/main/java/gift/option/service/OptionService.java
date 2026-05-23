package gift.option.service;

import gift.option.Option;
import gift.option.OptionNameValidator;
import gift.option.dto.OptionRequest;
import gift.option.dto.OptionResponse;
import gift.option.repository.OptionRepository;
import gift.product.domain.Product;
import gift.product.repository.ProductRepository;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OptionService {

    private final OptionRepository optionRepository;
    private final ProductRepository productRepository;

    public OptionService(
        final OptionRepository optionRepository,
        final ProductRepository productRepository
    ) {
        this.optionRepository = optionRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<OptionResponse> getOptions(final Long productId) {
        return optionRepository.findByProductId(productId).stream()
            .map(OptionResponse::from)
            .toList();
    }

    @Transactional
    public OptionResponse createOption(final Long productId, final OptionRequest request) {
        final List<String> errors = OptionNameValidator.validate(request.name());
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join(", ", errors));
        }

        final Product product = productRepository.findById(productId)
            .orElseThrow(() -> new NoSuchElementException("상품을 찾을 수 없습니다."));

        if (optionRepository.existsByProductIdAndName(productId, request.name())) {
            throw new IllegalArgumentException("이미 존재하는 옵션명입니다.");
        }

        final Option saved = optionRepository.save(new Option(product, request.name(), request.quantity()));
        return OptionResponse.from(saved);
    }

    @Transactional
    public void deleteOption(final Long productId, final Long optionId) {
        final List<Option> options = optionRepository.findByProductId(productId);
        if (options.size() <= 1) {
            throw new IllegalArgumentException("옵션이 1개인 상품은 옵션을 삭제할 수 없습니다.");
        }

        final Option option = optionRepository.findById(optionId)
            .orElseThrow(() -> new NoSuchElementException("옵션을 찾을 수 없습니다."));

        optionRepository.delete(option);
    }
}
