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
import se.sundsvall.byggrintegrator.api.model.ErrandDecisions;
import se.sundsvall.byggrintegrator.api.model.KeyValue;
import se.sundsvall.byggrintegrator.service.ByggrIntegratorService;
import se.sundsvall.dept44.problem.Problem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;

@ActiveProfiles("junit")
@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
class ApplicantResourceTest {

	private static final String VALID_IDENTIFIER = "190101011234";
	private static final String VALID_MUNICIPALITY_ID = "2281";
	private static final String VALID_CASE_NUMBER = "BYGG 2024-000666";
	private static final String FIND_ERRAND_URL = "/{municipalityId}/applicants/{identifier}/errands";
	private static final String FIND_ERRAND_DECISIONS_URL = "/{municipalityId}/applicants/{identifier}/errands/decisions";

	@MockitoBean
	private ByggrIntegratorService mockByggrIntegratorService;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void testFindApplicantErrands() {
		when(mockByggrIntegratorService.findApplicantErrands(anyString())).thenReturn(List.of(new KeyValue("key", "value")));

		final var responseBody = webTestClient.get()
			.uri(FIND_ERRAND_URL, VALID_MUNICIPALITY_ID, VALID_IDENTIFIER)
			.exchange()
			.expectStatus().isOk()
			.expectBodyList(KeyValue.class)
			.hasSize(1)
			.returnResult()
			.getResponseBody();

		assertThat(responseBody).isNotNull();
		assertThat(responseBody.getFirst().key()).isEqualTo("key");
		assertThat(responseBody.getFirst().value()).isEqualTo("value");

		verify(mockByggrIntegratorService).findApplicantErrands(VALID_IDENTIFIER);
		verifyNoMoreInteractions(mockByggrIntegratorService);
	}

	@Test
	void testFindApplicantErrands_serviceThrowsException() {
		when(mockByggrIntegratorService.findApplicantErrands(VALID_IDENTIFIER)).thenThrow(new RuntimeException("Service failed"));

		final var responseBody = webTestClient.get()
			.uri(FIND_ERRAND_URL, VALID_MUNICIPALITY_ID, VALID_IDENTIFIER)
			.exchange()
			.expectStatus().is5xxServerError()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(Problem.class)
			.returnResult()
			.getResponseBody();

		assertThat(responseBody).isNotNull();
		assertThat(responseBody.getStatus()).isEqualTo(INTERNAL_SERVER_ERROR);
		assertThat(responseBody.getDetail()).contains("Service failed");
		verify(mockByggrIntegratorService).findApplicantErrands(VALID_IDENTIFIER);
		verifyNoMoreInteractions(mockByggrIntegratorService);
	}

	@Test
	void testFindApplicantErrand_serviceThrowsProblem() {
		when(mockByggrIntegratorService.findApplicantErrands(anyString())).thenThrow(Problem.builder()
			.withTitle("502 Title")
			.withStatus(BAD_GATEWAY)
			.withDetail("502 Detail")
			.build());

		final var responseBody = webTestClient.get()
			.uri(FIND_ERRAND_URL, VALID_MUNICIPALITY_ID, VALID_IDENTIFIER)
			.exchange()
			.expectStatus().is5xxServerError()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(Problem.class)
			.returnResult()
			.getResponseBody();

		assertThat(responseBody).isNotNull();
		assertThat(responseBody.getStatus()).isEqualTo(BAD_GATEWAY);
		assertThat(responseBody.getTitle()).isEqualTo("502 Title");
		assertThat(responseBody.getDetail()).isEqualTo("502 Detail");

		verify(mockByggrIntegratorService).findApplicantErrands(VALID_IDENTIFIER);
		verifyNoMoreInteractions(mockByggrIntegratorService);
	}

	@Test
	void testFindApplicantErrandDecisions() {
		final var errandDecisions = new ErrandDecisions(VALID_CASE_NUMBER, "description", "SUNDSVALL 2:55", List.of());
		when(mockByggrIntegratorService.getDecisions(VALID_MUNICIPALITY_ID, VALID_IDENTIFIER, VALID_CASE_NUMBER)).thenReturn(errandDecisions);

		final var responseBody = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(FIND_ERRAND_DECISIONS_URL)
				.queryParams(MultiValueMap.fromSingleValue(Map.of("caseNumber", VALID_CASE_NUMBER)))
				.build(VALID_MUNICIPALITY_ID, VALID_IDENTIFIER))
			.exchange()
			.expectStatus().isOk()
			.expectBody(ErrandDecisions.class)
			.returnResult()
			.getResponseBody();

		assertThat(responseBody).isEqualTo(errandDecisions);

		verify(mockByggrIntegratorService).getDecisions(VALID_MUNICIPALITY_ID, VALID_IDENTIFIER, VALID_CASE_NUMBER);
		verifyNoMoreInteractions(mockByggrIntegratorService);
	}

	@Test
	void testFindApplicantErrandDecisions_serviceThrowsProblem() {
		when(mockByggrIntegratorService.getDecisions(VALID_MUNICIPALITY_ID, VALID_IDENTIFIER, VALID_CASE_NUMBER)).thenThrow(Problem.builder()
			.withTitle("404 Title")
			.withStatus(NOT_FOUND)
			.withDetail("404 Detail")
			.build());

		final var responseBody = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(FIND_ERRAND_DECISIONS_URL)
				.queryParams(MultiValueMap.fromSingleValue(Map.of("caseNumber", VALID_CASE_NUMBER)))
				.build(VALID_MUNICIPALITY_ID, VALID_IDENTIFIER))
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

		verify(mockByggrIntegratorService).getDecisions(VALID_MUNICIPALITY_ID, VALID_IDENTIFIER, VALID_CASE_NUMBER);
		verifyNoMoreInteractions(mockByggrIntegratorService);
	}

	@Test
	void testFindApplicantErrandDecisions_serviceThrowsException() {
		when(mockByggrIntegratorService.getDecisions(VALID_MUNICIPALITY_ID, VALID_IDENTIFIER, VALID_CASE_NUMBER)).thenThrow(new RuntimeException("Service failed"));

		final var responseBody = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(FIND_ERRAND_DECISIONS_URL)
				.queryParams(MultiValueMap.fromSingleValue(Map.of("caseNumber", VALID_CASE_NUMBER)))
				.build(VALID_MUNICIPALITY_ID, VALID_IDENTIFIER))
			.exchange()
			.expectStatus().is5xxServerError()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(Problem.class)
			.returnResult()
			.getResponseBody();

		assertThat(responseBody).isNotNull();
		assertThat(responseBody.getStatus()).isEqualTo(INTERNAL_SERVER_ERROR);
		assertThat(responseBody.getDetail()).contains("Service failed");

		verify(mockByggrIntegratorService).getDecisions(VALID_MUNICIPALITY_ID, VALID_IDENTIFIER, VALID_CASE_NUMBER);
		verifyNoMoreInteractions(mockByggrIntegratorService);
	}
}
