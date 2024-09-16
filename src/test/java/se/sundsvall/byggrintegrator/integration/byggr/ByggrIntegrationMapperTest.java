package se.sundsvall.byggrintegrator.integration.byggr;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static se.sundsvall.byggrintegrator.TestObjectFactory.BYGGR_ARENDE_NR_1;
import static se.sundsvall.byggrintegrator.TestObjectFactory.BYGGR_ARENDE_NR_2;
import static se.sundsvall.byggrintegrator.TestObjectFactory.HANDELSESLAG_GRASVA;
import static se.sundsvall.byggrintegrator.TestObjectFactory.createArende;
import static se.sundsvall.byggrintegrator.TestObjectFactory.createPopulatedGetArendeResponse;
import static se.sundsvall.byggrintegrator.TestObjectFactory.generateEmptyRelateradeArendenResponse;
import static se.sundsvall.byggrintegrator.TestObjectFactory.generateRelateradeArendenResponse;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import generated.se.sundsvall.arendeexport.Arende;
import generated.se.sundsvall.arendeexport.ArendeIntressent;
import generated.se.sundsvall.arendeexport.ArrayOfArende1;
import generated.se.sundsvall.arendeexport.ArrayOfArendeIntressent2;
import generated.se.sundsvall.arendeexport.ArrayOfHandelse;
import generated.se.sundsvall.arendeexport.ArrayOfString2;
import generated.se.sundsvall.arendeexport.GetArendeResponse;
import generated.se.sundsvall.arendeexport.Handelse;
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

		// Prepare list of unwanted handelseslag
		setField(mapper, "unwantedHandelseslag", List.of(HANDELSESLAG_GRASVA));

		// Act
		final var byggErrandDtos = mapper.mapToNeighborhoodNotifications(response);

		// Assert
		assertThat(byggErrandDtos).hasSize(1).satisfiesExactly(errand -> {
			assertThat(errand.getByggrCaseNumber()).isEqualTo(BYGGR_ARENDE_NR_1);
			assertThat(errand.getPropertyDesignation()).hasSize(2);

			final var propertyDesignations = errand.getPropertyDesignation();
			assertThat(propertyDesignations).hasSize(2);
			assertThat(propertyDesignations).extracting("property", "designation")
				.containsExactlyInAnyOrder(
					tuple("ANKEBORG", "1:1234"),
					tuple("ANKEBORG", "2:5678"));
		});
	}

	@Test
	void testMapToNeighborhoodNotifications_noUnwantedEventsValidation1() {
		// Arrange
		final var response = generateRelateradeArendenResponse();

		// Act
		final var byggErrandDtos = mapper.mapToNeighborhoodNotifications(response);

		// Assert
		assertThat(byggErrandDtos).hasSize(2).satisfiesExactlyInAnyOrder(errand -> {
			assertThat(errand.getByggrCaseNumber()).isEqualTo(BYGGR_ARENDE_NR_1);
			assertThat(errand.getPropertyDesignation()).hasSize(2);

			final var propertyDesignations = errand.getPropertyDesignation();
			assertThat(propertyDesignations).hasSize(2);
			assertThat(propertyDesignations).extracting("property", "designation")
				.containsExactlyInAnyOrder(
					tuple("ANKEBORG", "1:1234"),
					tuple("ANKEBORG", "2:5678"));
		}, errand -> {
			assertThat(errand.getByggrCaseNumber()).isEqualTo(BYGGR_ARENDE_NR_2);
			assertThat(errand.getPropertyDesignation()).hasSize(2);

			final var propertyDesignations = errand.getPropertyDesignation();
			assertThat(propertyDesignations).hasSize(2);
			assertThat(propertyDesignations).extracting("property", "designation")
				.containsExactlyInAnyOrder(
					tuple("ANKEBORG", "1:1234"),
					tuple("ANKEBORG", "2:5678"));
		});
	}

	@Test
	void testMapToNeighborhoodNotifications_noValidEvents() {
		// Arrange
		final var response = generateRelateradeArendenResponse();

		// Prepare list of unwanted handelseslag
		setField(mapper, "unwantedHandelseslag", List.of(HANDELSESLAG_GRASVA));

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
			assertThat(errand.getByggrCaseNumber()).isEqualTo("BYGG 2024-000123");
		}, errand -> {
			assertThat(errand.getByggrCaseNumber()).isEqualTo("BYGG 2024-000234");
		});
	}

	@Test
	void testMapToNeighborhoodNotificationFiles() {
		final var response = createPopulatedGetArendeResponse();
		final var errandDto = mapper.mapToNeighborhoodNotificationFiles(response);

		assertThat(errandDto).isNotNull();
		assertThat(errandDto.getPropertyDesignation()).isNull();
		assertThat(errandDto.getByggrCaseNumber()).isEqualTo("BYGG 2024-000123");
		assertThat(errandDto.getFiles()).isNotEmpty();
		assertThat(errandDto.getFiles()).containsExactly(entry("documentId", "documentName"));
	}

	@ParameterizedTest
	@MethodSource("getArendeResponseProvider")
	void testNullValues(GetArendeResponse response) {
		final var byggrErrandDto = mapper.mapToNeighborhoodNotificationFiles(response);
		assertThat(byggrErrandDto).isNotNull();
		assertThat(byggrErrandDto.getFiles()).isNotNull().isEmpty();
	}

	static Stream<GetArendeResponse> getArendeResponseProvider() {
		return Stream.of(
			null,
			new GetArendeResponse(),
			new GetArendeResponse().withGetArendeResult(new Arende()),
			new GetArendeResponse().withGetArendeResult(new Arende().withHandelseLista(null)),
			new GetArendeResponse().withGetArendeResult(new Arende().withHandelseLista(new ArrayOfHandelse())),
			new GetArendeResponse().withGetArendeResult(new Arende().withHandelseLista(new ArrayOfHandelse().withHandelse(Collections.emptyList()))),
			new GetArendeResponse().withGetArendeResult(new Arende().withHandelseLista(new ArrayOfHandelse().withHandelse(List.of(new Handelse())))));
	}

	@Test
	void testMapToGetArendeRequest() {
		final var dnr = "dnr";
		final var request = mapper.mapToGetArendeRequest(dnr);

		assertThat(request.getDnr()).isEqualTo(dnr);
	}

	@Test
	void testMapToGetDocumentRequest() {
		final var fileId = "111222";
		final var request = mapper.mapToGetDocumentRequest(fileId);

		assertThat(request.getDocumentId()).isEqualTo(fileId);
		assertThat(request.isInkluderaFil()).isTrue();
	}
}
