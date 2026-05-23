package gift.wish.api;

import gift.common.dto.PageResponse;
import gift.member.dto.MemberInfo;
import gift.wish.dto.WishAddResult;
import gift.wish.dto.WishRequest;
import gift.wish.dto.WishResponse;
import gift.wish.service.WishService;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wishes")
class WishController {

    private final WishService wishService;

    public WishController(final WishService wishService) {
        this.wishService = wishService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<WishResponse>> getWishes(
        final MemberInfo memberInfo,
        final Pageable pageable
    ) {
        return ResponseEntity.ok(wishService.getWishes(memberInfo.id(), pageable));
    }

    @PostMapping
    public ResponseEntity<WishResponse> addWish(
        final MemberInfo memberInfo,
        @Valid @RequestBody final WishRequest request
    ) {
        final WishAddResult result = wishService.addWish(memberInfo.id(), request);

        if (result.created()) {
            return ResponseEntity.created(URI.create("/api/wishes/" + result.response().id()))
                .body(result.response());
        }

        return ResponseEntity.ok(result.response());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeWish(
        final MemberInfo memberInfo,
        @PathVariable final Long id
    ) {
        wishService.removeWish(memberInfo.id(), id);

        return ResponseEntity.noContent().build();
    }
}
