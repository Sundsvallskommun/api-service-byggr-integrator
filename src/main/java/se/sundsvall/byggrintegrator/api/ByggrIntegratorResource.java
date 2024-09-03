package se.sundsvall.byggrintegrator.api;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.problem.Problem;
import org.zalando.problem.violations.ConstraintViolationProblem;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import se.sundsvall.byggrintegrator.api.model.KeyValue;
import se.sundsvall.byggrintegrator.api.validation.ValidPersonalOrOrgNumber;
import se.sundsvall.byggrintegrator.service.ByggrIntegratorService;
import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;

@RestController
@Validated
@Tag(name = "ByggR Integrator", description = "ByggR Integrator resources")
@RequestMapping(path = "/{municipalityId}")
@ApiResponse(responseCode = "200", description = "Successful Operation", content = @Content(schema = @Schema()))
@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(schema = @Schema(oneOf = { Problem.class, ConstraintViolationProblem.class })))
@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(implementation = Problem.class)))
@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = Problem.class)))
@ApiResponse(responseCode = "502", description = "Bad Gateway", content = @Content(schema = @Schema(implementation = Problem.class)))
public class ByggrIntegratorResource {

	private final ByggrIntegratorService byggrIntegratorService;

	public ByggrIntegratorResource(ByggrIntegratorService byggrIntegratorService) {
		this.byggrIntegratorService = byggrIntegratorService;
	}

	@GetMapping(path = "/byggr/neighborhood-notification/{identifier}/errands", produces = { APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE })
	@Operation(summary = "Lists all neighborhood notifications")
	public ResponseEntity<List<KeyValue>> findNeighborhoodNotifications(
		@Parameter(name = "municipalityId", description = "Municipality ID", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "identifier", description = "Personal or organization number") @NotBlank @ValidPersonalOrOrgNumber @PathVariable String identifier) {

		return ResponseEntity.ok(byggrIntegratorService.findNeighborhoodNotifications(identifier));
	}

	@GetMapping(path = "/byggr/applicant/{identifier}/errands", produces = { APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE })
	@Operation(summary = "Lists all errands for a person or an organization")
	public ResponseEntity<List<KeyValue>> findApplicantErrands(
		@Parameter(name = "municipalityId", description = "Municipality ID", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "identifier", description = "Personal or organization number") @NotBlank @ValidPersonalOrOrgNumber @PathVariable String identifier) {

		return ResponseEntity.ok(byggrIntegratorService.findApplicantErrands(identifier));
	}
}
