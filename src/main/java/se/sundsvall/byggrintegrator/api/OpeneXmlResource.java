package se.sundsvall.byggrintegrator.api;

import static org.springframework.http.MediaType.APPLICATION_PROBLEM_XML_VALUE;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;

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
import se.sundsvall.byggrintegrator.api.model.Weight;
import se.sundsvall.byggrintegrator.service.ByggrIntegratorService;
import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;

@RestController
@Validated
@Tag(name = "Open-E", description = "ByggR Integrator Open-E resources (returning XML responses)")
@RequestMapping(path = "/{municipalityId}/opene")
@ApiResponse(responseCode = "200", description = "Successful Operation", content = @Content(schema = @Schema()))
@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(schema = @Schema(oneOf = { Problem.class, ConstraintViolationProblem.class })))
@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(implementation = Problem.class)))
@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = Problem.class)))
@ApiResponse(responseCode = "502", description = "Bad Gateway", content = @Content(schema = @Schema(implementation = Problem.class)))
public class OpeneXmlResource {

	private final ByggrIntegratorService byggrIntegratorService;

	public OpeneXmlResource(ByggrIntegratorService byggrIntegratorService) {
		this.byggrIntegratorService = byggrIntegratorService;
	}

	@GetMapping(path = "/errand/{dnr}/type", produces = { APPLICATION_XML_VALUE, APPLICATION_PROBLEM_XML_VALUE })
	@Operation(summary = "Return errand type for the errand matching sent in diary number")
	public Weight getErrandType(
		@Parameter(name = "municipalityId", description = "Municipality ID", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "dnr", description = "Document diarie number") @NotBlank @PathVariable String dnr) {

		return byggrIntegratorService.getErrandType(dnr);
	}
}
