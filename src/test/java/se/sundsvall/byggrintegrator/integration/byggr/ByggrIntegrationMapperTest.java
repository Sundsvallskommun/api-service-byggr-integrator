package se.sundsvall.byggrintegrator.integration.byggr;

import generated.se.sundsvall.arendeexport.v8.Arende;
import generated.se.sundsvall.arendeexport.v8.ArrayOfArende1;
import generated.se.sundsvall.arendeexport.v8.GetRelateradeArendenByFastighetResponse;
import generated.se.sundsvall.arendeexport.v8.RollTyp;
import generated.se.sundsvall.arendeexport.v8.StatusFilter;
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
import se.sundsvall.dept44.problem.ThrowableProblem;

import static generated.se.sundsvall.arendeexport.v4.RemissStatusFilter.EJ_BESVARAD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
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
import static se.sundsvall.byggrintegrator.TestObjectFactory.WANTED_DOKUMENT_TYPE;
import static se.sundsvall.byggrintegrator.TestObjectFactory.generateArendeResponse;
import static se.sundsvall.byggrintegrator.TestObjectFactory.generateRelateradeArendenResponse;

@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("junit")
class ByggrIntegrationMapperTest {

	@Autowired
	private ByggrIntegrationMapper mapper;

	@Test
	void testCreateGetRolesRequest() {
		// Act
		var request = mapper.createGetRolesRequest();

		// Assert
		assertThat(request.getRollTyp()).isEqualTo(RollTyp.INTRESSENT);
		assertThat(request.getStatusfilter()).isEqualTo(StatusFilter.AKTIV);
	}

	@Test
	void testMapToGetRelateradeArendenRequest() {
		// Arrange
		var id = "1234567890";

		// Act
		var request = mapper.mapToGetRelateradeArendenRequest(id);

		// Assert
		assertThat(request.getStatusfilter()).isEqualByComparingTo(StatusFilter.AKTIV);
		assertThat(request.getPersOrgNr()).isEqualTo(id);
	}

