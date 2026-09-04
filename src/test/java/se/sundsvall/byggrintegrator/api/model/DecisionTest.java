package se.sundsvall.byggrintegrator.api.model;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DecisionTest {

	@Test
	void testCreationAndGetters() {
		final var id = 292453;
		final var decisionNumber = "ST 2024-000002";
		final var heading = "Strandskyddsdispens, Beviljas";
		final var decisionDate = LocalDate.of(2024, Month.SEPTEMBER, 2);
		final var outcome = "BEV";
		final var instanceType = "DelgSTA";
		final var validUntil = LocalDate.of(2029, Month.SEPTEMBER, 2);
		final var documents = List.of(new DecisionDocument("id", "name", "description", "type", "typeDescription", decisionDate, "url"));

		final var decision = new Decision(id, decisionNumber, heading, decisionDate, outcome, true, instanceType, validUntil, documents);

		assertThat(decision.id()).isEqualTo(id);
		assertThat(decision.decisionNumber()).isEqualTo(decisionNumber);
		assertThat(decision.heading()).isEqualTo(heading);
		assertThat(decision.decisionDate()).isEqualTo(decisionDate);
		assertThat(decision.outcome()).isEqualTo(outcome);
		assertThat(decision.mainDecision()).isTrue();
		assertThat(decision.instanceType()).isEqualTo(instanceType);
		assertThat(decision.validUntil()).isEqualTo(validUntil);
		assertThat(decision.documents()).isEqualTo(documents);
	}

	@Test
	void testToString() {
		final var decision = new Decision(1, "ST 2024-000002", "heading", null, "BEV", false, "DelgSTA", null, List.of());

		assertThat(decision.toString())
			.contains("id=1")
			.contains("decisionNumber=ST 2024-000002")
			.contains("heading=heading")
			.contains("outcome=BEV")
			.contains("mainDecision=false")
			.contains("instanceType=DelgSTA")
			.contains("documents=[]");
	}

	@Test
	void testWithNullValues() {
		final var decision = new Decision(0, null, null, null, null, false, null, null, null);

		assertThat(decision.id()).isZero();
		assertThat(decision.decisionNumber()).isNull();
		assertThat(decision.heading()).isNull();
		assertThat(decision.decisionDate()).isNull();
		assertThat(decision.outcome()).isNull();
		assertThat(decision.mainDecision()).isFalse();
		assertThat(decision.instanceType()).isNull();
		assertThat(decision.validUntil()).isNull();
		assertThat(decision.documents()).isNull();
		assertThat(decision).isEqualTo(new Decision(0, null, null, null, null, false, null, null, null));
	}
}
