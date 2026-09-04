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
import se.sundsvall.byggrintegrator.api.model.ErrandDecisions;
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
@Tag(name = "Applicant", description = "Applicant resources")
@RequestMapping(path = "/{municipalityId}")
@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = {
	Problem.class, ConstraintViolationProblem.class
})))
@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
@ApiResponse(responseCode = "502", description = "Bad Gateway", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
class ApplicantResource {

	private final ByggrIntegratorService byggrIntegratorService;

	ApplicantResource(final ByggrIntegratorService byggrIntegratorService) {
		this.byggrIntegratorService = byggrIntegratorService;
	}

	@GetMapping(path = "/applicants/{identifier}/errands", produces = APPLICATION_JSON_VALUE)
	@Operation(summary = "Lists all errands where the provided identifier is applicant", responses = {
		@ApiResponse(responseCode = "200", description = "Successful Operation", useReturnTypeSchema = true)
	})
	public ResponseEntity<List<KeyValue>> findApplicantErrands(
		@Parameter(name = "municipalityId", description = "Municipality ID", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "identifier", description = "Personal or organization number", example = "190102031234") @NotBlank @ValidPersonalOrOrgNumber @PathVariable final String identifier) {

		return ok(byggrIntegratorService.findApplicantErrands(identifier));
	}

	@GetMapping(path = "/applicants/{identifier}/errands/decisions", produces = APPLICATION_JSON_VALUE)
	@Operation(summary = "Lists all decisions with decision documents for the errand matching the provided case number where the provided identifier is applicant",
		description = "Returns decisions filtered by: events with configured decision event types that are neither cancelled nor secret, containing documents with configured decision document types. Document urls are valid for a limited time",
		responses = {
			@ApiResponse(responseCode = "200", description = "Successful Operation", useReturnTypeSchema = true),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
		})
	public ResponseEntity<ErrandDecisions> findApplicantErrandDecisions(
		@Parameter(name = "municipalityId", description = "Municipality ID", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "identifier", description = "Personal or organization number", example = "190102031234") @NotBlank @ValidPersonalOrOrgNumber @PathVariable final String identifier,
		@Parameter(name = "caseNumber", description = "Case number from ByggR to match", example = "BYGG 2024-000559") @NotBlank @RequestParam final String caseNumber) {

		return ok(byggrIntegratorService.getDecisions(municipalityId, identifier, caseNumber));
	}
}
