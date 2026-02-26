package se.sundsvall.byggrintegrator.configuration;

import jakarta.validation.ValidationException;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import se.sundsvall.byggrintegrator.api.OpeneXmlResource;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.dept44.problem.ThrowableProblem;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_XML;

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
		public ResponseEntity<Problem> handleProblem(final Problem problem) {
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
				.status(BAD_REQUEST)
				.contentType(APPLICATION_PROBLEM_XML)
				.body(createProblem(BAD_REQUEST, exception));
		}

		@ExceptionHandler(Exception.class)
		@ResponseBody
		public ResponseEntity<Problem> handleException(Exception exception) {
			LOGGER.info(LOG_MESSAGE, exception);

			return ResponseEntity
				.status(INTERNAL_SERVER_ERROR)
				.contentType(APPLICATION_PROBLEM_XML)
				.body(createProblem(INTERNAL_SERVER_ERROR, exception));
		}

		private static ThrowableProblem createProblem(HttpStatus status, Exception exception) {
			return Problem.builder()
				.withStatus(status)
				.withTitle(status.getReasonPhrase())
				.withDetail(extractMessage(exception))
				.build();
		}

		private static HttpStatus exctractStatusCode(final Problem problem) {
			return Optional.ofNullable(problem.getStatus()).orElse(INTERNAL_SERVER_ERROR);
		}

		private static String extractMessage(final Exception e) {
			return Optional.ofNullable(e.getMessage()).orElse(String.valueOf(e));
		}
	}
}
