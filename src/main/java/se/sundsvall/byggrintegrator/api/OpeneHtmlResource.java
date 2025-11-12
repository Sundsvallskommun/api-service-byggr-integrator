package se.sundsvall.byggrintegrator.api;

import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_HTML_VALUE;
import static org.springframework.http.ResponseEntity.ok;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.problem.Problem;
import org.zalando.problem.violations.ConstraintViolationProblem;
import se.sundsvall.byggrintegrator.service.ByggrIntegratorService;
import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;

@RestController
@Validated
@Tag(name = "Open-E", description = "ByggR Integrator Open-E resources")
@RequestMapping(path = "/{municipalityId}/opene")
@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = {
	Problem.class, ConstraintViolationProblem.class
})))
@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
@ApiResponse(responseCode = "502", description = "Bad Gateway", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
public class OpeneHtmlResource {

	private final ByggrIntegratorService byggrIntegratorService;

	public OpeneHtmlResource(final ByggrIntegratorService byggrIntegratorService) {
		this.byggrIntegratorService = byggrIntegratorService;
	}

	@GetMapping(path = "/neighborhood-notifications/filenames", produces = TEXT_HTML_VALUE)
	@Operation(summary = "Return html structure for all neighborhood-notification files belonging to the case matching sent case number where event stakeholder matches sent in identifier",
		responses = @ApiResponse(responseCode = "200", description = "Successful Operation", useReturnTypeSchema = true))
	public ResponseEntity<String> findNeighborhoodNotificationFilesWithRequestParameter(
		@Parameter(name = "municipalityId", description = "Municipality ID", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "identifier", description = "Personal or organization number", example = "190102031234") @RequestParam final String identifier,
		@Parameter(name = "caseNumber", description = "Case number from ByggR to match", example = "BYGG 2001-123456") @NotBlank @RequestParam final String caseNumber,
		@Parameter(name = "referralReference", description = "Referral reference from ByggR to match", example = "EXAMPLE 1:1 - ej besvarad [167334]") @NotBlank @RequestParam final String referralReference) {

		return ok(byggrIntegratorService.listNeighborhoodNotificationFiles(municipalityId, identifier, caseNumber, referralReference));
	}

	@GetMapping(path = "/cases/property-designation", produces = TEXT_HTML_VALUE)
	@Operation(summary = "Return html structure with the property designations belonging to the case number",
		responses = @ApiResponse(responseCode = "200", description = "Successful Operation", useReturnTypeSchema = true))
	public ResponseEntity<String> findPropertyDesignationWithRequestParameter(
		@Parameter(name = "municipalityId", description = "Municipality ID", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "caseNumber", description = "Case number from ByggR to match", example = "BYGG 2001-123456") @NotBlank @RequestParam final String caseNumber) {

		return ok(byggrIntegratorService.getPropertyDesignation(caseNumber));
	}
}
