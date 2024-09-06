package se.sundsvall.byggrintegrator.api;

import static org.springframework.http.MediaType.TEXT_HTML_VALUE;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import se.sundsvall.byggrintegrator.service.ByggrIntegratorService;
import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;

@RestController
@Validated
@Tag(name = "File list resource", description = "List files on a neighborhood-notification case")
@RequestMapping(path = "/{municipalityId}")
@ApiResponse(
	responseCode = "200",
	description = "Successful Operation",
	useReturnTypeSchema = true)
public class ByggrFileListResource {

	private final ByggrIntegratorService byggrIntegratorService;

	public ByggrFileListResource(ByggrIntegratorService byggrIntegratorService) {
		this.byggrIntegratorService = byggrIntegratorService;
	}

	@GetMapping(path = "/neighborhood-notification/{caseNumber}/files", produces = { TEXT_HTML_VALUE })
	@Operation(summary = "Lists all files on a neighborhood-notification case")
	public ResponseEntity<String> findNeighborhoodNotificationFiles(
		@Parameter(name = "municipalityId", description = "Municipality ID", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "caseNumber", description = "Case number from ByggR", example = "BYGG 2001-123456") @NotBlank @PathVariable String caseNumber) {

		return ResponseEntity.ok(byggrIntegratorService.listNeighborhoodNotificationFiles(caseNumber));
	}
}