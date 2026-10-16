package se.sundsvall.byggrintegrator.api.validation;

import java.io.IOException;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ValidPdfConstraintValidatorTest {

	private static final byte[] PDF_CONTENT = "%PDF-1.7\n%%EOF".getBytes();
	private final ValidPdfConstraintValidator validator = new ValidPdfConstraintValidator();

	@ParameterizedTest
	@ValueSource(strings = {
		"protokoll.pdf", "PROTOKOLL.PDF", "ovk.pdf"
	})
	void isValidPdf(final String filename) {
		final var file = new MockMultipartFile("file", filename, "application/pdf", PDF_CONTENT);

		assertThat(validator.isValid(file, null)).isTrue();
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("invalidFiles")
	void isInvalidPdf(final String scenario, final MultipartFile file) {
		assertThat(validator.isValid(file, null)).isFalse();
	}

	private static Stream<Arguments> invalidFiles() {
		return Stream.of(
			Arguments.of("null file", null),
			Arguments.of("empty file", new MockMultipartFile("file", "protokoll.pdf", "application/pdf", new byte[0])),
			Arguments.of("wrong extension", new MockMultipartFile("file", "protokoll.txt", "text/plain", PDF_CONTENT)),
			Arguments.of("no extension", new MockMultipartFile("file", "protokoll", "application/pdf", PDF_CONTENT)),
			Arguments.of("no filename", new MockMultipartFile("file", null, "application/pdf", PDF_CONTENT)),
			Arguments.of("not pdf content", new MockMultipartFile("file", "protokoll.pdf", "application/pdf", "test".getBytes())),
			Arguments.of("shorter than signature", new MockMultipartFile("file", "protokoll.pdf", "application/pdf", "%PD".getBytes())));
	}

	@Test
	void isInvalidPdf_unreadableFile() throws IOException {
		final var file = mock(MultipartFile.class);
		when(file.getOriginalFilename()).thenReturn("protokoll.pdf");
		when(file.getInputStream()).thenThrow(new IOException("reading error"));

		assertThat(validator.isValid(file, null)).isFalse();
	}
}
