package gift.option.service;

import gift.option.dto.OptionRequest;
import gift.option.dto.OptionResponse;
import gift.product.service.ProductService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OptionFacade {

    private final OptionService optionService;
    private final ProductService productService;

    public OptionFacade(
        final OptionService optionService,
        final ProductService productService
    ) {
        this.optionService = optionService;
        this.productService = productService;
    }

    @Transactional(readOnly = true)
    public List<OptionResponse> getOptions(final Long productId) {
        productService.findProduct(productId);

        return optionService.getOptions(productId);
    }

    @Transactional
    public OptionResponse createOption(final Long productId, final OptionRequest request) {
        return optionService.createOption(productId, request);
    }

    @Transactional
    public void deleteOption(final Long productId, final Long optionId) {
        productService.findProduct(productId);

        optionService.deleteOption(productId, optionId);
    }
}
