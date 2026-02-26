package se.sundsvall.byggrintegrator.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import se.sundsvall.byggrintegrator.api.model.Weight;
import se.sundsvall.byggrintegrator.service.ByggrIntegratorService;
import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;

import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@Validated
@Tag(name = "Open-E", description = "ByggR Integrator Open-E resources")
@RequestMapping(path = "/{municipalityId}/opene")
@ApiResponse(responseCode = "400", description = "Bad Request", useReturnTypeSchema = true)
@ApiResponse(responseCode = "404", description = "Not Found", useReturnTypeSchema = true)
@ApiResponse(responseCode = "500", description = "Internal Server Error", useReturnTypeSchema = true)
@ApiResponse(responseCode = "502", description = "Bad Gateway", useReturnTypeSchema = true)
public class OpeneXmlResource {

	private final ByggrIntegratorService byggrIntegratorService;

	public OpeneXmlResource(final ByggrIntegratorService byggrIntegratorService) {
		this.byggrIntegratorService = byggrIntegratorService;
	}

	@GetMapping(path = "/cases/type", produces = APPLICATION_XML_VALUE)
	@Operation(summary = "Return xml structure errand type for the errand matching sent in case number",
		responses = @ApiResponse(responseCode = "200", description = "Successful Operation", useReturnTypeSchema = true))
	public ResponseEntity<Weight> getErrandTypeWithRequestParameter(
		@Parameter(name = "municipalityId", description = "Municipality ID", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "caseNumber", description = "Case number from ByggR", example = "BYGG 2001-123456") @NotBlank @RequestParam final String caseNumber) {

		return ok(byggrIntegratorService.getErrandType(caseNumber));
	}
}
