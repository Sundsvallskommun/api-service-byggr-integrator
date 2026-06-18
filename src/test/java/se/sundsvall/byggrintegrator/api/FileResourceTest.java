package se.sundsvall.byggrintegrator.api;

import jakarta.servlet.http.HttpServletResponse;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.GONE;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;

@ActiveProfiles("junit")
@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
class FileResourceTest {

	private static final String VALID_MUNICIPALITY_ID = "2281";
	private static final String INVALID_MUNICIPALITY_ID = "invalid municipality";
	private static final String FILE_URL = "/{municipalityId}/files/{fileId}?token={token}";

	@MockitoBean
	private ByggrIntegratorService mockByggrIntegratorService;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void testReadFile() {
		final var fileId = randomUUID().toString();
		final var token = randomUUID().toString();

		webTestClient.get()
			.uri(FILE_URL, VALID_MUNICIPALITY_ID, fileId, token)
			.exchange()
			.expectStatus().isOk()
			.expectBody()
			.isEmpty();

		verify(mockByggrIntegratorService).readFile(eq(VALID_MUNICIPALITY_ID), eq(fileId), eq(token), any(HttpServletResponse.class));
		verifyNoMoreInteractions(mockByggrIntegratorService);
	}

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
	void testReadFile_missingFile() {
		final var fileId = randomUUID().toString();
		final var token = randomUUID().toString();
		doThrow(Problem.builder()
			.withTitle("404 Title")
			.withStatus(NOT_FOUND)
			.withDetail("404 Detail").build())
			.when(mockByggrIntegratorService).readFile(eq(VALID_MUNICIPALITY_ID), eq(fileId), eq(token), any(HttpServletResponse.class));

		final var responseBody = webTestClient.get()
			.uri(FILE_URL, VALID_MUNICIPALITY_ID, fileId, token)
			.exchange()
			.expectStatus().isNotFound()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(Problem.class)
			.returnResult()
			.getResponseBody();

		assertThat(responseBody).isNotNull();
		assertThat(responseBody.getTitle()).isEqualTo("404 Title");
		assertThat(responseBody.getStatus()).isEqualTo(NOT_FOUND);
		assertThat(responseBody.getDetail()).isEqualTo("404 Detail");

		verify(mockByggrIntegratorService).readFile(eq(VALID_MUNICIPALITY_ID), eq(fileId), eq(token), any(HttpServletResponse.class));
		verifyNoMoreInteractions(mockByggrIntegratorService);
	}

	@Test
	void testReadFile_expiredToken() {
		final var fileId = randomUUID().toString();
		final var token = randomUUID().toString();

		doThrow(Problem.builder()
			.withTitle("Expired token title")
			.withStatus(GONE)
			.withDetail("Access token has expired").build())
			.when(mockByggrIntegratorService).readFile(eq(VALID_MUNICIPALITY_ID), eq(fileId), eq(token), any(HttpServletResponse.class));

		final var responseBody = webTestClient.get().uri(FILE_URL, VALID_MUNICIPALITY_ID, fileId, token)
			.exchange()
			.expectStatus().isEqualTo(GONE)
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(Problem.class)
			.returnResult()
			.getResponseBody();

		assertThat(responseBody).isNotNull();
		assertThat(responseBody.getTitle()).isEqualTo("Expired token title");
		assertThat(responseBody.getStatus()).isEqualTo(GONE);
		assertThat(responseBody.getDetail()).isEqualTo("Access token has expired");
		verify(mockByggrIntegratorService).readFile(eq(VALID_MUNICIPALITY_ID), eq(fileId), eq(token), any(HttpServletResponse.class));
		verifyNoMoreInteractions(mockByggrIntegratorService);
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

	@Test
	void testReadFile_serviceThrowsException() {
		final var fileId = randomUUID().toString();
		final var token = randomUUID().toString();

		doThrow(Problem.builder()
			.withTitle("500 Title")
			.withStatus(INTERNAL_SERVER_ERROR)
			.withDetail("500 Detail").build())
			.when(mockByggrIntegratorService).readFile(eq(VALID_MUNICIPALITY_ID), eq(fileId), eq(token), any(HttpServletResponse.class));

		final var responseBody = webTestClient.get().uri(FILE_URL, VALID_MUNICIPALITY_ID, fileId, token)
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
		verify(mockByggrIntegratorService).readFile(eq(VALID_MUNICIPALITY_ID), eq(fileId), eq(token), any(HttpServletResponse.class));
		verifyNoMoreInteractions(mockByggrIntegratorService);
	}

	@Test
	void testReadFile_serviceThrowsRuntimeException() {
		final var fileId = randomUUID().toString();
		final var token = randomUUID().toString();

		doThrow(new RuntimeException("Service failed"))
			.when(mockByggrIntegratorService).readFile(eq(VALID_MUNICIPALITY_ID), eq(fileId), eq(token), any(HttpServletResponse.class));

		final var responseBody = webTestClient.get()
			.uri(FILE_URL, VALID_MUNICIPALITY_ID, fileId, token)
			.exchange()
			.expectStatus().is5xxServerError()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(Problem.class)
			.returnResult()
			.getResponseBody();

		assertThat(responseBody).isNotNull();
		assertThat(responseBody.getStatus()).isEqualTo(INTERNAL_SERVER_ERROR);
		assertThat(responseBody.getDetail()).isEqualTo("Service failed");
		verify(mockByggrIntegratorService).readFile(eq(VALID_MUNICIPALITY_ID), eq(fileId), eq(token), any(HttpServletResponse.class));
		verifyNoMoreInteractions(mockByggrIntegratorService);
	}

}
