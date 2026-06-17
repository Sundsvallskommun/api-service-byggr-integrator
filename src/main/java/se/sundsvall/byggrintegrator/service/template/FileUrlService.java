package se.sundsvall.byggrintegrator.service.template;

import org.springframework.stereotype.Service;
import se.sundsvall.byggrintegrator.service.FileAccessTokenService;

@Service
public class FileUrlService {

	private final TemplateProperties templateProperties;
	private final FileAccessTokenService fileAccessTokenService;

	public FileUrlService(final TemplateProperties templateProperties, final FileAccessTokenService fileAccessTokenService) {
		this.templateProperties = templateProperties;
		this.fileAccessTokenService = fileAccessTokenService;
	}

	public String parseFileUrl(final String municipalityId, final int fileId) {
		// String content is divided into the following: [domain][version]/[municipalityid][subdirectory][file
		// identificator]?token=[uuid]
		final var token = fileAccessTokenService.createToken(municipalityId, String.valueOf(fileId));
		return "%s%s/%s%s%s?token=%s".formatted(
			templateProperties.domain(),
			templateProperties.version(),
			municipalityId,
			templateProperties.subDirectory(),
			fileId,
			token);
	}
}
