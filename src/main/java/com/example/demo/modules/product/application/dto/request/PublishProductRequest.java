package com.example.demo.modules.product.application.dto.request;

import com.example.demo.common.base.dto.BaseRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Request DTO for publishing a product
 *
 * @author PIM Team
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Publish product request")
public class PublishProductRequest extends BaseRequest {

    private static final long serialVersionUID = 1L;

    @Schema(
        description = "Schedule publication date (leave null for immediate)",
        example = "2024-02-01T00:00:00"
    )
    private LocalDateTime publishAt;

    @Schema(
        description = "Available from date",
        example = "2024-02-01T00:00:00"
    )
    private LocalDateTime availableFrom;

    @Schema(
        description = "Available until date",
        example = "2024-12-31T23:59:59"
    )
    private LocalDateTime availableUntil;

    @Schema(
        description = "Force publish even if validation warnings exist",
        example = "false",
        defaultValue = "false"
    )
    private Boolean forcePublish = false;

    @Override
    public void validate() {
        super.validate();

        if (availableFrom != null && availableUntil != null) {
            if (availableFrom.isAfter(availableUntil)) {
                throw new IllegalArgumentException("Available from must be before available until");
            }
        }
    }
}