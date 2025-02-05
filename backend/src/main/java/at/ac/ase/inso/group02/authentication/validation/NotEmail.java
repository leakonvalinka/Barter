package at.ac.ase.inso.group02.authentication.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = NotEmailValidator.class)
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface NotEmail {
    String message() default "Username must not be a valid email address";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
