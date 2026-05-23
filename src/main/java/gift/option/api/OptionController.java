package gift.option.api;

import gift.option.dto.OptionRequest;
import gift.option.dto.OptionResponse;
import gift.option.service.OptionFacade;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products/{productId}/options")
class OptionController {

    private final OptionFacade optionFacade;

    public OptionController(final OptionFacade optionFacade) {
        this.optionFacade = optionFacade;
    }

    @GetMapping
    public ResponseEntity<List<OptionResponse>> getOptions(@PathVariable final Long productId) {
        return ResponseEntity.ok(optionFacade.getOptions(productId));
    }

    @PostMapping
    public ResponseEntity<OptionResponse> createOption(
        @PathVariable final Long productId,
        @Valid @RequestBody final OptionRequest request
    ) {
        final OptionResponse response = optionFacade.createOption(productId, request);
        final URI location = URI.create("/api/products/" + productId + "/options/" + response.id());
        return ResponseEntity.created(location).body(response);
    }

    @DeleteMapping("/{optionId}")
    public ResponseEntity<Void> deleteOption(
        @PathVariable final Long productId,
        @PathVariable final Long optionId
    ) {
        optionFacade.deleteOption(productId, optionId);
        return ResponseEntity.noContent().build();
    }
}
