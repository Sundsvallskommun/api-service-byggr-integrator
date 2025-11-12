package se.sundsvall.byggrintegrator.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;
import static org.zalando.problem.Status.BAD_REQUEST;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.MultiValueMap;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;
import org.zalando.problem.violations.ConstraintViolationProblem;
import org.zalando.problem.violations.Violation;
import se.sundsvall.byggrintegrator.Application;
import se.sundsvall.byggrintegrator.api.model.KeyValue;
import se.sundsvall.byggrintegrator.service.ByggrIntegratorService;

@ActiveProfiles("junit")
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class NeighborhoodNotificationResourceTest {

	private static final String VALID_IDENTIFIER = "190101011234";
	private static final String VALID_MUNICIPALITY_ID = "2281";
	private static final String VALID_CASE_NUMBER = "123456789";
	private static final String INVALID_IDENTIFIER = "invalid identifier";
	private static final String INVALID_MUNICIPALITY_ID = "invalid municipality";
	private static final String INVALID_CASE_NUMBER = "   ";
	private static final String NEIGHBORHOOD_NOTIFICATION_URL = "/{municipalityId}/neighborhood-notifications/{identifier}/errands";
	private static final String NEIGHBORHOOD_NOTIFICATION_FACILITIES_REQUEST_PARAMETERS_URL = "/{municipalityId}/neighborhood-notifications/properties";

	@MockitoBean
	private ByggrIntegratorService mockByggrIntegratorService;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void testFindNeighborhoodNotifications() {
		when(mockByggrIntegratorService.findNeighborhoodNotifications(anyString())).thenReturn(List.of(new KeyValue("key", "value")));

		final var responseBody = webTestClient.get()
			.uri(NEIGHBORHOOD_NOTIFICATION_URL, VALID_MUNICIPALITY_ID, VALID_IDENTIFIER)
			.exchange()
			.expectStatus().isOk()
			.expectBodyList(KeyValue.class)
			.hasSize(1)
			.returnResult()
			.getResponseBody();

		assertThat(responseBody).isNotNull();
		assertThat(responseBody.getFirst().key()).isEqualTo("key");
		assertThat(responseBody.getFirst().value()).isEqualTo("value");

		verify(mockByggrIntegratorService).findNeighborhoodNotifications(VALID_IDENTIFIER);
		verifyNoMoreInteractions(mockByggrIntegratorService);
	}

	@Test
	void testFindNeighborhoodNotifications_faultyMunicipalityId_shouldThrowException() {
		final var responseBody = webTestClient.get()
			.uri(NEIGHBORHOOD_NOTIFICATION_URL, INVALID_MUNICIPALITY_ID, VALID_IDENTIFIER)
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(responseBody).isNotNull();
		assertThat(responseBody.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(responseBody.getTitle()).isEqualTo("Constraint Violation");
		assertThat(responseBody.getViolations()).extracting(Violation::getField, Violation::getMessage)
			.containsExactlyInAnyOrder(tuple("findNeighborhoodNotifications.municipalityId", "not a valid municipality ID"));

		verifyNoInteractions(mockByggrIntegratorService);
	}

	@Test
	void testFindNeighborhoodNotifications_faultyIdentifier_shouldThrowException() {
		final var responseBody = webTestClient.get()
			.uri(NEIGHBORHOOD_NOTIFICATION_URL, VALID_MUNICIPALITY_ID, INVALID_IDENTIFIER)
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(responseBody).isNotNull();
		assertThat(responseBody.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(responseBody.getTitle()).isEqualTo("Constraint Violation");
		assertThat(responseBody.getViolations()).extracting(Violation::getField, Violation::getMessage)
			.containsExactlyInAnyOrder(tuple("findNeighborhoodNotifications.identifier", "Invalid personal or organization number"));

		verifyNoInteractions(mockByggrIntegratorService);
	}

	@Test
	void testFindNeighborhoodNotifications_faultyMunicipalityIdAndIdentifier_shouldThrowException() {
		final var responseBody = webTestClient.get()
			.uri(NEIGHBORHOOD_NOTIFICATION_URL, INVALID_MUNICIPALITY_ID, INVALID_IDENTIFIER)
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(responseBody).isNotNull();
		assertThat(responseBody.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(responseBody.getTitle()).isEqualTo("Constraint Violation");
		assertThat(responseBody.getViolations()).extracting(Violation::getField, Violation::getMessage)
			.containsExactlyInAnyOrder(
				tuple("findNeighborhoodNotifications.identifier", "Invalid personal or organization number"),
				tuple("findNeighborhoodNotifications.municipalityId", "not a valid municipality ID"));

		verifyNoInteractions(mockByggrIntegratorService);
	}

	@Test
	void testFindNeighborhoodNotifications_serviceThrows404() {
		when(mockByggrIntegratorService.findNeighborhoodNotifications(anyString())).thenThrow(Problem.builder()
			.withTitle("404 Title")
			.withStatus(Status.NOT_FOUND)
			.withDetail("404 Detail")
			.build());

		final var responseBody = webTestClient.get()
			.uri(NEIGHBORHOOD_NOTIFICATION_URL, VALID_MUNICIPALITY_ID, VALID_IDENTIFIER)
			.exchange()
			.expectStatus().isNotFound()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ThrowableProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(responseBody).isNotNull();
		assertThat(responseBody.getStatus()).isEqualTo(Status.NOT_FOUND);
		assertThat(responseBody.getMessage()).isEqualTo("404 Title: 404 Detail");
		assertThat(responseBody.getTitle()).isEqualTo("404 Title");
		assertThat(responseBody.getDetail()).isEqualTo("404 Detail");

		verify(mockByggrIntegratorService).findNeighborhoodNotifications(VALID_IDENTIFIER);
		verifyNoMoreInteractions(mockByggrIntegratorService);
	}

	@Test
	void findNeighborhoodNotificationFacilitiesWithRequestParameters() {
		when(mockByggrIntegratorService.getNeighborhoodNotificationFacilities(VALID_IDENTIFIER, VALID_CASE_NUMBER)).thenReturn(List.of(new KeyValue("key", "value")));

		final var responseBody = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(NEIGHBORHOOD_NOTIFICATION_FACILITIES_REQUEST_PARAMETERS_URL)
				.queryParams(MultiValueMap.fromSingleValue(Map.of("identifier", VALID_IDENTIFIER, "caseNumber", VALID_CASE_NUMBER)))
				.build(VALID_MUNICIPALITY_ID))
			.exchange()
			.expectStatus().isOk()
			.expectBodyList(KeyValue.class)
			.hasSize(1)
			.returnResult()
			.getResponseBody();

		assertThat(responseBody).isNotNull();
		assertThat(responseBody.getFirst().key()).isEqualTo("key");
		assertThat(responseBody.getFirst().value()).isEqualTo("value");

		verify(mockByggrIntegratorService).getNeighborhoodNotificationFacilities(VALID_IDENTIFIER, VALID_CASE_NUMBER);
		verifyNoMoreInteractions(mockByggrIntegratorService);
	}

	@Test
	void findNeighborhoodNotificationFacilitiesWithRequestParameters_faultyMunicipalityId_shouldThrowException() {
		final var responseBody = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(NEIGHBORHOOD_NOTIFICATION_FACILITIES_REQUEST_PARAMETERS_URL)
				.queryParams(MultiValueMap.fromSingleValue(Map.of("identifier", VALID_IDENTIFIER, "caseNumber", VALID_CASE_NUMBER)))
				.build(INVALID_MUNICIPALITY_ID))
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(responseBody).isNotNull();
		assertThat(responseBody.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(responseBody.getTitle()).isEqualTo("Constraint Violation");
		assertThat(responseBody.getViolations()).extracting(Violation::getField, Violation::getMessage)
			.containsExactlyInAnyOrder(tuple("findNeighborhoodNotificationFacilitiesWithRequestParameters.municipalityId", "not a valid municipality ID"));

		verifyNoInteractions(mockByggrIntegratorService);
	}

	@Test
	void findNeighborhoodNotificationFacilitiesWithRequestParameters_faultyIdentifier_shouldThrowException() {
		final var responseBody = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(NEIGHBORHOOD_NOTIFICATION_FACILITIES_REQUEST_PARAMETERS_URL)
				.queryParams(MultiValueMap.fromSingleValue(Map.of("identifier", INVALID_IDENTIFIER, "caseNumber", VALID_CASE_NUMBER)))
				.build(VALID_MUNICIPALITY_ID))
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(responseBody).isNotNull();
		assertThat(responseBody.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(responseBody.getTitle()).isEqualTo("Constraint Violation");
		assertThat(responseBody.getViolations()).extracting(Violation::getField, Violation::getMessage)
			.containsExactlyInAnyOrder(tuple("findNeighborhoodNotificationFacilitiesWithRequestParameters.identifier", "Invalid personal or organization number"));

		verifyNoInteractions(mockByggrIntegratorService);
	}

	@Test
	void findNeighborhoodNotificationFacilitiesWithRequestParameters_faultyCaseNumber_shouldThrowException() {
		final var responseBody = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(NEIGHBORHOOD_NOTIFICATION_FACILITIES_REQUEST_PARAMETERS_URL)
				.queryParams(MultiValueMap.fromSingleValue(Map.of("identifier", VALID_IDENTIFIER, "caseNumber", INVALID_CASE_NUMBER)))
				.build(VALID_MUNICIPALITY_ID))
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(responseBody).isNotNull();
		assertThat(responseBody.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(responseBody.getTitle()).isEqualTo("Constraint Violation");
		assertThat(responseBody.getViolations()).extracting(Violation::getField, Violation::getMessage)
			.containsExactlyInAnyOrder(tuple("findNeighborhoodNotificationFacilitiesWithRequestParameters.caseNumber", "must not be blank"));

		verifyNoInteractions(mockByggrIntegratorService);
	}

	@Test
	void findNeighborhoodNotificationFacilitiesWithRequestParameters_serviceThrows404() {
		when(mockByggrIntegratorService.getNeighborhoodNotificationFacilities(VALID_IDENTIFIER, VALID_CASE_NUMBER)).thenThrow(Problem.builder()
			.withTitle("404 Title")
			.withStatus(Status.NOT_FOUND)
			.withDetail("404 Detail")
			.build());

		final var responseBody = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(NEIGHBORHOOD_NOTIFICATION_FACILITIES_REQUEST_PARAMETERS_URL)
				.queryParams(MultiValueMap.fromSingleValue(Map.of("identifier", VALID_IDENTIFIER, "caseNumber", VALID_CASE_NUMBER)))
				.build(VALID_MUNICIPALITY_ID))
			.exchange()
			.expectStatus().isNotFound()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ThrowableProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(responseBody).isNotNull();
		assertThat(responseBody.getStatus()).isEqualTo(Status.NOT_FOUND);
		assertThat(responseBody.getMessage()).isEqualTo("404 Title: 404 Detail");
		assertThat(responseBody.getTitle()).isEqualTo("404 Title");
		assertThat(responseBody.getDetail()).isEqualTo("404 Detail");

		verify(mockByggrIntegratorService).getNeighborhoodNotificationFacilities(VALID_IDENTIFIER, VALID_CASE_NUMBER);
		verifyNoMoreInteractions(mockByggrIntegratorService);
	}
}
