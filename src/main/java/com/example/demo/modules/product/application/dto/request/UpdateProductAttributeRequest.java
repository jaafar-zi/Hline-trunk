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
 * Request DTO for updating a product attribute value
 *
 * @author PIM Team
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Update product attribute request")
public class UpdateProductAttributeRequest extends BaseRequest {

    private static final long serialVersionUID = 1L;

    @Schema(
        description = "New attribute value",
        example = "Navy Blue",
        required = true
    )
    @NotBlank(message = "Attribute value is required")
    @Size(min = 1, max = 1000, message = "Attribute value must be between 1 and 1000 characters")
    private String value;

    @Schema(
        description = "Display order",
        example = "2"
    )
    private Integer displayOrder;

    @Override
    public void validate() {
        super.validate();

        if (value != null) {
            value = value.trim();
        }
    }
}