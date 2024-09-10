package se.sundsvall.byggrintegrator.configuration;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.I_AM_A_TEAPOT;
import static org.springframework.http.MediaType.TEXT_HTML;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

import jakarta.validation.ValidationException;
import se.sundsvall.byggrintegrator.Application;
import se.sundsvall.dept44.requestid.RequestId;

@ActiveProfiles("junit")
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class ExceptionAsHtmlHandlerConfigTest {
	private static final String UUID = randomUUID().toString();

	@Autowired
	private ExceptionAsHtmlHandlerConfig.ControllerExceptionAsHtmlHandler handler;

	@BeforeAll
	static void setupRequestId() {
		RequestId.init(UUID);
	}

	@Test
	void testHandleProblem() {
		// Arrange
		final var detail = "Detail";
		final var problem = Problem.builder()
			.withStatus(Status.I_AM_A_TEAPOT)
			.withTitle("Some title")
			.withDetail(detail)
			.build();

		// Act
		final var result = handler.handleProblem(problem);

		// Assert
		assertThat(result).isNotNull();
		assertThat(result.getStatusCode()).isEqualTo(I_AM_A_TEAPOT);
		assertThat(result.getHeaders().getContentType()).isEqualTo(TEXT_HTML);
		assertThat(result.getBody())
			.isNotNull()
			.isEqualTo("<ul><li><span >Something went wrong while fetching file locations: Some title.</span><br><span>Please refer to this requestId in any conversation:&nbsp</span><span>%s</span></li></ul>".formatted(UUID));
	}

	@Test
	void testHandleValidationException() {
		final var exception = new ValidationException("test");

		final var result = handler.handleValidationException(exception);

		assertThat(result).isNotNull();
		assertThat(result.getStatusCode()).isEqualTo(BAD_REQUEST);
		assertThat(result.getHeaders().getContentType()).isEqualTo(TEXT_HTML);
		assertThat(result.getBody())
			.isNotNull()
			.isEqualTo("<ul><li><span >Validation error: test.</span><br><span>Please refer to this requestId in any conversation:&nbsp</span><span>%s</span></li></ul>".formatted(UUID));
	}

	@Test
	void testHandleException() {
		final var exception = new NullPointerException("NPE test");

		final var result = handler.handleException(exception);

		assertThat(result).isNotNull();
		assertThat(result.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
		assertThat(result.getHeaders().getContentType()).isEqualTo(TEXT_HTML);
		assertThat(result.getBody())
			.isNotNull()
			.isEqualTo("<ul><li><span >Something went wrong while fetching file locations: NPE test.</span><br><span>Please refer to this requestId in any conversation:&nbsp</span><span>%s</span></li></ul>".formatted(UUID));
	}
}
