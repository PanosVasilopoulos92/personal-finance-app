package org.viators.personalfinanceapp.common.validators;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import org.viators.personalfinanceapp.user.dto.request.CreateUserRequest;
import org.viators.personalfinanceapp.user.dto.request.UpdateUserPasswordRequest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE}) // At class/record level because it checks two (more than one) fields of same class/record
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {
        PasswordMatch.CreateUserPasswordValidator.class,
        PasswordMatch.UpdatePasswordValidator.class
})
public @interface PasswordMatch {
    String message() default "Passwords do not match";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    class CreateUserPasswordValidator implements ConstraintValidator<PasswordMatch, CreateUserRequest> {
        @Override
        public boolean isValid(CreateUserRequest req, ConstraintValidatorContext ctx) {
            if (req == null || req.password() == null || req.confirmPassword() == null) {
                return true;
            }

            boolean matches = req.password().equals(req.confirmPassword());
            if (!matches) {
                ctx.disableDefaultConstraintViolation();
                ctx.buildConstraintViolationWithTemplate(ctx.getDefaultConstraintMessageTemplate())
                        .addPropertyNode("confirmPassword")
                        .addConstraintViolation();
            }
            return matches;
        }
    }

    class UpdatePasswordValidator implements ConstraintValidator<PasswordMatch, UpdateUserPasswordRequest> {
        @Override
        public boolean isValid(UpdateUserPasswordRequest req, ConstraintValidatorContext ctx) {
            if (req == null || req.newPassword() == null || req.confirmPassword() == null) {
                return true;
            }

            boolean matches = req.newPassword().equals(req.confirmPassword());
            if (!matches) {
                ctx.disableDefaultConstraintViolation();
                ctx.buildConstraintViolationWithTemplate(ctx.getDefaultConstraintMessageTemplate())
                        .addPropertyNode("confirmPassword")
                        .addConstraintViolation();
            }
            return matches;
        }
    }
}

