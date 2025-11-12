package se.sundsvall.byggrintegrator.service.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.overviewproject.mime_types.MimeTypeDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.MimeTypeUtils;

public final class MimeTypeUtility {

	private static final Logger LOGGER = LoggerFactory.getLogger(MimeTypeUtility.class);
	private static final MimeTypeDetector DETECTOR = new MimeTypeDetector();

	private MimeTypeUtility() {}

	public static String detectMimeType(String fileName, byte[] byteArray) {
		try (InputStream stream = new ByteArrayInputStream(byteArray)) {
			return DETECTOR.detectMimeType(fileName, stream);
		} catch (Exception e) {
			LOGGER.error("Exception when detecting mime type of file with filename '{}'", fileName, e);
			return MimeTypeUtils.APPLICATION_OCTET_STREAM_VALUE; // Return mime type for arbitrary binary files
		}
	}
}
