package se.sundsvall.byggrintegrator.integration.byggr;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.byggrintegrator.TestObjectFactory.APPLICANT_ROLE;
import static se.sundsvall.byggrintegrator.TestObjectFactory.BYGGR_ARENDE_NR_1;
import static se.sundsvall.byggrintegrator.TestObjectFactory.BYGGR_ARENDE_NR_2;
import static se.sundsvall.byggrintegrator.TestObjectFactory.CASE_APPLICANT;
import static se.sundsvall.byggrintegrator.TestObjectFactory.HANDELSESLAG_GRASVA;
import static se.sundsvall.byggrintegrator.TestObjectFactory.HANDELSESLAG_GRAUTS;
import static se.sundsvall.byggrintegrator.TestObjectFactory.HANDELSETYP_GRANHO;
import static se.sundsvall.byggrintegrator.TestObjectFactory.NEIGHBORHOOD_NOTIFICATION_STAKEHOLDER;
import static se.sundsvall.byggrintegrator.TestObjectFactory.WANTED_DOCUMENT_ID;
import static se.sundsvall.byggrintegrator.TestObjectFactory.WANTED_DOCUMENT_NAME;
import static se.sundsvall.byggrintegrator.TestObjectFactory.generateArendeResponse;
import static se.sundsvall.byggrintegrator.TestObjectFactory.generateRelateradeArendenResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import se.sundsvall.byggrintegrator.Application;
import se.sundsvall.byggrintegrator.model.ByggrErrandDto;
import se.sundsvall.byggrintegrator.model.ByggrErrandDto.Event;

import generated.se.sundsvall.arendeexport.RollTyp;
import generated.se.sundsvall.arendeexport.StatusFilter;

@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
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
	void testMapToByggrErrandDtos() throws Exception {
		// Arrange
		final var response = List.of(generateRelateradeArendenResponse());

		// Act
		final var byggErrandDtos = mapper.mapToByggrErrandDtos(response);

		// Assert
		assertThat(byggErrandDtos).hasSize(2).satisfiesExactlyInAnyOrder(errand -> {
			assertErrandValues(errand, BYGGR_ARENDE_NR_1);

			assertThat(errand.getEvents()).hasSize(2).satisfiesExactlyInAnyOrder(event -> {
				assertThat(event.getId()).isEqualTo(1);
				assertThat(event.getEventType()).isEqualTo(HANDELSETYP_GRANHO);
				assertThat(event.getEventSubtype()).isEqualTo(HANDELSESLAG_GRAUTS);
				assertThat(event.getEventDate()).isEqualTo(LocalDate.now());
				assertEventStakeholders(event);
			}, event -> {
				assertThat(event.getId()).isEqualTo(2);
				assertThat(event.getEventType()).isEqualTo(HANDELSETYP_GRANHO);
				assertThat(event.getEventSubtype()).isEqualTo(HANDELSESLAG_GRAUTS);
				assertThat(event.getEventDate()).isEqualTo(LocalDate.now());
				assertEventStakeholders(event);
			});
		}, errand -> {
			assertErrandValues(errand, BYGGR_ARENDE_NR_2);

			assertThat(errand.getEvents()).hasSize(2).satisfiesExactlyInAnyOrder(event -> {
				assertThat(event.getId()).isEqualTo(1);
				assertThat(event.getEventType()).isEqualTo(HANDELSETYP_GRANHO);
				assertThat(event.getEventSubtype()).isEqualTo(HANDELSESLAG_GRASVA);
				assertThat(event.getEventDate()).isEqualTo(LocalDate.now());
				assertEventStakeholders(event);
			}, event -> {
				assertThat(event.getId()).isEqualTo(2);
				assertThat(event.getEventType()).isEqualTo(HANDELSETYP_GRANHO);
				assertThat(event.getEventSubtype()).isEqualTo(HANDELSESLAG_GRAUTS);
				assertThat(event.getEventDate()).isEqualTo(LocalDate.now());
				assertEventStakeholders(event);
			});
		});
	}

	private void assertEventStakeholders(Event event) {
		assertThat(event.getStakeholders()).hasSize(1).satisfiesExactly(stakeholder -> {
			assertThat(stakeholder.getLegalId()).isEqualTo(NEIGHBORHOOD_NOTIFICATION_STAKEHOLDER);
		});
	}

	private void assertErrandValues(ByggrErrandDto errand, String caseNumber) {
		assertThat(errand.getByggrCaseNumber()).isEqualTo(caseNumber);
		assertThat(errand.getStakeholders()).hasSize(1).satisfiesExactly(stakeholder -> {
			assertThat(stakeholder.getLegalId()).isEqualTo(CASE_APPLICANT);
			assertThat(stakeholder.getRoles()).containsExactly(APPLICANT_ROLE);
		});
	}

	@Test
	void testMapToByggrErrandDto() throws Exception {
		// Arrange
		final var dnr = "ByggrDiaryNumber";
		final var response = generateArendeResponse(dnr);

		// Act
		final var byggErrandDto = mapper.mapToByggrErrandDto(response);

		// Assert
		assertThat(byggErrandDto).isNotNull().satisfies(errand -> {
			assertErrandValues(errand, dnr);

			assertThat(errand.getEvents()).hasSize(2).satisfiesExactlyInAnyOrder(event -> {
				assertThat(event.getId()).isEqualTo(1);
				assertThat(event.getEventType()).isEqualTo(HANDELSETYP_GRANHO);
				assertThat(event.getEventSubtype()).isEqualTo(HANDELSESLAG_GRAUTS);
				assertThat(event.getEventDate()).isEqualTo(LocalDate.now());
				assertEventStakeholders(event);
			}, event -> {
				assertThat(event.getId()).isEqualTo(2);
				assertThat(event.getEventType()).isEqualTo(HANDELSETYP_GRANHO);
				assertThat(event.getEventSubtype()).isEqualTo(HANDELSESLAG_GRAUTS);
				assertThat(event.getEventDate()).isEqualTo(LocalDate.now());
				assertEventStakeholders(event);
			});
		});
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

	@Test
	void testMapToErrandDto_shouldOmitUnwantedDocumentTypes() throws Exception {
		final var dnr = "ByggrDiaryNumber";
		final var response = generateArendeResponse(dnr);

		final var byggrErrandDto = mapper.mapToByggrErrandDto(response);

		// Get the files
		final var fileList = byggrErrandDto.getEvents().stream()
			.flatMap(event -> event.getFiles().entrySet().stream())
			.toList();

		// Assert that all files are of the wanted type
		assertThat(fileList.stream().map(Map.Entry::getKey)).allMatch(WANTED_DOCUMENT_ID::equals);
		assertThat(fileList.stream().map(Map.Entry::getValue)).allMatch(WANTED_DOCUMENT_NAME::equals);
	}
}
