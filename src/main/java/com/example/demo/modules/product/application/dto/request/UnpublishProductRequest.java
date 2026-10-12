package com.example.demo.modules.product.application.dto.request;

import com.example.demo.common.base.dto.BaseRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Request DTO for unpublishing a product
 *
 * @author PIM Team
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Unpublish product request")
public class UnpublishProductRequest extends BaseRequest {

    private static final long serialVersionUID = 1L;

    @Schema(
        description = "Reason for unpublishing",
        example = "Product recall due to quality issue",
        required = true
    )
    @NotBlank(message = "Reason is required")
    @Size(min = 5, max = 500, message = "Reason must be between 5 and 500 characters")
    private String reason;

    @Schema(
        description = "Notify customers who have this in wishlist/cart",
        example = "true",
        defaultValue = "false"
    )
    private Boolean notifyCustomers = false;

    @Override
    public void validate() {
        super.validate();

        if (reason != null) {
            reason = reason.trim();
        }
    }
}