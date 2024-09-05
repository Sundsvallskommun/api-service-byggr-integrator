package se.sundsvall.byggrintegrator.integration.byggr;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static se.sundsvall.byggrintegrator.TestObjectFactory.BYGGR_ARENDE_NR_1;
import static se.sundsvall.byggrintegrator.TestObjectFactory.HANDELSESLAG_GRASVA;
import static se.sundsvall.byggrintegrator.TestObjectFactory.createArende;
import static se.sundsvall.byggrintegrator.TestObjectFactory.generateEmptyRelateradeArendenResponse;
import static se.sundsvall.byggrintegrator.TestObjectFactory.generateRelateradeArendenResponse;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import generated.se.sundsvall.arendeexport.ArendeIntressent;
import generated.se.sundsvall.arendeexport.ArrayOfArende1;
import generated.se.sundsvall.arendeexport.ArrayOfArendeIntressent2;
import generated.se.sundsvall.arendeexport.ArrayOfString2;
import generated.se.sundsvall.arendeexport.RollTyp;
import generated.se.sundsvall.arendeexport.StatusFilter;
import se.sundsvall.byggrintegrator.Application;

@SpringBootTest(classes = Application.class)
@ActiveProfiles("junit")
class ByggrIntegrationMapperTest {

	@Autowired
	private ByggrIntegrationMapper mapper;

	@Test
	void testCreateGetRolesRequest() {
		// Act
		final var request = mapper.createGetRolesRequest();

		// Assert
		assertThat(request.getRollTyp()).isEqualTo(RollTyp.INTRESSENT);
		assertThat(request.getStatusfilter()).isEqualTo(StatusFilter.AKTIV);
	}

	@Test
	void testMapToGetRelateradeArendenRequest() {
		// Arrange
		final var id = "1234567890";

		// Act
		final var request = mapper.mapToGetRelateradeArendenRequest(id);

		// Assert
		assertThat(request.getStatusfilter()).isEqualByComparingTo(StatusFilter.AKTIV);
		assertThat(request.getPersOrgNr()).isEqualTo(id);
	}

	@Test
	void testMapToNeighborhoodNotifications() {
		// Arrange
		final var response = generateRelateradeArendenResponse();

		// Act
		final var byggErrandDtos = mapper.mapToNeighborhoodNotifications(response);

		// Assert
		assertThat(byggErrandDtos).hasSize(1);
		assertThat(byggErrandDtos.getFirst().getByggrErrandNumber()).isEqualTo(BYGGR_ARENDE_NR_1);
		assertThat(byggErrandDtos.getFirst().getPropertyDesignation()).hasSize(2);

		final var propertyDesignations = byggErrandDtos.getFirst().getPropertyDesignation();
		assertThat(propertyDesignations).hasSize(2);
		assertThat(propertyDesignations).extracting("property", "designation")
			.containsExactlyInAnyOrder(
				tuple("ANKEBORG", "1:1234"),
				tuple("ANKEBORG", "2:5678"));
	}

	@Test
	void testMapToNeighborhoodNotifications_noValidEvents() {
		// Arrange
		final var response = generateRelateradeArendenResponse();
		// Set all events to invalid
		response.getGetRelateradeArendenByPersOrgNrAndRoleResult().getArende().getFirst().getHandelseLista().getHandelse().getFirst().setHandelseslag(HANDELSESLAG_GRASVA);

		// Act
		final var byggErrandDtos = mapper.mapToNeighborhoodNotifications(response);

		// Assert
		assertThat(byggErrandDtos).isEmpty();
	}

	@Test
	void testMapToApplicantErrands() {
		// Arrange
		final var legalId = "legalId";

		final var arendeList = List.of(
			createArende("BYGG 2024-000123", true)
				.withIntressentLista(new ArrayOfArendeIntressent2()
					.withIntressent(new ArendeIntressent().withPersOrgNr(legalId)
						.withRollLista(new ArrayOfString2().withRoll("SOK")))),
			createArende("BYGG 2024-000234", true)
				.withIntressentLista(new ArrayOfArendeIntressent2()
					.withIntressent(new ArendeIntressent().withPersOrgNr(legalId)
						.withRollLista(new ArrayOfString2().withRoll("KPER")))),
			createArende("BYGG 2024-000345", true)
				.withIntressentLista(new ArrayOfArendeIntressent2()
					.withIntressent(new ArendeIntressent().withPersOrgNr("nonMatchinglegalId")
						.withRollLista(new ArrayOfString2().withRoll("SOK")))),
			createArende("BYGG 2024-000456", true)
				.withIntressentLista(new ArrayOfArendeIntressent2()
					.withIntressent(new ArendeIntressent().withPersOrgNr(legalId)
						.withRollLista(new ArrayOfString2().withRoll("NONMATCHINGROLE")))));

		final var response = generateEmptyRelateradeArendenResponse().withGetRelateradeArendenByPersOrgNrAndRoleResult(new ArrayOfArende1().withArende(arendeList));

		// Act
		final var byggErrandDtos = mapper.mapToApplicantErrands(response, legalId);

		// Assert
		assertThat(byggErrandDtos).hasSize(2).satisfiesExactlyInAnyOrder(errand -> {
			assertThat(errand.getByggrErrandNumber()).isEqualTo("BYGG 2024-000123");
		}, errand -> {
			assertThat(errand.getByggrErrandNumber()).isEqualTo("BYGG 2024-000234");
		});
	}
}
