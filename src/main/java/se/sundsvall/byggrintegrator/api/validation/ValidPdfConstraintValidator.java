package se.sundsvall.byggrintegrator.api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.io.IOException;
import org.apache.pdfbox.Loader;
import org.springframework.web.multipart.MultipartFile;

import static java.nio.charset.StandardCharsets.ISO_8859_1;

public class ValidPdfConstraintValidator implements ConstraintValidator<ValidPdf, MultipartFile> {

	// Acrobat accepts the %%EOF marker anywhere within the last 1024 bytes of the file
	private static final int EOF_SEARCH_WINDOW = 1024;
	private static final String EOF_MARKER = "%%EOF";

	@Override
	public boolean isValid(final MultipartFile file, final ConstraintValidatorContext context) {
		if (file == null || file.isEmpty()) {
			return false;
		}
		try {
			final var bytes = file.getBytes();
			if (!hasEofMarker(bytes)) {
				return false;
			}
			try (final var document = Loader.loadPDF(bytes)) {
				return document.getNumberOfPages() > 0 && !document.isEncrypted();
			}
		} catch (final IOException e) {
			return false;
		}
	}

	private static boolean hasEofMarker(final byte[] bytes) {
		final var from = Math.max(0, bytes.length - EOF_SEARCH_WINDOW);
		return new String(bytes, from, bytes.length - from, ISO_8859_1).contains(EOF_MARKER);
	}

}
