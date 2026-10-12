package com.example.demo.modules.product.domain.model.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Dimensions Value Object
 * 
 * Represents physical dimensions of a product
 * 
 * Business Rules:
 * - All dimensions must be positive
 * - Precision: 2 decimal places
 * - Default unit: CM/KG
 * - Immutable after creation
 * 
 * @author PIM Team
 * @version 1.0
 */
@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED) // For JPA
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class Dimensions implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final int SCALE = 2;
    private static final BigDecimal MAX_DIMENSION = new BigDecimal("9999.99");

    @Column(name = "length", precision = 10, scale = 2)
    private BigDecimal length;

    @Column(name = "width", precision = 10, scale = 2)
    private BigDecimal width;

    @Column(name = "height", precision = 10, scale = 2)
    private BigDecimal height;

    @Column(name = "weight", precision = 10, scale = 2)
    private BigDecimal weight;

    @Enumerated(EnumType.STRING)
    @Column(name = "dimension_unit", length = 20)
    @Builder.Default
    private DimensionUnit dimensionUnit = DimensionUnit.CM;

    @Enumerated(EnumType.STRING)
    @Column(name = "weight_unit", length = 20)
    @Builder.Default
    private WeightUnit weightUnit = WeightUnit.KG;

    /**
     * Create dimensions with default units (CM, KG)
     */
    public static Dimensions of(
        BigDecimal length,
        BigDecimal width,
        BigDecimal height,
        BigDecimal weight
    ) {
        return of(length, width, height, weight, DimensionUnit.CM, WeightUnit.KG);
    }

    /**
     * Create dimensions with specified units
     */
    public static Dimensions of(
        BigDecimal length,
        BigDecimal width,
        BigDecimal height,
        BigDecimal weight,
        DimensionUnit dimensionUnit,
        WeightUnit weightUnit
    ) {
        validateDimension("Length", length);
        validateDimension("Width", width);
        validateDimension("Height", height);
        validateDimension("Weight", weight);

        return Dimensions.builder()
            .length(normalize(length))
            .width(normalize(width))
            .height(normalize(height))
            .weight(normalize(weight))
            .dimensionUnit(dimensionUnit != null ? dimensionUnit : DimensionUnit.CM)
            .weightUnit(weightUnit != null ? weightUnit : WeightUnit.KG)
            .build();
    }

    /**
     * Create dimensions from double values
     */
    public static Dimensions of(
        double length,
        double width,
        double height,
        double weight
    ) {
        return of(
            BigDecimal.valueOf(length),
            BigDecimal.valueOf(width),
            BigDecimal.valueOf(height),
            BigDecimal.valueOf(weight)
        );
    }

    /**
     * Validate dimension value
     */
    private static void validateDimension(String name, BigDecimal value) {
        if (value == null) {
            throw new IllegalArgumentException(name + " cannot be null");
        }

        if (value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(name + " must be positive");
        }

        if (value.compareTo(MAX_DIMENSION) > 0) {
            throw new IllegalArgumentException(
                name + " cannot exceed " + MAX_DIMENSION
            );
        }
    }

    /**
     * Normalize value to standard scale
     */
    private static BigDecimal normalize(BigDecimal value) {
        return value.setScale(SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Calculate volume
     */
    public BigDecimal calculateVolume() {
        return length
            .multiply(width)
            .multiply(height)
            .setScale(SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Calculate volumetric weight (for shipping)
     * Formula: (L × W × H) / divisor
     */
    public BigDecimal calculateVolumetricWeight(int divisor) {
        if (divisor <= 0) {
            throw new IllegalArgumentException("Divisor must be positive");
        }

        return calculateVolume()
            .divide(BigDecimal.valueOf(divisor), SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Calculate volumetric weight with standard divisor (5000 for CM/KG)
     */
    public BigDecimal calculateVolumetricWeight() {
        int divisor = dimensionUnit == DimensionUnit.CM ? 5000 : 166;
        return calculateVolumetricWeight(divisor);
    }

    /**
     * Get billable weight (higher of actual or volumetric)
     */
    public BigDecimal getBillableWeight() {
        BigDecimal volumetricWeight = calculateVolumetricWeight();
        return weight.max(volumetricWeight);
    }

    /**
     * Convert to different units
     */
    public Dimensions convertTo(DimensionUnit newDimensionUnit, WeightUnit newWeightUnit) {
        BigDecimal convertedLength = convertDimension(length, dimensionUnit, newDimensionUnit);
        BigDecimal convertedWidth = convertDimension(width, dimensionUnit, newDimensionUnit);
        BigDecimal convertedHeight = convertDimension(height, dimensionUnit, newDimensionUnit);
        BigDecimal convertedWeight = convertWeight(weight, weightUnit, newWeightUnit);

        return Dimensions.of(
            convertedLength,
            convertedWidth,
            convertedHeight,
            convertedWeight,
            newDimensionUnit,
            newWeightUnit
        );
    }

    /**
     * Convert dimension value between units
     */
    private BigDecimal convertDimension(
        BigDecimal value,
        DimensionUnit from,
        DimensionUnit to
    ) {
        if (from == to) {
            return value;
        }

        // Convert to CM first, then to target
        BigDecimal inCM = from.toCentimeters(value);
        return to.fromCentimeters(inCM);
    }

    /**
     * Convert weight value between units
     */
    private BigDecimal convertWeight(
        BigDecimal value,
        WeightUnit from,
        WeightUnit to
    ) {
        if (from == to) {
            return value;
        }

        // Convert to KG first, then to target
        BigDecimal inKG = from.toKilograms(value);
        return to.fromKilograms(inKG);
    }

    /**
     * Check if dimensions are oversized (for shipping)
     */
    public boolean isOversized(BigDecimal maxLength, BigDecimal maxGirth) {
        BigDecimal longestSide = length.max(width).max(height);
        
        // Girth = 2 × (width + height) for longest side as length
        BigDecimal girth;
        if (longestSide.equals(length)) {
            girth = width.add(height).multiply(BigDecimal.valueOf(2));
        } else if (longestSide.equals(width)) {
            girth = length.add(height).multiply(BigDecimal.valueOf(2));
        } else {
            girth = length.add(width).multiply(BigDecimal.valueOf(2));
        }

        return longestSide.compareTo(maxLength) > 0 || 
               girth.compareTo(maxGirth) > 0;
    }

    @Override
    public String toString() {
        return String.format(
            "Dimensions[L=%.2f%s, W=%.2f%s, H=%.2f%s, Weight=%.2f%s]",
            length, dimensionUnit.getSymbol(),
            width, dimensionUnit.getSymbol(),
            height, dimensionUnit.getSymbol(),
            weight, weightUnit.getSymbol()
        );
    }

    /**
     * Dimension Unit Enum
     */
    @Getter
    public enum DimensionUnit {
        CM("Centimeters", "cm", BigDecimal.ONE),
        M("Meters", "m", new BigDecimal("100")),
        IN("Inches", "in", new BigDecimal("2.54")),
        FT("Feet", "ft", new BigDecimal("30.48"));

        private final String displayName;
        private final String symbol;
        private final BigDecimal toCmFactor;

        DimensionUnit(String displayName, String symbol, BigDecimal toCmFactor) {
            this.displayName = displayName;
            this.symbol = symbol;
            this.toCmFactor = toCmFactor;
        }

        public BigDecimal toCentimeters(BigDecimal value) {
            return value.multiply(toCmFactor).setScale(SCALE, RoundingMode.HALF_UP);
        }

        public BigDecimal fromCentimeters(BigDecimal valueInCm) {
            return valueInCm.divide(toCmFactor, SCALE, RoundingMode.HALF_UP);
        }
    }

    /**
     * Weight Unit Enum
     */
    @Getter
    public enum WeightUnit {
        KG("Kilograms", "kg", BigDecimal.ONE),
        G("Grams", "g", new BigDecimal("0.001")),
        LB("Pounds", "lb", new BigDecimal("0.453592")),
        OZ("Ounces", "oz", new BigDecimal("0.0283495"));

        private final String displayName;
        private final String symbol;
        private final BigDecimal toKgFactor;

        WeightUnit(String displayName, String symbol, BigDecimal toKgFactor) {
            this.displayName = displayName;
            this.symbol = symbol;
            this.toKgFactor = toKgFactor;
        }

        public BigDecimal toKilograms(BigDecimal value) {
            return value.multiply(toKgFactor).setScale(SCALE, RoundingMode.HALF_UP);
        }

        public BigDecimal fromKilograms(BigDecimal valueInKg) {
            return valueInKg.divide(toKgFactor, SCALE, RoundingMode.HALF_UP);
        }
    }
}
