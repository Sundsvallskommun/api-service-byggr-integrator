package se.sundsvall.byggrintegrator.api;

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
import se.sundsvall.byggrintegrator.service.ByggrIntegratorService;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.dept44.problem.violations.ConstraintViolationProblem;
import se.sundsvall.dept44.problem.violations.Violation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;

@ActiveProfiles("junit")
@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
class ApplicantResourceFailureTest {

	private static final String VALID_IDENTIFIER = "190101011234";
	private static final String VALID_MUNICIPALITY_ID = "2281";
	private static final String VALID_CASE_NUMBER = "BYGG 2024-000666";
	private static final String INVALID_MUNICIPALITY_ID = "invalid municipality";
	private static final String INVALID_IDENTIFIER = "invalid identifier";
	private static final String BLANK_CASE_NUMBER = "   ";
	private static final String FIND_ERRAND_URL = "/{municipalityId}/applicants/{identifier}/errands";
	private static final String FIND_ERRAND_DECISIONS_URL = "/{municipalityId}/applicants/{identifier}/errands/decisions";

	@MockitoBean
	private ByggrIntegratorService mockByggrIntegratorService;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void testFindApplicantErrandDecisions_faultyMunicipalityId_shouldThrowException() {
		final var responseBody = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(FIND_ERRAND_DECISIONS_URL)
				.queryParams(MultiValueMap.fromSingleValue(Map.of("caseNumber", VALID_CASE_NUMBER)))
				.build(INVALID_MUNICIPALITY_ID, VALID_IDENTIFIER))
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(responseBody).isNotNull();
		assertThat(responseBody.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(responseBody.getTitle()).isEqualTo("Constraint Violation");
		assertThat(responseBody.getViolations()).extracting(Violation::field, Violation::message)
			.containsExactlyInAnyOrder(tuple("findApplicantErrandDecisions.municipalityId", "not a valid municipality ID"));

		verifyNoInteractions(mockByggrIntegratorService);
	}

	@Test
	void testFindApplicantErrandDecisions_faultyIdentifier_shouldThrowException() {
		final var responseBody = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(FIND_ERRAND_DECISIONS_URL)
				.queryParams(MultiValueMap.fromSingleValue(Map.of("caseNumber", VALID_CASE_NUMBER)))
				.build(VALID_MUNICIPALITY_ID, INVALID_IDENTIFIER))
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(responseBody).isNotNull();
		assertThat(responseBody.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(responseBody.getTitle()).isEqualTo("Constraint Violation");
		assertThat(responseBody.getViolations()).extracting(Violation::field, Violation::message)
			.containsExactlyInAnyOrder(tuple("findApplicantErrandDecisions.identifier", "Invalid personal or organization number"));

		verifyNoInteractions(mockByggrIntegratorService);
	}

	@Test
	void testFindApplicantErrandDecisions_blankCaseNumber_shouldThrowException() {
		final var responseBody = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(FIND_ERRAND_DECISIONS_URL)
				.queryParams(MultiValueMap.fromSingleValue(Map.of("caseNumber", BLANK_CASE_NUMBER)))
				.build(VALID_MUNICIPALITY_ID, VALID_IDENTIFIER))
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(responseBody).isNotNull();
		assertThat(responseBody.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(responseBody.getTitle()).isEqualTo("Constraint Violation");
		assertThat(responseBody.getViolations()).extracting(Violation::field, Violation::message)
			.containsExactlyInAnyOrder(tuple("findApplicantErrandDecisions.caseNumber", "must not be blank"));

		verifyNoInteractions(mockByggrIntegratorService);
	}

	@Test
	void testFindApplicantErrandDecisions_missingCaseNumber_shouldThrowException() {
		final var responseBody = webTestClient.get()
			.uri(FIND_ERRAND_DECISIONS_URL, VALID_MUNICIPALITY_ID, VALID_IDENTIFIER)
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(Problem.class)
			.returnResult()
			.getResponseBody();

		assertThat(responseBody).isNotNull();
		assertThat(responseBody.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(responseBody.getTitle()).isEqualTo("Bad Request");
		assertThat(responseBody.getDetail()).contains("caseNumber", "not present");

		verifyNoInteractions(mockByggrIntegratorService);
	}

	@Test
	void testFindApplicantErrandDecisions_allParametersInvalid_shouldThrowException() {
		final var responseBody = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(FIND_ERRAND_DECISIONS_URL)
				.queryParams(MultiValueMap.fromSingleValue(Map.of("caseNumber", BLANK_CASE_NUMBER)))
				.build(INVALID_MUNICIPALITY_ID, INVALID_IDENTIFIER))
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(responseBody).isNotNull();
		assertThat(responseBody.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(responseBody.getTitle()).isEqualTo("Constraint Violation");
		assertThat(responseBody.getViolations()).extracting(Violation::field, Violation::message)
			.containsExactlyInAnyOrder(
				tuple("findApplicantErrandDecisions.municipalityId", "not a valid municipality ID"),
				tuple("findApplicantErrandDecisions.identifier", "Invalid personal or organization number"),
				tuple("findApplicantErrandDecisions.caseNumber", "must not be blank"));

		verifyNoInteractions(mockByggrIntegratorService);
	}

	@Test
	void testFindApplicantErrands_faultyMunicipalityId_shouldThrowException() {
		final var responseBody = webTestClient.get()
			.uri(FIND_ERRAND_URL, INVALID_MUNICIPALITY_ID, VALID_IDENTIFIER)
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(responseBody).isNotNull();
		assertThat(responseBody.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(responseBody.getTitle()).isEqualTo("Constraint Violation");
		assertThat(responseBody.getViolations()).extracting(Violation::field, Violation::message)
			.containsExactlyInAnyOrder(tuple("findApplicantErrands.municipalityId", "not a valid municipality ID"));

		verifyNoInteractions(mockByggrIntegratorService);
	}

	@Test
	void testFindApplicantErrands_faultyIdentifier_shouldThrowException() {
		final var responseBody = webTestClient.get()
			.uri(FIND_ERRAND_URL, VALID_MUNICIPALITY_ID, INVALID_IDENTIFIER)
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(responseBody).isNotNull();
		assertThat(responseBody.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(responseBody.getTitle()).isEqualTo("Constraint Violation");
		assertThat(responseBody.getViolations()).extracting(Violation::field, Violation::message)
			.containsExactlyInAnyOrder(tuple("findApplicantErrands.identifier", "Invalid personal or organization number"));

		verifyNoInteractions(mockByggrIntegratorService);
	}

	@Test
	void testFindApplicantErrands_faultyMunicipalityIdAndIdentifier_shouldThrowException() {
		final var responseBody = webTestClient.get()
			.uri(FIND_ERRAND_URL, INVALID_MUNICIPALITY_ID, INVALID_IDENTIFIER)
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(responseBody).isNotNull();
		assertThat(responseBody.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(responseBody.getTitle()).isEqualTo("Constraint Violation");
		assertThat(responseBody.getViolations()).extracting(Violation::field, Violation::message)
			.containsExactlyInAnyOrder(
				tuple("findApplicantErrands.identifier", "Invalid personal or organization number"),
				tuple("findApplicantErrands.municipalityId", "not a valid municipality ID"));

		verifyNoInteractions(mockByggrIntegratorService);
	}
}
