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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.zalando.problem.violations.ConstraintViolationProblem;
import org.zalando.problem.violations.Violation;
import se.sundsvall.byggrintegrator.Application;
import se.sundsvall.byggrintegrator.api.model.KeyValue;
import se.sundsvall.byggrintegrator.service.ByggrIntegratorService;

@ActiveProfiles("junit")
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApplicantResourceTest {

	@MockitoBean
	private ByggrIntegratorService mockByggrIntegratorService;

	@Autowired
	private WebTestClient webTestClient;

	private static final String VALID_IDENTIFIER = "190101011234";
	private static final String VALID_MUNICIPALITY_ID = "2281";
	private static final String INVALID_MUNICIPALITY_ID = "invalid municipality";
	private static final String INVALID_IDENTIFIER = "invalid identifier";

	private static final String APPLICANT_URL = "/{municipalityId}/applicants/{identifier}/errands";

	@Test
	void testFindApplicantErrands() {
		when(mockByggrIntegratorService.findApplicantErrands(anyString())).thenReturn(List.of(new KeyValue("key", "value")));

		final var responseBody = webTestClient.get()
			.uri(APPLICANT_URL, VALID_MUNICIPALITY_ID, VALID_IDENTIFIER)
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
	void testFindApplicantErrands_faultyMunicipalityId_shouldThrowException() {
		final var responseBody = webTestClient.get()
			.uri(APPLICANT_URL, INVALID_MUNICIPALITY_ID, VALID_IDENTIFIER)
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
			.containsExactlyInAnyOrder(tuple("findApplicantErrands.municipalityId", "not a valid municipality ID"));

		verifyNoInteractions(mockByggrIntegratorService);
	}

	@Test
	void testFindApplicantErrands_faultyIdentifier_shouldThrowException() {
		final var responseBody = webTestClient.get()
			.uri(APPLICANT_URL, VALID_MUNICIPALITY_ID, INVALID_IDENTIFIER)
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
			.containsExactlyInAnyOrder(tuple("findApplicantErrands.identifier", "Invalid personal or organization number"));

		verifyNoInteractions(mockByggrIntegratorService);
	}

	@Test
	void testFindApplicantErrands_faultyMunicipalityIdAndIdentifier_shouldThrowException() {
		final var responseBody = webTestClient.get()
			.uri(APPLICANT_URL, INVALID_MUNICIPALITY_ID, INVALID_IDENTIFIER)
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
				tuple("findApplicantErrands.identifier", "Invalid personal or organization number"),
				tuple("findApplicantErrands.municipalityId", "not a valid municipality ID"));

		verifyNoInteractions(mockByggrIntegratorService);
	}
}
