package gift.product.api;

import gift.product.dto.ProductRequest;
import gift.product.dto.ProductResponse;
import gift.product.service.ProductService;
import jakarta.validation.Valid;
import gift.common.dto.PageResponse;
import java.net.URI;
import org.springframework.data.domain.Pageable;
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
@RequestMapping("/api/products")
class ProductController {
    private final ProductService productService;

    ProductController(final ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public PageResponse<ProductResponse> getProducts(final Pageable pageable) {
        return productService.getProducts(pageable);
    }

    @GetMapping("/{id}")
    public ProductResponse getProduct(@PathVariable final Long id) {
        return productService.getProduct(id);
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody final ProductRequest request) {
        final ProductResponse response = productService.createProduct(request);

        return ResponseEntity.created(URI.create("/api/products/" + response.id()))
            .body(response);
    }

    @PutMapping("/{id}")
    public ProductResponse updateProduct(
        @PathVariable final Long id,
        @Valid @RequestBody final ProductRequest request
    ) {
        return productService.updateProduct(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable final Long id) {
        productService.deleteProduct(id);

        return ResponseEntity.noContent().build();
    }
}
