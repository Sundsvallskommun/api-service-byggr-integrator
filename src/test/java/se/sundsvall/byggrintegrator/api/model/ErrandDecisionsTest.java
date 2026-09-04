package se.sundsvall.byggrintegrator.api.model;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ErrandDecisionsTest {

	@Test
	void testCreationAndGetters() {
		final var caseNumber = "BYGG 2024-000666";
		final var description = "description";
		final var propertyDesignation = "SUNDSVALL 2:55";
		final var decisions = List.of(new Decision(1, "ST 2024-000002", "heading", LocalDate.now(), "BEV", true, "DelgSTA", null, List.of()));

		final var errandDecisions = new ErrandDecisions(caseNumber, description, propertyDesignation, decisions);

		assertThat(errandDecisions.caseNumber()).isEqualTo(caseNumber);
		assertThat(errandDecisions.description()).isEqualTo(description);
		assertThat(errandDecisions.propertyDesignation()).isEqualTo(propertyDesignation);
		assertThat(errandDecisions.decisions()).isEqualTo(decisions);
	}

	@Test
	void testToString() {
		final var errandDecisions = new ErrandDecisions("BYGG 2024-000666", "description", "SUNDSVALL 2:55", List.of());

		assertThat(errandDecisions.toString())
			.contains("caseNumber=BYGG 2024-000666")
			.contains("description=description")
			.contains("propertyDesignation=SUNDSVALL 2:55")
			.contains("decisions=[]");
	}

	@Test
	void testWithNullValues() {
		final var errandDecisions = new ErrandDecisions(null, null, null, null);

		assertThat(errandDecisions.caseNumber()).isNull();
		assertThat(errandDecisions.description()).isNull();
		assertThat(errandDecisions.propertyDesignation()).isNull();
		assertThat(errandDecisions.decisions()).isNull();
		assertThat(errandDecisions).isEqualTo(new ErrandDecisions(null, null, null, null));
	}
}
