package com.example.demo.common.validator;

import com.example.demo.common.validator.constraints.ValidTimezone;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.ZoneId;
import java.util.Set;

/**
 * Timezone validator
 *
 * @author Architecture Team
 * @version 1.0
 */
public class TimezoneValidator implements ConstraintValidator<ValidTimezone, String> {

    private static final Set<String> VALID_TIMEZONES = ZoneId.getAvailableZoneIds();

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }

        return VALID_TIMEZONES.contains(value);
    }
}
