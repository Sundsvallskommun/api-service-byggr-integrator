package se.sundsvall.byggrintegrator.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.ReportAsSingleViolation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.hibernate.validator.constraints.CompositionType;
import org.hibernate.validator.constraints.ConstraintComposition;
import se.sundsvall.dept44.common.validators.annotation.ValidOrganizationNumber;
import se.sundsvall.dept44.common.validators.annotation.ValidPersonalNumber;

@Documented
@ConstraintComposition(CompositionType.OR)
@Constraint(validatedBy = {})
@ValidPersonalNumber
@ValidOrganizationNumber
@ReportAsSingleViolation
@Target({
	ElementType.FIELD, ElementType.PARAMETER
})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPersonalOrOrgNumber {

	String message() default "Invalid personal or organization number";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
