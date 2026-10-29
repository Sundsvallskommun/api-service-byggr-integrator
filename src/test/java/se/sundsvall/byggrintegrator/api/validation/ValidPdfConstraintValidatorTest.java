package se.sundsvall.byggrintegrator.api.validation;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.stream.Stream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.sundsvall.byggrintegrator.TestObjectFactory.readPdf;

class ValidPdfConstraintValidatorTest {

	private static final byte[] PDF_CONTENT = readPdf();
	private final ValidPdfConstraintValidator validator = new ValidPdfConstraintValidator();

	@ParameterizedTest
	@ValueSource(strings = {
		"protokoll.pdf", "PROTOKOLL.PDF", "ovk.pdf", "protokoll"
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
			Arguments.of("empty file", pdfFile(new byte[0])),
			Arguments.of("not pdf content", pdfFile("test".getBytes(US_ASCII))),
			Arguments.of("eof and header marker only", pdfFile("%PDF-1.7\n%%EOF".getBytes(US_ASCII))),
			Arguments.of("truncated pdf", pdfFile(Arrays.copyOf(PDF_CONTENT, PDF_CONTENT.length / 2))),
			Arguments.of("pdf without pages", pdfFile(createPdf(0, false))),
			Arguments.of("encrypted pdf", pdfFile(createPdf(1, true))));
	}

	@Test
	void isInvalidPdf_unreadableFile() throws IOException {
		final var file = mock(MultipartFile.class);
		when(file.getBytes()).thenThrow(new IOException("reading error"));

		assertThat(validator.isValid(file, null)).isFalse();
	}

	private static MultipartFile pdfFile(final byte[] content) {
		return new MockMultipartFile("file", "protokoll.pdf", "application/pdf", content);
	}

	private static byte[] createPdf(final int pages, final boolean encrypted) {
		try (final var document = new PDDocument(); final var out = new ByteArrayOutputStream()) {
			for (var i = 0; i < pages; i++) {
				document.addPage(new PDPage());
			}
			if (encrypted) {
				// Owner password only, loads without a password but is still encrypted
				document.protect(new StandardProtectionPolicy("owner", "", new AccessPermission()));
			}
			document.save(out);
			return out.toByteArray();
		} catch (final IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}
