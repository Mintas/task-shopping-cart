package ru.kovalev.shopping.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class NotZeroValidatorForInteger implements ConstraintValidator<NotZero, Integer> {

    /**
     * Null values are VALID, sticking to the contract of {@link javax.validation.constraints.Positive}.
     * <p>
     * @return true if value is not zero
     */
    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        if ( value == null ) {
            return true;
        }
        return Integer.signum(value) != 0;
    }
}