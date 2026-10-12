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
 * Request DTO for adding a product attribute
 *
 * @author PIM Team
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Add product attribute request")
public class AddProductAttributeRequest extends BaseRequest {

    private static final long serialVersionUID = 1L;

    @Schema(
        description = "Attribute key",
        example = "color",
        required = true
    )
    @NotBlank(message = "Attribute key is required")
    @Size(min = 1, max = 100, message = "Attribute key must be between 1 and 100 characters")
    private String key;

    @Schema(
        description = "Attribute value",
        example = "Black",
        required = true
    )
    @NotBlank(message = "Attribute value is required")
    @Size(min = 1, max = 1000, message = "Attribute value must be between 1 and 1000 characters")
    private String value;

    @Schema(
        description = "Is this attribute required?",
        example = "false",
        defaultValue = "false"
    )
    private Boolean required = false;

    @Schema(
        description = "Display order",
        example = "1"
    )
    private Integer displayOrder = 0;

    @Override
    public void validate() {
        super.validate();

        if (key != null) {
            key = key.trim().toLowerCase();
        }
        if (value != null) {
            value = value.trim();
        }
    }
}