package com.example.demo.common.base.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Paginated response wrapper
 *
 * @param <T> Content type
 * @author Architecture Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Paginated response wrapper")
public class PageResponse<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Page content")
    private List<T> content;

    @Schema(description = "Current page number (0-based)", example = "0")
    private Integer pageNumber;

    @Schema(description = "Number of items per page", example = "20")
    private Integer pageSize;

    @Schema(description = "Total number of elements", example = "100")
    private Long totalElements;

    @Schema(description = "Total number of pages", example = "5")
    private Integer totalPages;

    @Schema(description = "Is this the first page", example = "true")
    private Boolean first;

    @Schema(description = "Is this the last page", example = "false")
    private Boolean last;

    @Schema(description = "Has next page", example = "true")
    private Boolean hasNext;

    @Schema(description = "Has previous page", example = "false")
    private Boolean hasPrevious;

    @Schema(description = "Number of elements in current page", example = "20")
    private Integer numberOfElements;

    @Schema(description = "Is the page empty", example = "false")
    private Boolean empty;

    @Schema(description = "Sort information")
    private SortInfo sort;

    // ====================================
    // Factory Methods
    // ====================================

    /**
     * Create PageResponse from Spring Data Page
     */
    public static <T> PageResponse<T> from(Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .numberOfElements(page.getNumberOfElements())
                .empty(page.isEmpty())
                .sort(SortInfo.from(page.getSort()))
                .build();
    }

    /**
     * Create PageResponse with custom content mapping
     */
    public static <T, R> PageResponse<R> from(Page<T> page, List<R> mappedContent) {
        return PageResponse.<R>builder()
                .content(mappedContent)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .numberOfElements(page.getNumberOfElements())
                .empty(page.isEmpty())
                .sort(SortInfo.from(page.getSort()))
                .build();
    }

    /**
     * Create PageResponse with mapper function
     */
    public static <T, R> PageResponse<R> from(Page<T> page, Function<T, R> mapper) {
        List<R> mappedContent = page.getContent().stream()
                .map(mapper)
                .collect(Collectors.toList());

        return from(page, mappedContent);
    }

    /**
     * Create empty PageResponse
     */
    public static <T> PageResponse<T> empty() {
        return PageResponse.<T>builder()
                .content(Collections.emptyList())
                .pageNumber(0)
                .pageSize(0)
                .totalElements(0L)
                .totalPages(0)
                .first(true)
                .last(true)
                .hasNext(false)
                .hasPrevious(false)
                .numberOfElements(0)
                .empty(true)
                .build();
    }

    /**
     * Create PageResponse with single page
     */
    public static <T> PageResponse<T> of(List<T> content) {
        return PageResponse.<T>builder()
                .content(content)
                .pageNumber(0)
                .pageSize(content.size())
                .totalElements((long) content.size())
                .totalPages(1)
                .first(true)
                .last(true)
                .hasNext(false)
                .hasPrevious(false)
                .numberOfElements(content.size())
                .empty(content.isEmpty())
                .build();
    }

    // ====================================
    // Inner Class - Sort Information
    // ====================================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Sort information")
    public static class SortInfo implements Serializable {

        private static final long serialVersionUID = 1L;

        @Schema(description = "Is sorted", example = "true")
        private Boolean sorted;

        @Schema(description = "Is unsorted", example = "false")
        private Boolean unsorted;

        @Schema(description = "Sort orders")
        private List<OrderInfo> orders;

        public static SortInfo from(org.springframework.data.domain.Sort sort) {
            if (sort.isUnsorted()) {
                return SortInfo.builder()
                        .sorted(false)
                        .unsorted(true)
                        .orders(Collections.emptyList())
                        .build();
            }

            List<OrderInfo> orders = sort.stream()
                    .map(order -> OrderInfo.builder()
                            .property(order.getProperty())
                            .direction(order.getDirection().name())
                            .ignoreCase(order.isIgnoreCase())
                            .build())
                    .collect(Collectors.toList());

            return SortInfo.builder()
                    .sorted(true)
                    .unsorted(false)
                    .orders(orders)
                    .build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Sort order information")
    public static class OrderInfo implements Serializable {

        private static final long serialVersionUID = 1L;

        @Schema(description = "Property name", example = "createdAt")
        private String property;

        @Schema(description = "Sort direction", example = "DESC")
        private String direction;

        @Schema(description = "Ignore case", example = "false")
        private Boolean ignoreCase;
    }
}