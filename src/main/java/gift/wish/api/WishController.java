package gift.wish.api;

import gift.auth.AuthenticationResolver;
import gift.common.dto.PageResponse;
import gift.member.domain.Member;
import gift.wish.dto.WishAddResult;
import gift.wish.dto.WishRequest;
import gift.wish.dto.WishResponse;
import gift.wish.service.WishService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.NoSuchElementException;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wishes")
class WishController {

    private final WishService wishService;
    private final AuthenticationResolver authenticationResolver;

    public WishController(final WishService wishService, final AuthenticationResolver authenticationResolver) {
        this.wishService = wishService;
        this.authenticationResolver = authenticationResolver;
    }

    @GetMapping
    public ResponseEntity<PageResponse<WishResponse>> getWishes(
        @RequestHeader("Authorization") final String authorization,
        final Pageable pageable
    ) {
        final Member member = authenticationResolver.extractMember(authorization);
        if (member == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(wishService.getWishes(member.getId(), pageable));
    }

    @PostMapping
    public ResponseEntity<WishResponse> addWish(
        @RequestHeader("Authorization") final String authorization,
        @Valid @RequestBody final WishRequest request
    ) {
        final Member member = authenticationResolver.extractMember(authorization);
        if (member == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            final WishAddResult result = wishService.addWish(member.getId(), request);
            if (result.created()) {
                return ResponseEntity.created(URI.create("/api/wishes/" + result.response().id()))
                    .body(result.response());
            }
            return ResponseEntity.ok(result.response());
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeWish(
        @RequestHeader("Authorization") final String authorization,
        @PathVariable final Long id
    ) {
        final Member member = authenticationResolver.extractMember(authorization);
        if (member == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            wishService.removeWish(member.getId(), id);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(403).build();
        }
    }
}
