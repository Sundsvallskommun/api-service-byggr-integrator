package se.sundsvall.byggrintegrator.api;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.MultiValueMap;
import se.sundsvall.byggrintegrator.Application;
import se.sundsvall.byggrintegrator.api.model.KeyValue;
import se.sundsvall.byggrintegrator.service.ByggrIntegratorService;
import se.sundsvall.dept44.problem.Problem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;

@ActiveProfiles("junit")
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class NeighborhoodNotificationResourceTest {

	private static final String VALID_IDENTIFIER = "190101011234";
	private static final String VALID_MUNICIPALITY_ID = "2281";
	private static final String VALID_CASE_NUMBER = "123456789";
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
	void testFindNeighborhoodNotifications_serviceThrows404() {
		when(mockByggrIntegratorService.findNeighborhoodNotifications(anyString())).thenThrow(Problem.builder()
			.withTitle("404 Title")
			.withStatus(NOT_FOUND)
			.withDetail("404 Detail")
			.build());

		final var responseBody = webTestClient.get()
			.uri(NEIGHBORHOOD_NOTIFICATION_URL, VALID_MUNICIPALITY_ID, VALID_IDENTIFIER)
			.exchange()
			.expectStatus().isNotFound()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(Problem.class)
			.returnResult()
			.getResponseBody();

		assertThat(responseBody).isNotNull();
		assertThat(responseBody.getStatus()).isEqualTo(NOT_FOUND);
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
	void findNeighborhoodNotificationFacilitiesWithRequestParameters_serviceThrows404() {
		when(mockByggrIntegratorService.getNeighborhoodNotificationFacilities(VALID_IDENTIFIER, VALID_CASE_NUMBER)).thenThrow(Problem.builder()
			.withTitle("404 Title")
			.withStatus(NOT_FOUND)
			.withDetail("404 Detail")
			.build());

		final var responseBody = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(NEIGHBORHOOD_NOTIFICATION_FACILITIES_REQUEST_PARAMETERS_URL)
				.queryParams(MultiValueMap.fromSingleValue(Map.of("identifier", VALID_IDENTIFIER, "caseNumber", VALID_CASE_NUMBER)))
				.build(VALID_MUNICIPALITY_ID))
			.exchange()
			.expectStatus().isNotFound()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(Problem.class)
			.returnResult()
			.getResponseBody();

		assertThat(responseBody).isNotNull();
		assertThat(responseBody.getStatus()).isEqualTo(NOT_FOUND);
		assertThat(responseBody.getTitle()).isEqualTo("404 Title");
		assertThat(responseBody.getDetail()).isEqualTo("404 Detail");

		verify(mockByggrIntegratorService).getNeighborhoodNotificationFacilities(VALID_IDENTIFIER, VALID_CASE_NUMBER);
		verifyNoMoreInteractions(mockByggrIntegratorService);
	}

	@Test
	void testFindNeighborhoodNotificationFacilitiesWithRequestParameters_serviceThrows500() {
		when(mockByggrIntegratorService.getNeighborhoodNotificationFacilities(VALID_IDENTIFIER, VALID_CASE_NUMBER)).thenThrow(Problem.builder()
			.withTitle("500 Title")
			.withStatus(INTERNAL_SERVER_ERROR)
			.withDetail("500 Detail")
			.build());

		final var responseBody = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(NEIGHBORHOOD_NOTIFICATION_FACILITIES_REQUEST_PARAMETERS_URL)
				.queryParams(MultiValueMap.fromSingleValue(Map.of("identifier", VALID_IDENTIFIER, "caseNumber", VALID_CASE_NUMBER)))
				.build(VALID_MUNICIPALITY_ID))
			.exchange()
			.expectStatus().is5xxServerError()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(Problem.class)
			.returnResult()
			.getResponseBody();

		assertThat(responseBody).isNotNull();
		assertThat(responseBody.getTitle()).isEqualTo("500 Title");
		assertThat(responseBody.getStatus()).isEqualTo(INTERNAL_SERVER_ERROR);
		assertThat(responseBody.getDetail()).isEqualTo("500 Detail");

		verify(mockByggrIntegratorService).getNeighborhoodNotificationFacilities(VALID_IDENTIFIER, VALID_CASE_NUMBER);
		verifyNoMoreInteractions(mockByggrIntegratorService);
	}
}
