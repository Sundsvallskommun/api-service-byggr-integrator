package se.sundsvall.byggrintegrator.configuration;

import static se.sundsvall.dept44.logbook.filter.ResponseFilterDefinition.binaryContentFilter;
import static se.sundsvall.dept44.logbook.filter.ResponseFilterDefinition.fileAttachmentFilter;
import static se.sundsvall.dept44.util.EncodingUtils.fixDoubleEncodedUTF8Content;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.HttpLogWriter;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.Precorrelation;
import org.zalando.logbook.core.BodyFilters;
import org.zalando.logbook.core.DefaultSink;
import org.zalando.logbook.json.JsonHttpLogFormatter;

@Configuration
public class LogbookConfig {

	private static final String LOGGER_NAME = "se.sundsvall.dept44.payload";

	private final int maxBodySizeToLog;

	public LogbookConfig(@Value("${logbook.logs.maxBodySizeToLog}") int maxBodySizeToLog) {
		this.maxBodySizeToLog = maxBodySizeToLog;
	}

	@Bean
	Logbook logbook(final ObjectMapper objectMapper) {
		return Logbook.builder()
			.sink(new DefaultSink(
				new JsonHttpLogFormatter(objectMapper),
				new NamedLoggerHttpLogWriter(LOGGER_NAME)))
			.responseFilters(List.of(
				fileAttachmentFilter(),
				binaryContentFilter()))
			.bodyFilter(BodyFilters.truncate(maxBodySizeToLog))
			.build();
	}

	static class NamedLoggerHttpLogWriter implements HttpLogWriter {

		private final Logger log;

		NamedLoggerHttpLogWriter(final String name) {
			log = LoggerFactory.getLogger(name);
		}

		@Override
		public boolean isActive() {
			return log.isTraceEnabled();
		}

		@Override
		public void write(final Precorrelation precorrelation, final String request) {
			log.trace(request);
		}

		@Override
		public void write(final Correlation correlation, final String response) {
			final var logMessage = fixDoubleEncodedUTF8Content(response);
			log.trace(logMessage);
		}
	}
}
