package com.example.demo.modules.product.application.dto.request;

import com.example.demo.common.base.dto.BaseRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Request DTO for reordering categories
 *
 * @author PIM Team
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Reorder categories request")
public class ReorderCategoriesRequest extends BaseRequest {

    private static final long serialVersionUID = 1L;

    @Schema(
        description = "List of category orders",
        required = true
    )
    @NotNull(message = "Category orders are required")
    @NotEmpty(message = "Category orders cannot be empty")
    @Valid
    private List<CategoryOrder> categoryOrders;

    @Override
    public void validate() {
        super.validate();

        if (categoryOrders != null) {
            categoryOrders.forEach(CategoryOrder::validate);
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Category order")
    public static class CategoryOrder {

        @Schema(
            description = "Category ID",
            example = "550e8400-e29b-41d4-a716-446655440000",
            required = true
        )
        @NotNull(message = "Category ID is required")
        private UUID categoryId;

        @Schema(
            description = "New display order",
            example = "1",
            required = true
        )
        @NotNull(message = "Display order is required")
        private Integer displayOrder;

        public void validate() {
            if (categoryId == null) {
                throw new IllegalArgumentException("Category ID is required");
            }
            if (displayOrder == null || displayOrder < 0) {
                throw new IllegalArgumentException("Display order must be non-negative");
            }
        }
    }
}