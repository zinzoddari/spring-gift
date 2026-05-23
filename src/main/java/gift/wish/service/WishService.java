package gift.wish.service;

import gift.common.dto.PageResponse;
import gift.product.domain.Product;
import gift.product.repository.ProductRepository;
import gift.wish.Wish;
import gift.wish.dto.WishAddResult;
import gift.wish.dto.WishRequest;
import gift.wish.dto.WishResponse;
import gift.wish.repository.WishRepository;
import java.util.NoSuchElementException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WishService {

    private final WishRepository wishRepository;
    private final ProductRepository productRepository;

    public WishService(final WishRepository wishRepository, final ProductRepository productRepository) {
        this.wishRepository = wishRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<WishResponse> getWishes(final Long memberId, final Pageable pageable) {
        return PageResponse.from(wishRepository.findByMemberId(memberId, pageable).map(WishResponse::from));
    }

    @Transactional
    public WishAddResult addWish(final Long memberId, final WishRequest request) {
        final Product product = productRepository.findById(request.productId())
            .orElseThrow(() -> new NoSuchElementException("상품을 찾을 수 없습니다."));
        
        return wishRepository.findByMemberIdAndProductId(memberId, product.getId())
            .map(w -> new WishAddResult(WishResponse.from(w), false))
            .orElseGet(() -> {
                final Wish saved = wishRepository.save(new Wish(memberId, product));
                return new WishAddResult(WishResponse.from(saved), true);
            });
    }

    @Transactional
    public void removeWish(final Long memberId, final Long wishId) {
        final Wish wish = wishRepository.findById(wishId)
            .orElseThrow(() -> new NoSuchElementException("위시를 찾을 수 없습니다."));

        if (!wish.isOwnedBy(memberId)) {
            throw new SecurityException("위시 삭제 권한이 없습니다.");
        }

        wishRepository.delete(wish);
    }
}
