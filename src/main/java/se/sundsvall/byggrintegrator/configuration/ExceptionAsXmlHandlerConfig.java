package se.sundsvall.byggrintegrator.configuration;

import static org.springframework.http.MediaType.APPLICATION_PROBLEM_XML;
import static org.zalando.problem.Status.BAD_REQUEST;
import static org.zalando.problem.Status.INTERNAL_SERVER_ERROR;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;

import se.sundsvall.byggrintegrator.api.OpeneXmlResource;

import jakarta.validation.ValidationException;

/**
 * Configuration needed to convert execption responses to correct response content types
 */
@Configuration
public class ExceptionAsXmlHandlerConfig {
	private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionAsXmlHandlerConfig.class);
	private static final String LOG_MESSAGE = "Mapping exception into Problem";

	/**
	 * ControllerAdvice for the OpenE XML resource
	 */
	@ControllerAdvice(assignableTypes = OpeneXmlResource.class)
	public static class ControllerExceptionAsXmlHandler {

		@ExceptionHandler(ThrowableProblem.class)
		@ResponseBody
		public ResponseEntity<Problem> handleProblem(Problem problem) {
			return ResponseEntity
				.status(exctractStatusCode(problem))
				.contentType(APPLICATION_PROBLEM_XML)
				.body(problem);
		}

		@ExceptionHandler(ValidationException.class)
		@ResponseBody
		public ResponseEntity<Problem> handleValidationException(ValidationException exception) {
			LOGGER.info(LOG_MESSAGE, exception);

			return ResponseEntity
				.status(Status.BAD_REQUEST.getStatusCode())
				.contentType(APPLICATION_PROBLEM_XML)
				.body(createProblem(BAD_REQUEST, exception));
		}

		@ExceptionHandler(Exception.class)
		@ResponseBody
		public ResponseEntity<Problem> handleException(Exception exception) {
			LOGGER.info(LOG_MESSAGE, exception);

			return ResponseEntity
				.status(Status.INTERNAL_SERVER_ERROR.getStatusCode())
				.contentType(APPLICATION_PROBLEM_XML)
				.body(createProblem(INTERNAL_SERVER_ERROR, exception));
		}

		private static ThrowableProblem createProblem(Status status, Exception exception) {
			return Problem.builder()
				.withStatus(status)
				.withTitle(status.getReasonPhrase())
				.withDetail(extractMessage(exception))
				.build();
		}

		private static int exctractStatusCode(Problem problem) {
			return Optional.ofNullable(problem.getStatus()).orElse(INTERNAL_SERVER_ERROR).getStatusCode();
		}

		private static String extractMessage(Exception e) {
			return Optional.ofNullable(e.getMessage()).orElse(String.valueOf(e));
		}
	}
}
