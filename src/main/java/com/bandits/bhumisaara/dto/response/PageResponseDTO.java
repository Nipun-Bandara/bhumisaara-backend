package com.bandits.bhumisaara.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/**
 * The wire shape of a paginated list.
 * <p>
 * Spring's {@code Page} serialises with an unstable, deprecation-warned JSON
 * structure (it carries the whole {@code Pageable} and {@code Sort} object
 * graph), so admin list endpoints return this fixed shape instead. Only the
 * four numbers a client actually needs to render a pager.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponseDTO<T> {

    private List<T> content;

    /** Zero-based, matching the {@code page} query parameter. */
    private int page;

    private int size;

    private long totalElements;

    private int totalPages;

    /** Maps an entity page straight to a DTO page — the only way these are built. */
    public static <E, D> PageResponseDTO<D> from(Page<E> page, Function<E, D> mapper) {
        return PageResponseDTO.<D>builder()
                .content(page.getContent().stream().map(mapper).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }
}
