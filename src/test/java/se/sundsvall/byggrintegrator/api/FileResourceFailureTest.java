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
import se.sundsvall.dept44.problem.violations.ConstraintViolationProblem;
import se.sundsvall.dept44.problem.violations.Violation;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;

@ActiveProfiles("junit")
@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
class FileResourceFailureTest {

	private static final String VALID_MUNICIPALITY_ID = "2281";
	private static final String INVALID_MUNICIPALITY_ID = "invalid municipality";
	private static final String FILE_URL = "/{municipalityId}/files/{fileId}?token={token}";

	@MockitoBean
	private ByggrIntegratorService mockByggrIntegratorService;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void testReadFile_faultyMunicipalityId_shouldThrowException() {
		final var fileId = randomUUID().toString();
		final var token = randomUUID().toString();

		final var responseBody = webTestClient.get()
			.uri(FILE_URL, INVALID_MUNICIPALITY_ID, fileId, token)
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
			.containsExactlyInAnyOrder(tuple("readFile.municipalityId", "not a valid municipality ID"));

		verifyNoInteractions(mockByggrIntegratorService);
	}

	@Test
	void testReadFile_missingToken() {
		final var fileId = randomUUID().toString();

		final var responseBody = webTestClient.get().uri("/{municipalityId}/files/{fileId}", VALID_MUNICIPALITY_ID, fileId)
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(Problem.class)
			.returnResult()
			.getResponseBody();

		assertThat(responseBody).isNotNull();
		assertThat(responseBody.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(responseBody.getDetail()).contains("token");
		verifyNoInteractions(mockByggrIntegratorService);
	}
}
