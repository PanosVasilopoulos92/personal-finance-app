package org.viators.personalfinanceapp.annotations.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import org.viators.personalfinanceapp.dto.inflationcalc.request.CreateInflationCalculationRequest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidDateRange {
    String message() default "Start date is after end date";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    class StartDateAfterEndDateValidator implements ConstraintValidator<ValidDateRange, CreateInflationCalculationRequest> {

        @Override
        public boolean isValid(CreateInflationCalculationRequest request, ConstraintValidatorContext ctx) {
            if (request == null || request.startDate() == null || request.endDate() == null) {
                return true;
            }

            boolean startDateAfterEndDate = request.startDate().isAfter(request.endDate());
            if (!startDateAfterEndDate) {
                ctx.disableDefaultConstraintViolation();
                ctx.buildConstraintViolationWithTemplate(ctx.getDefaultConstraintMessageTemplate())
                        .addPropertyNode("dataRange")
                        .addConstraintViolation();
            }

            return startDateAfterEndDate;
        }
    }
}
