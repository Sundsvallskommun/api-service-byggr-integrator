package se.sundsvall.byggrintegrator.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.TEXT_HTML_VALUE;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

import se.sundsvall.byggrintegrator.Application;
import se.sundsvall.byggrintegrator.service.ByggrIntegratorService;

@ActiveProfiles("junit")
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OpeneHtmlResourceTest {

	@MockBean
	private ByggrIntegratorService mockByggrIntegratorService;

	@Autowired
	private WebTestClient webTestClient;

	private static final String INFO_QUERY_RESPONSE_HEADER = "InfoQueryResponse";
	private static final String INFO_QUERY_RESPONSE_HEADER_VALUE = "true";
	private static final String ERRAND_NUMBER = "BYGG 2001-123456";
	private static final String MUNICIPALITY_ID = "2281";
	private static final String FAULTY_MUNICIPALITY_ID = "notValid";
	private static final String NEIGHBORHOOD_NOTIFICATION_FILES_PATH = "/{municipalityId}/opene/cases/{caseNumber}/neighborhood-notifications";

	@Test
	void testFindNeighborhoodNotificationFiles() {
		when(mockByggrIntegratorService.listNeighborhoodNotificationFiles("2281", "BYGG 2001-123456")).thenReturn("<html></html>");

		final var response = webTestClient.get()
			.uri(NEIGHBORHOOD_NOTIFICATION_FILES_PATH, MUNICIPALITY_ID, ERRAND_NUMBER)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().valueEquals(INFO_QUERY_RESPONSE_HEADER, INFO_QUERY_RESPONSE_HEADER_VALUE)
			.expectBody(String.class)
			.returnResult()
			.getResponseBody();

		assertThat(response)
			.isNotNull()
			.isNotEmpty()
			.isEqualTo("<html></html>");

		verify(mockByggrIntegratorService).listNeighborhoodNotificationFiles(MUNICIPALITY_ID, ERRAND_NUMBER);
		verifyNoMoreInteractions(mockByggrIntegratorService);
	}

	@Test
	void testFindNeighborhoodNotificationFiles_faultyMunicipalityId() {
		final var result = webTestClient.get()
			.uri(NEIGHBORHOOD_NOTIFICATION_FILES_PATH, FAULTY_MUNICIPALITY_ID, ERRAND_NUMBER)
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(TEXT_HTML_VALUE)
			.expectHeader().valueEquals(INFO_QUERY_RESPONSE_HEADER, INFO_QUERY_RESPONSE_HEADER_VALUE)
			.expectBody(String.class)
			.returnResult();

		final var responseBody = result.getResponseBody();
		final var requestId = result.getResponseHeaders().get("x-request-id").getFirst();

		assertThat(responseBody)
			.isNotNull()
			.isNotEmpty()
			.contains("<ul><li><span >Validation error: findNeighborhoodNotificationFiles.municipalityId: not a valid municipality ID.</span><br><span>Please refer to this requestId in any conversation:&nbsp</span><span>" + requestId
				+ "</span></li></ul>");

		verifyNoInteractions(mockByggrIntegratorService);
	}

	@Test
	void testFindNeighborhoodNotificationFiles_whenServiceThrowsException() {
		when(mockByggrIntegratorService.listNeighborhoodNotificationFiles("2281", "BYGG 2001-123456")).thenThrow(new RuntimeException("Service failed"));

		final var result = webTestClient.get()
			.uri(NEIGHBORHOOD_NOTIFICATION_FILES_PATH, MUNICIPALITY_ID, ERRAND_NUMBER)
			.exchange()
			.expectStatus().is5xxServerError()
			.expectHeader().contentType(TEXT_HTML_VALUE)
			.expectHeader().valueEquals(INFO_QUERY_RESPONSE_HEADER, INFO_QUERY_RESPONSE_HEADER_VALUE)
			.expectBody(String.class)
			.returnResult();

		final var responseBody = result.getResponseBody();
		final var requestId = result.getResponseHeaders().get("x-request-id").getFirst();

		assertThat(responseBody)
			.isNotNull()
			.isNotEmpty()
			.contains("ul><li><span >Something went wrong while fetching file locations: Service failed.</span><br><span>Please refer to this requestId in any conversation:&nbsp</span><span>" + requestId + "</span></li></ul>");

		verify(mockByggrIntegratorService).listNeighborhoodNotificationFiles(MUNICIPALITY_ID, ERRAND_NUMBER);
		verifyNoMoreInteractions(mockByggrIntegratorService);
	}

	@Test
	void testFindNeighborhoodNotificationFiles_whenServiceThrowsProblem() {
		when(mockByggrIntegratorService.listNeighborhoodNotificationFiles("2281", "BYGG 2001-123456")).thenThrow(Problem.builder()
			.withTitle("404 Title")
			.withStatus(Status.NOT_FOUND)
			.withDetail("404 Detail")
			.build());

		final var result = webTestClient.get()
			.uri(NEIGHBORHOOD_NOTIFICATION_FILES_PATH, MUNICIPALITY_ID, ERRAND_NUMBER)
			.exchange()
			.expectStatus().is4xxClientError()
			.expectHeader().contentType(TEXT_HTML_VALUE)
			.expectHeader().valueEquals(INFO_QUERY_RESPONSE_HEADER, INFO_QUERY_RESPONSE_HEADER_VALUE)
			.expectBody(String.class)
			.returnResult();

		final var responseBody = result.getResponseBody();
		final var requestId = result.getResponseHeaders().get("x-request-id").getFirst();

		assertThat(responseBody)
			.isNotNull()
			.isNotEmpty()
			.contains("<ul><li><span >Something went wrong while fetching file locations: 404 Title.</span><br><span>Please refer to this requestId in any conversation:&nbsp</span><span>" + requestId + "</span></li></ul>");

		verify(mockByggrIntegratorService).listNeighborhoodNotificationFiles(MUNICIPALITY_ID, ERRAND_NUMBER);
		verifyNoMoreInteractions(mockByggrIntegratorService);
	}
}
