package se.sundsvall.byggrintegrator.api.model;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "Decision on a ByggR errand")
public record Decision(
	@Schema(description = "ByggR event id of the decision", examples = "292453") int id,

	@Schema(description = "Decision number", examples = "ST 2024-000002") String decisionNumber,

	@Schema(description = "Heading of the decision", examples = "Strandskyddsdispens, Beviljas") String heading,

	@Schema(description = "Date of the decision", examples = "2024-09-02") LocalDate decisionDate,

	@Schema(description = "Outcome code of the decision", examples = "BEV") String outcome,

	@Schema(description = "Whether the decision is the main decision of the errand", examples = "true") boolean mainDecision,

	@Schema(description = "Instance type of the decision", examples = "DelgSTA") String instanceType,

	@Schema(description = "Date until the decision is valid", examples = "2029-09-02") LocalDate validUntil,

	@ArraySchema(arraySchema = @Schema(description = "Documents classified as decision documents"), schema = @Schema(implementation = DecisionDocument.class)) List<DecisionDocument> documents) {
}
