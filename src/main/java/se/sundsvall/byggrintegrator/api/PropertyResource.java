package se.sundsvall.byggrintegrator.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.sundsvall.byggrintegrator.api.model.OVKProtocol;
import se.sundsvall.byggrintegrator.api.validation.ValidPropertyDesignation;
import se.sundsvall.byggrintegrator.service.ByggrIntegratorService;
import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.dept44.problem.violations.ConstraintViolationProblem;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;

@RestController
@Validated
@RequestMapping(path = "/{municipalityId}")
@Tag(name = "Property", description = "Property resources")
@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = {
	Problem.class, ConstraintViolationProblem.class
})))
@ApiResponse(responseCode = "500", description = "Internal Server error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
@ApiResponse(responseCode = "502", description = "Bad Gateway", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
class PropertyResource {

	private final ByggrIntegratorService byggrIntegratorService;

	public PropertyResource(ByggrIntegratorService byggrIntegratorService) {
		this.byggrIntegratorService = byggrIntegratorService;
	}

	@GetMapping(path = "/properties/{propertyDesignation}/errands/ovk/latest", produces = APPLICATION_JSON_VALUE)
	@Operation(summary = "Returns the latest OVK protocol for the property matching the provided property designation",
		description = "Returns the OVK protocol with the most recent date among all OVK documents found on errands connected to the property. Returns 404 if no OVK protocol is found",
		responses = {
			@ApiResponse(responseCode = "200", description = "Successful Operation", useReturnTypeSchema = true),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
		})
	ResponseEntity<OVKProtocol> getLatestOVKProtocol(
		@Parameter(name = "municipalityId", description = "Municipality ID", example = "2281") @PathVariable @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "propertyDesignation", description = "Property designation", example = "TESTÖN 1:123") @ValidPropertyDesignation @PathVariable final String propertyDesignation) {
		return ResponseEntity.ok(byggrIntegratorService.getLatestOVKprotocol(municipalityId, propertyDesignation));
	}

	@GetMapping(path = "/properties/{propertyDesignation}/errands/ovk", produces = APPLICATION_JSON_VALUE)
	@Operation(summary = "Lists all OVK protocols for the property matching the provided property designation",
		description = "Returns documents of type OVK found on errands connected to the property, including closed errands. A document referenced by several events on the same errand is only returned once, with the date of the earliest event",
		responses = {
			@ApiResponse(responseCode = "200", description = "Successful Operation", useReturnTypeSchema = true)
		})
	ResponseEntity<List<OVKProtocol>> getAllOVKProtocols(
		@Parameter(name = "municipalityId", description = "Municipality ID", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "propertyDesignation", description = "Property designation", example = "TESTÖN 1:123") @ValidPropertyDesignation @PathVariable final String propertyDesignation) {
		return ResponseEntity.ok(byggrIntegratorService.getOVKprotocols(municipalityId, propertyDesignation));

	}
}
