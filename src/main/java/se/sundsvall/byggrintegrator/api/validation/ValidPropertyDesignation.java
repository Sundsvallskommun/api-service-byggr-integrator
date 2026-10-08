package se.sundsvall.byggrintegrator.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.ReportAsSingleViolation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Pattern(regexp = "^\\s*\\S.*\\s+\\S+\\s*$")
@ReportAsSingleViolation
@NotBlank
@Constraint(validatedBy = {})
@Target({
		ElementType.FIELD, ElementType.PARAMETER
})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPropertyDesignation {

	String message() default "must be a property designation(fastighetsbeteckning), e.g. 'HÖGOM 3:194'";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
