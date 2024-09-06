package se.sundsvall.byggrintegrator.configuration;

import java.util.Locale;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.StatusType;
import org.zalando.problem.ThrowableProblem;

import se.sundsvall.byggrintegrator.api.OpeneHtmlResource;
import se.sundsvall.dept44.requestid.RequestId;

import jakarta.validation.ValidationException;

/**
 * Configuration to convert exceptions and problems to HTML responses.
 */
@Configuration
public class ExceptionAsHtmlHandlerConfig {

	private static final Logger LOG = LoggerFactory.getLogger(ExceptionAsHtmlHandlerConfig.class);

	@ControllerAdvice(assignableTypes = OpeneHtmlResource.class)
	public static class ControllerExceptionAsHtmlHandler {

		private static final String TEMPLATE_FILE = "neighborhood-notification-file-exception";
		private static final String TEMPLATE_ERROR_MESSAGE = "errorMessage";
		private static final String TEMPLATE_REQUEST_ID = "requestId";

		private static final String ERROR_MESSAGE = "Something went wrong while fetching file locations: %s.";
		private static final String VALIDATION_ERROR_MESSAGE = "Validation error: %s.";

		private static final Context context = new Context(Locale.of("sv", "SE"));
		private final ITemplateEngine templateEngine;

		public ControllerExceptionAsHtmlHandler(ITemplateEngine templateEngine) {
			this.templateEngine = templateEngine;
		}

		@ExceptionHandler(ThrowableProblem.class)
		@ResponseBody
		public ResponseEntity<String> handleProblem(Problem problem) {
			LOG.info("Mapping problem to HTML string {}.", problem);

			context.setVariable(TEMPLATE_ERROR_MESSAGE, createBody(ERROR_MESSAGE, problem.getTitle()));

			return ResponseEntity
				.status(Optional.ofNullable(problem.getStatus())
					.map(StatusType::getStatusCode)
					.orElse(HttpStatus.INTERNAL_SERVER_ERROR.value()))
				.contentType(MediaType.TEXT_HTML)
				.body(processTemplate());
		}

		@ExceptionHandler(Exception.class)
		@ResponseStatus
		public ResponseEntity<String> handleException(Exception exception) {
			LOG.error("Mapping exception to HTML string.", exception);

			context.setVariable(TEMPLATE_ERROR_MESSAGE, createBody(ERROR_MESSAGE, exception.getMessage()));

			return ResponseEntity
				.status(Status.INTERNAL_SERVER_ERROR.getStatusCode())
				.contentType(MediaType.TEXT_HTML)
				.body(processTemplate());
		}

		@ExceptionHandler(ValidationException.class)
		@ResponseBody
		public ResponseEntity<String> handleValidationException(ValidationException exception) {
			LOG.info("Mapping validation exception to HTML string.", exception);

			context.setVariable(TEMPLATE_ERROR_MESSAGE, createBody(VALIDATION_ERROR_MESSAGE, exception.getMessage()));

			return ResponseEntity
				.status(Status.BAD_REQUEST.getStatusCode())
				.contentType(MediaType.TEXT_HTML)
				.body(processTemplate());
		}

		private String processTemplate() {
			context.setVariable(TEMPLATE_REQUEST_ID, RequestId.get());
			return templateEngine.process(TEMPLATE_FILE, context);
		}

		private static String createBody(String errorMessage, String exceptionMessage) {
			return String.format(errorMessage, exceptionMessage, RequestId.get());
		}
	}
}
