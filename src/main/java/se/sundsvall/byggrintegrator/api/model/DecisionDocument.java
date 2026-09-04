package se.sundsvall.byggrintegrator.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(description = "Decision document on a ByggR errand")
public record DecisionDocument(
	@Schema(description = "ByggR document id", examples = "470583") String id,

	@Schema(description = "File name of the document", examples = "Beslut strandskyddsdispens.pdf") String name,

	@Schema(description = "Description of the document", examples = "Beslut") String description,

	@Schema(description = "ByggR document type code", examples = "BESLUT") String type,

	@Schema(description = "Description of the document type", examples = "Beslut") String typeDescription,

	@Schema(description = "Date of the document", examples = "2024-09-03") LocalDate documentDate,

	@Schema(description = "Url for downloading the document. The url is valid for a limited time", examples = "https://api.sundsvall.se/byggr-integrator/2.5/2281/files/470583") String url) {
}
