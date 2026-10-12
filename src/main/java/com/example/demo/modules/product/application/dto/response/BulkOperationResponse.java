package com.example.demo.modules.product.application.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Bulk Operation Response - Lean version
 *
 * @author PIM Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Bulk operation response")
public class BulkOperationResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Total items requested", example = "100")
    private Integer totalRequested;

    @Schema(description = "Successfully processed items", example = "95")
    private Integer successCount;

    @Schema(description = "Failed items", example = "5")
    private Integer failureCount;

    @Schema(description = "Skipped items", example = "0")
    private Integer skippedCount;

    @Schema(description = "List of successful IDs")
    private List<UUID> successfulIds;

    @Schema(description = "List of failed items with reasons")
    private List<FailedItem> failures;

    @Schema(description = "Overall operation message")
    private String message;

    @Schema(description = "Operation type", example = "DELETE")
    private String operationType;

    @Schema(description = "Processing time in milliseconds", example = "1250")
    private Long processingTimeMs;

    @Schema(description = "Success rate percentage", example = "95.00")
    private BigDecimal successRate;

    // Helper methods
    public boolean isFullySuccessful() {
        return failureCount != null && failureCount == 0 
            && (skippedCount == null || skippedCount == 0);
    }

    public boolean hasFailures() {
        return failureCount != null && failureCount > 0;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Failed item details")
    public static class FailedItem implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "Item ID")
        private UUID id;

        @Schema(description = "Item SKU", example = "SKU-001")
        private String sku;

        @Schema(description = "Failure reason")
        private String reason;

        @Schema(description = "Error code", example = "ALREADY_PUBLISHED")
        private String errorCode;
    }

    @Override
    public String toString() {
        return String.format("BulkOperationResponse[operation=%s, total=%d, success=%d, failed=%d]",
            operationType, totalRequested, successCount, failureCount);
    }
}