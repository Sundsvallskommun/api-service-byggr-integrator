package se.sundsvall.byggrintegrator.api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.io.IOException;
import java.util.Arrays;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

public class ValidPdfConstraintValidator implements ConstraintValidator<ValidPdf, MultipartFile> {

	private static final byte[] PDF = "%PDF-".getBytes();

	@Override
	public boolean isValid(final MultipartFile file, final ConstraintValidatorContext context) {
		if (file == null || file.isEmpty()) {
			return false;
		}
		if (!("pdf".equalsIgnoreCase(StringUtils.getFilenameExtension(file.getOriginalFilename())))) {
			return false;
		}
		try (final var inputStream = file.getInputStream()) {
			return Arrays.equals(inputStream.readNBytes(PDF.length), PDF);
		} catch (final IOException e) {
			return false;
		}
	}
}