	@Test
	void testMapToByggrErrandDtos() throws Exception {
		// Arrange
		var response = List.of(generateRelateradeArendenResponse());
		final var wantedFiles = Map.of("wantedDocumentId", new Event.DocumentNameAndType("wantedDocumentName", "WANTED"));

		// Act
		var byggErrandDtos = mapper.mapToByggrErrandDtos(response);

		// Assert
		assertThat(byggErrandDtos).hasSize(2).satisfiesExactlyInAnyOrder(errand -> {
			assertErrandValues(errand, BYGGR_ARENDE_NR_1);
			assertThat(errand.getEvents())
				.extracting(Event::getId, Event::getEventType, Event::getEventSubtype, Event::getEventDate, Event::getFiles)
				.containsExactlyInAnyOrder(
					tuple(1, HANDELSETYP_GRANHO, HANDELSESLAG_GRAUTS, LocalDate.now(), wantedFiles),
					tuple(2, HANDELSETYP_GRANHO, HANDELSESLAG_GRAUTS, LocalDate.now(), Map.of()));
			errand.getEvents().forEach(this::assertEventStakeholders);
		}, errand -> {
			assertErrandValues(errand, BYGGR_ARENDE_NR_2);
			assertThat(errand.getEvents())
				.extracting(Event::getId, Event::getEventType, Event::getEventSubtype, Event::getEventDate, Event::getFiles)
				.containsExactlyInAnyOrder(
					tuple(1, HANDELSETYP_GRANHO, HANDELSESLAG_GRASVA, LocalDate.now(), wantedFiles),
					tuple(2, HANDELSETYP_GRANHO, HANDELSESLAG_GRAUTS, LocalDate.now(), Map.of()));
			errand.getEvents().forEach(this::assertEventStakeholders);
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
		var dnr = "ByggrDiaryNumber";
		var response = generateArendeResponse(dnr);

		// Act
		var byggErrandDto = mapper.mapToByggrErrandDto(response);

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
		var dnr = "dnr";
		var request = mapper.mapToGetArendeRequest(dnr);

		assertThat(request.getDnr()).isEqualTo(dnr);
	}

	@Test
	void testMapToGetDocumentRequest() {
		var fileId = "111222";
		var request = mapper.mapToGetDocumentRequest(fileId);

		assertThat(request.getDocumentId()).isEqualTo(fileId);
		assertThat(request.isInkluderaFil()).isTrue();
	}

	@Test
	void testMapToErrandDto_shouldOmitUnwantedDocumentTypes() throws Exception {
		var dnr = "ByggrDiaryNumber";
		var response = generateArendeResponse(dnr);

		var byggrErrandDto = mapper.mapToByggrErrandDto(response);

		// Get the files
		var fileList = byggrErrandDto.getEvents().stream()
			.flatMap(event -> event.getFiles().entrySet().stream())
			.toList();

		var wantedDocumentNameAndType = new Event.DocumentNameAndType(WANTED_DOCUMENT_NAME, WANTED_DOKUMENT_TYPE);

		// Assert that all files are of the wanted type
		assertThat(fileList.stream().map(Map.Entry::getKey)).allMatch(WANTED_DOCUMENT_ID::equals);
		assertThat(fileList.stream().map(Map.Entry::getValue)).allMatch(wantedDocumentNameAndType::equals);
	}

	@Test
	void testCreateGetHandlingTyperRequest() {
		var request = mapper.createGetHandlingTyperRequest();

		assertThat(request.getStatusfilter()).isEqualTo(StatusFilter.NONE);
	}

	@Test
	void testToGetRemisserByPersOrgNrRequest() {
		var identifier = "160001011234";

		var request = mapper.toGetRemisserByPersOrgNrRequest(identifier);

		assertThat(request.getPersOrgNr()).isEqualTo(identifier);
		assertThat(request.getStatusFilter()).isEqualTo(EJ_BESVARAD);
	}

	@Test
	void testMapToGetRelateradeArendenByFastighetRequest() {
		// Arrange
		var propertyDesignation = "TESTÖN 1:123";

		// Act
		var request = mapper.mapToGetRelateradeArendenByFastighetRequest(propertyDesignation);

		// Assert
		assertThat(request.getTrakt()).isEqualTo("TESTÖN");
		assertThat(request.getFBetNr()).isEqualTo("1:123");
		assertThat(request.getFnr()).isNull();
		assertThat(request.isArHuvudObjekt()).isNull();
		assertThat(request.getStatusFilter()).isEqualTo(StatusFilter.NONE);
	}

	@Test
	void testMapToGetRelateradeArendenByFastighetRequest_traktWithSpaces() {
		// Arrange
		var propertyDesignation = "NORRA TESTBERGET 1:1";

		// Act
		var request = mapper.mapToGetRelateradeArendenByFastighetRequest(propertyDesignation);

		// Assert
		assertThat(request.getTrakt()).isEqualTo("NORRA TESTBERGET");
		assertThat(request.getFBetNr()).isEqualTo("1:1");
	}

	@Test
	void testMapToGetRelateradeArendenByFastighetRequest_invalidPropertyDesignation() {
		// Arrange
		var propertyDesignation = "TESTÖN";
		// Act
		var exception = assertThrows(ThrowableProblem.class, () -> mapper.mapToGetRelateradeArendenByFastighetRequest(propertyDesignation));

		// Assert
		assertThat(exception.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(exception.getDetail()).isEqualTo("Invalid property designation: " + propertyDesignation);
	}

	@Test
	void testMapRelateradeArendenByFastighetToByggrErrandDtos() {
		// Arrange
		final var arende = new Arende();
		arende.setDnr("OVK 2026-222222");
		arende.setBeskrivning("Funktionskontroll");

		final var result = new ArrayOfArende1();
		result.getArende().add(arende);

		final var response = new GetRelateradeArendenByFastighetResponse();
		response.setGetRelateradeArendenByFastighetResult(result);

		// Act
		final var dtos = mapper.mapRelateradeArendenByFastighetToByggrErrandDtos(response);

		// Assert
		assertThat(dtos).hasSize(1);
		assertThat(dtos.getFirst().getByggrCaseNumber()).isEqualTo("OVK 2026-222222");
		assertThat(dtos.getFirst().getDescription()).isEqualTo("Funktionskontroll");
	}

	@Test
	void testMapRelateradeArendenByFastighetToByggrErrandDtos_emptyResponse() {
		// Act
		final var result = mapper.mapRelateradeArendenByFastighetToByggrErrandDtos(new GetRelateradeArendenByFastighetResponse());

		// Assert
		assertThat(result).isEmpty();
	}

}
