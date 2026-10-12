package com.example.demo.modules.product.domain.model.vo;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VariantOption {
        
        @Column(name = "option_name", nullable = false, length = 100)
        private String optionName; // e.g., "Size", "Color"
        
        @Column(name = "value", nullable = false, length = 100)
        private String value; // e.g., "Large", "Red"
        
        @Column(name = "display_order")
        @Builder.Default
        private Integer displayOrder = 0;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof VariantOption)) return false;
            VariantOption that = (VariantOption) o;
            return Objects.equals(optionName, that.optionName) &&
                   Objects.equals(value, that.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(optionName, value);
        }
    }
