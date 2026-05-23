package gift.common.dto;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public record PageResponse<T>(
    List<T> content,
    PageableResponse pageable,
    long totalElements,
    int totalPages,
    int number,
    int size,
    int numberOfElements,
    SortResponse sort,
    boolean first,
    boolean last,
    boolean empty
) {
    public static <T> PageResponse<T> from(final Page<T> page) {
        final SortResponse sort = SortResponse.from(page);
        return new PageResponse<>(
            page.getContent(),
            PageableResponse.from(page, sort),
            page.getTotalElements(),
            page.getTotalPages(),
            page.getNumber(),
            page.getSize(),
            page.getNumberOfElements(),
            sort,
            page.isFirst(),
            page.isLast(),
            page.isEmpty()
        );
    }

    public record SortResponse(boolean empty, boolean sorted, boolean unsorted) {
        static SortResponse from(final Page<?> page) {
            return new SortResponse(
                page.getSort().isEmpty(),
                page.getSort().isSorted(),
                page.getSort().isUnsorted()
            );
        }
    }

    public record PageableResponse(
        int pageNumber,
        int pageSize,
        long offset,
        SortResponse sort,
        boolean paged,
        boolean unpaged
    ) {
        static PageableResponse from(final Page<?> page, final SortResponse sort) {
            final Pageable pageable = page.getPageable();
            if (pageable.isUnpaged()) {
                return new PageableResponse(0, 0, 0L, sort, false, true);
            }
            return new PageableResponse(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getOffset(),
                sort,
                true,
                false
            );
        }
    }
}
