package se.sundsvall.byggrintegrator.api.model;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Decisions with decision documents for a ByggR errand")
public record ErrandDecisions(
	@Schema(description = "ByggR case number (diary number) that the decisions belong to", examples = "BYGG 2024-000666") String caseNumber,

	@Schema(description = "Description of the errand", examples = "Nybyggnad av garage") String description,

	@Schema(description = "Property designation of the errand", examples = "SUNDSVALL 2:55") String propertyDesignation,

	@ArraySchema(arraySchema = @Schema(description = "Decisions on the errand, newest first"), schema = @Schema(implementation = Decision.class)) List<Decision> decisions) {
}
