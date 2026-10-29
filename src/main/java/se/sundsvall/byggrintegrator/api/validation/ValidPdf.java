package se.sundsvall.byggrintegrator.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = ValidPdfConstraintValidator.class)
@Target({
	ElementType.FIELD, ElementType.PARAMETER
})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPdf {

	String message() default "must be a non-empty PDF";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
