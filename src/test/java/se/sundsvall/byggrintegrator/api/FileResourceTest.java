package se.sundsvall.byggrintegrator.api;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;
import static org.zalando.problem.Status.BAD_REQUEST;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.zalando.problem.violations.ConstraintViolationProblem;
import org.zalando.problem.violations.Violation;

import jakarta.servlet.http.HttpServletResponse;
import se.sundsvall.byggrintegrator.Application;
import se.sundsvall.byggrintegrator.service.ByggrIntegratorService;

@ActiveProfiles("junit")
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FileResourceTest {

	@MockitoBean
	private ByggrIntegratorService mockByggrIntegratorService;

	@Autowired
	private WebTestClient webTestClient;

	private static final String VALID_MUNICIPALITY_ID = "2281";
	private static final String INVALID_MUNICIPALITY_ID = "invalid municipality";
	private static final String APPLICANT_URL = "/{municipalityId}/files/{fileId}";

	@Test
	void testReadFile() {
		final var fileId = randomUUID().toString();

		webTestClient.get()
			.uri(APPLICANT_URL, VALID_MUNICIPALITY_ID, fileId)
			.exchange()
			.expectStatus().isOk()
			.expectBody()
			.isEmpty();

		verify(mockByggrIntegratorService).readFile(eq(fileId), any(HttpServletResponse.class));
		verifyNoMoreInteractions(mockByggrIntegratorService);
	}

	@Test
	void testFindApplicantErrands_faultyMunicipalityId_shouldThrowException() {
		final var responseBody = webTestClient.get()
			.uri(APPLICANT_URL, INVALID_MUNICIPALITY_ID, randomUUID().toString())
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
			.containsExactlyInAnyOrder(tuple("readFile.municipalityId", "not a valid municipality ID"));

		verifyNoInteractions(mockByggrIntegratorService);
	}
}
