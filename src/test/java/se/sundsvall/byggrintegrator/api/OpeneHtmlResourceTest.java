package se.sundsvall.byggrintegrator.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.sundsvall.byggrintegrator.Application;
import se.sundsvall.byggrintegrator.service.ByggrIntegratorService;
import se.sundsvall.dept44.problem.Problem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.MediaType.TEXT_HTML_VALUE;

@ActiveProfiles("junit")
@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
class OpeneHtmlResourceTest {

	private static final String INFO_QUERY_RESPONSE_HEADER = "InfoQueryResponse";
	private static final String INFO_QUERY_RESPONSE_HEADER_VALUE = "true";
	private static final String CASE_NUMBER = "BYGG 2001-123456";
	private static final String IDENTIFIER = "190102034567";
	private static final String MUNICIPALITY_ID = "2281";
	private static final String FAULTY_MUNICIPALITY_ID = "notValid";
	private static final String NEIGHBORHOOD_NOTIFICATION_FILES_WITH_REQUEST_PARAMETER_PATH = "/{municipalityId}/opene/neighborhood-notifications/filenames";
	private static final String REFERRAL_REFERENCE = "referralReference";

	@MockitoBean
	private ByggrIntegratorService mockByggrIntegratorService;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void testFindNeighborhoodNotificationFilesWithRequestParameter() {
		when(mockByggrIntegratorService.listNeighborhoodNotificationFiles(MUNICIPALITY_ID, IDENTIFIER, CASE_NUMBER, REFERRAL_REFERENCE)).thenReturn("<html></html>");

		final var response = webTestClient.get()
			.uri(builder -> builder.path(NEIGHBORHOOD_NOTIFICATION_FILES_WITH_REQUEST_PARAMETER_PATH)
				.queryParam("identifier", IDENTIFIER)
				.queryParam("caseNumber", CASE_NUMBER)
				.queryParam("referralReference", REFERRAL_REFERENCE)
				.build(MUNICIPALITY_ID))
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

		verify(mockByggrIntegratorService).listNeighborhoodNotificationFiles(MUNICIPALITY_ID, IDENTIFIER, CASE_NUMBER, REFERRAL_REFERENCE);
		verifyNoMoreInteractions(mockByggrIntegratorService);
	}

	@Test
	void testFindNeighborhoodNotificationFilesWithRequestParameter_faultyMunicipalityId() {
		final var response = webTestClient.get()
			.uri(builder -> builder.path(NEIGHBORHOOD_NOTIFICATION_FILES_WITH_REQUEST_PARAMETER_PATH)
				.queryParam("identifier", IDENTIFIER)
				.queryParam("caseNumber", CASE_NUMBER)
				.queryParam("referralReference", REFERRAL_REFERENCE)
				.build(FAULTY_MUNICIPALITY_ID))
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(TEXT_HTML_VALUE)
			.expectHeader().valueEquals(INFO_QUERY_RESPONSE_HEADER, INFO_QUERY_RESPONSE_HEADER_VALUE)
			.expectBody(String.class)
			.returnResult();

		final var responseBody = response.getResponseBody();
		final var requestId = response.getResponseHeaders().get("x-request-id").getFirst();

		assertThat(responseBody)
			.isNotNull()
			.isNotEmpty()
			.containsIgnoringWhitespaces("""
				<ul>
					<li>
						<span >Validation error: findNeighborhoodNotificationFilesWithRequestParameter.municipalityId: not a valid municipality ID.</span>
						<span>Please refer to this requestId in any conversation:</span>
						<span>%s</span>
					</li>
				</ul>""".formatted(requestId));

		verifyNoInteractions(mockByggrIntegratorService);
	}

	@Test
	void testFindNeighborhoodNotificationFilesWithRequestParameter_whenServiceThrowsException() {
		when(mockByggrIntegratorService.listNeighborhoodNotificationFiles(MUNICIPALITY_ID, IDENTIFIER, CASE_NUMBER, REFERRAL_REFERENCE)).thenThrow(new RuntimeException("Service failed"));

		final var response = webTestClient.get()
			.uri(builder -> builder.path(NEIGHBORHOOD_NOTIFICATION_FILES_WITH_REQUEST_PARAMETER_PATH)
				.queryParam("identifier", IDENTIFIER)
				.queryParam("caseNumber", CASE_NUMBER)
				.queryParam("referralReference", REFERRAL_REFERENCE)
				.build(MUNICIPALITY_ID))
			.exchange()
			.expectStatus().is5xxServerError()
			.expectHeader().contentType(TEXT_HTML_VALUE)
			.expectHeader().valueEquals(INFO_QUERY_RESPONSE_HEADER, INFO_QUERY_RESPONSE_HEADER_VALUE)
			.expectBody(String.class)
			.returnResult();

		final var responseBody = response.getResponseBody();
		final var requestId = response.getResponseHeaders().get("x-request-id").getFirst();

		assertThat(responseBody)
			.isNotNull()
			.isNotEmpty()
			.containsIgnoringWhitespaces("""
				<ul>
					<li>
						<span >Something went wrong while fetching file locations: Service failed.</span>
						<span>Please refer to this requestId in any conversation:</span>
						<span>%s</span>
					</li>
				</ul>""".formatted(requestId));

		verify(mockByggrIntegratorService).listNeighborhoodNotificationFiles(MUNICIPALITY_ID, IDENTIFIER, CASE_NUMBER, REFERRAL_REFERENCE);
		verifyNoMoreInteractions(mockByggrIntegratorService);
	}

	@Test
	void testFindNeighborhoodNotificationFilesWithRequestParameter_whenServiceThrowsProblem() {
		when(mockByggrIntegratorService.listNeighborhoodNotificationFiles(MUNICIPALITY_ID, IDENTIFIER, CASE_NUMBER, REFERRAL_REFERENCE)).thenThrow(Problem.builder()
			.withTitle("404 Title")
			.withStatus(NOT_FOUND)
			.withDetail("404 Detail")
			.build());

		final var response = webTestClient.get()
			.uri(builder -> builder.path(NEIGHBORHOOD_NOTIFICATION_FILES_WITH_REQUEST_PARAMETER_PATH)
				.queryParam("identifier", IDENTIFIER)
				.queryParam("caseNumber", CASE_NUMBER)
				.queryParam("referralReference", REFERRAL_REFERENCE)
				.build(MUNICIPALITY_ID))
			.exchange()
			.expectStatus().is4xxClientError()
			.expectHeader().contentType(TEXT_HTML_VALUE)
			.expectHeader().valueEquals(INFO_QUERY_RESPONSE_HEADER, INFO_QUERY_RESPONSE_HEADER_VALUE)
			.expectBody(String.class)
			.returnResult();

		final var responseBody = response.getResponseBody();
		final var requestId = response.getResponseHeaders().get("x-request-id").getFirst();

		assertThat(responseBody)
			.isNotNull()
			.isNotEmpty()
			.containsIgnoringWhitespaces("""
				<ul>
					<li>
						<span >Something went wrong while fetching file locations: 404 Title.</span>
						<span>Please refer to this requestId in any conversation:</span>
						<span>%s</span>
					</li>
				</ul>""".formatted(requestId));

		verify(mockByggrIntegratorService).listNeighborhoodNotificationFiles(MUNICIPALITY_ID, IDENTIFIER, CASE_NUMBER, REFERRAL_REFERENCE);
		verifyNoMoreInteractions(mockByggrIntegratorService);
	}
}
