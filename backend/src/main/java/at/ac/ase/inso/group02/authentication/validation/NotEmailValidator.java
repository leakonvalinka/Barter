package at.ac.ase.inso.group02.authentication.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.hibernate.validator.internal.constraintvalidators.bv.EmailValidator;

public class NotEmailValidator implements ConstraintValidator<NotEmail, String> {

    private final EmailValidator emailValidator = new EmailValidator();

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return true;
        }
        return !emailValidator.isValid(value, context);
    }
}
