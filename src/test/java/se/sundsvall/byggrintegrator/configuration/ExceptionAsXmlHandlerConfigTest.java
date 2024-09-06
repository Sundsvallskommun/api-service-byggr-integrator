package se.sundsvall.byggrintegrator.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.I_AM_A_TEAPOT;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_XML;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

import jakarta.validation.ValidationException;

@SpringBootTest(classes = ExceptionAsXmlHandlerConfig.class)
class ExceptionAsXmlHandlerConfigTest {

	@Autowired
	private ExceptionAsXmlHandlerConfig.ControllerExceptionAsXmlHandler handler;

	@Test
	void testHandleProblem() {
		final var detail = "detail";
		final var problem = Problem.builder()
			.withStatus(Status.I_AM_A_TEAPOT)
			.withDetail(detail)
			.build();

		final var result = handler.handleProblem(problem);

		assertThat(result).isNotNull();
		assertThat(result.getStatusCode()).isEqualTo(I_AM_A_TEAPOT);
		assertThat(result.getHeaders().getContentType()).isEqualTo(APPLICATION_PROBLEM_XML);
		assertThat(result.getBody()).isNotNull().isEqualTo(problem);
	}

	@Test
	void testHandleValidationException() {
		final var exception = new ValidationException("test");

		final var result = handler.handleValidationException(exception);

		assertThat(result).isNotNull();
		assertThat(result.getStatusCode()).isEqualTo(BAD_REQUEST);
		assertThat(result.getHeaders().getContentType()).isEqualTo(APPLICATION_PROBLEM_XML);
		assertThat(result.getBody()).isNotNull().satisfies(problem -> {
			assertThat(problem.getStatus()).isEqualTo(Status.BAD_REQUEST);
			assertThat(problem.getTitle()).isEqualTo(Status.BAD_REQUEST.getReasonPhrase());
			assertThat(problem.getDetail()).isEqualTo(exception.getMessage());
		});
	}

	@Test
	void testHandleException() {
		final var exception = new NullPointerException("test");

		final var result = handler.handleException(exception);

		assertThat(result).isNotNull();
		assertThat(result.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
		assertThat(result.getHeaders().getContentType()).isEqualTo(APPLICATION_PROBLEM_XML);
		assertThat(result.getBody()).isNotNull().satisfies(problem -> {
			assertThat(problem.getStatus()).isEqualTo(Status.INTERNAL_SERVER_ERROR);
			assertThat(problem.getTitle()).isEqualTo(Status.INTERNAL_SERVER_ERROR.getReasonPhrase());
			assertThat(problem.getDetail()).isEqualTo(exception.getMessage());
		});

	}
}
