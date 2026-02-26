package se.sundsvall.byggrintegrator.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import se.sundsvall.byggrintegrator.api.model.KeyValue;
import se.sundsvall.byggrintegrator.api.validation.ValidPersonalOrOrgNumber;
import se.sundsvall.byggrintegrator.service.ByggrIntegratorService;
import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.dept44.problem.violations.ConstraintViolationProblem;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@Validated
@Tag(name = "Neighborhood notifications", description = "Neighborhood notification resources")
@RequestMapping(path = "/{municipalityId}")
@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = {
	Problem.class, ConstraintViolationProblem.class
})))
@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
@ApiResponse(responseCode = "502", description = "Bad Gateway", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
class NeighborhoodNotificationResource {

	private final ByggrIntegratorService byggrIntegratorService;

	NeighborhoodNotificationResource(final ByggrIntegratorService byggrIntegratorService) {
		this.byggrIntegratorService = byggrIntegratorService;
	}

	@GetMapping(path = "/neighborhood-notifications/{identifier}/errands", produces = APPLICATION_JSON_VALUE)
	@Operation(summary = "Lists all neighborhood notifications where the provided identifier is a stakeholder",
		description = "Returns errands filtered by: valid GRANHO events with subtype GRAUTS, excluding configured unwanted event types, matching the identifier as a stakeholder in events not older than 60 days",
		responses = {
			@ApiResponse(responseCode = "200", description = "Successful Operation", useReturnTypeSchema = true),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
		})
	ResponseEntity<List<KeyValue>> findNeighborhoodNotifications(
		@Parameter(name = "municipalityId", description = "Municipality ID", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "identifier", description = "Personal or organization number", example = "190102031234") @NotBlank @ValidPersonalOrOrgNumber @PathVariable final String identifier) {

		return ok(byggrIntegratorService.findNeighborhoodNotifications(identifier));
	}

	@GetMapping(path = "/neighborhood-notifications/properties", produces = APPLICATION_JSON_VALUE)
	@Operation(summary = "Lists all properties that are included in provided neighborhood notification case where the provided identifier is a stakeholder",
		description = "Returns properties filtered by: matching the provided case number, having a property designation, and where the identifier is a stakeholder in the referral",
		responses = {
			@ApiResponse(responseCode = "200", description = "Successful Operation", useReturnTypeSchema = true)
		})
	ResponseEntity<List<KeyValue>> findNeighborhoodNotificationFacilitiesWithRequestParameters(
		@Parameter(name = "municipalityId", description = "Municipality ID", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "identifier", description = "Personal or organization number", example = "190102031234") @NotBlank @ValidPersonalOrOrgNumber @RequestParam final String identifier,
		@Parameter(name = "caseNumber", description = "Case number", example = "BYGG 2024-000559") @NotBlank @RequestParam final String caseNumber) {

		return ok(byggrIntegratorService.getNeighborhoodNotificationFacilities(identifier, caseNumber));
	}

}
