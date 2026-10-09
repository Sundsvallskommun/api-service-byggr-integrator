package se.sundsvall.byggrintegrator.api.model;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "OVK protocols registerd to property")
public record OVKProtocol(
	@Schema(description = "Byggr casenumber") String caseNumber,
	@Schema(description = "Description of the document", examples = "OVK-protokoll") String description,
	@Schema(description = "Date of event the document is attached to") LocalDate date,
	@Schema(description = "File name of document") String documentName,
	@Schema(description = "Id of document file") String fileId,
	@Schema(description = "Link to download document, valid for a limited time") String downloadUrl) {
}
