package se.sundsvall.byggrintegrator.service.template;

import static java.util.Optional.ofNullable;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Component;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

import se.sundsvall.byggrintegrator.model.ByggrErrandDto;
import se.sundsvall.byggrintegrator.model.FileTemplateDto;

@Component
public class TemplateMapper {

	// Name of the file Thymeleaf should use as template
	private static final String TEMPLATE_FILE = "neighborhood-notification-file-list";

	private final TemplateProperties templateProperties;
	private final ITemplateEngine templateEngine;

	public TemplateMapper(TemplateProperties templateProperties, ITemplateEngine templateEngine) {
		this.templateProperties = templateProperties;
		this.templateEngine = templateEngine;
	}

	/**
	 * Generate a list of files as HTML
	 * @param byggrErrandDto The errand to generate the file list for
	 * @return The HTML as a string
	 */
	public String generateFileList(ByggrErrandDto byggrErrandDto) {
		var fileTemplateDtoList = ofNullable(byggrErrandDto)
			.map(this::mapByggrFilesToList)
			.orElse(List.of());

		var context = new Context(Locale.of("sv", "SE"));
		// Add the list of files to the context (as "fileList") so Thymeleaf can use it
		context.setVariable("fileList", fileTemplateDtoList);

		return templateEngine.process(TEMPLATE_FILE, context);
	}

	// Create a list that we iterate over with Thymeleaf
	private List<FileTemplateDto> mapByggrFilesToList(ByggrErrandDto byggrErrandDto) {
		return ofNullable(byggrErrandDto.getFiles())
			.map(file -> file.entrySet().stream()
				.map(entry -> mapToUrl(byggrErrandDto.getByggrCaseNumber(), entry.getKey(), entry.getValue()))
				.toList())
			.orElse(List.of());
	}

	// Create a FileTemplateDto containing the URL to the file and the file name as the display name
	private FileTemplateDto mapToUrl(String errandNumber, String fileId, String fileName) {
		return FileTemplateDto.builder()
			.withFileUrl(String.format("%s%s%s%s%s",
				templateProperties.domain(),
				templateProperties.path(),
				urlEncodeString(errandNumber), //Contain characters that needs to be url encoded
				templateProperties.subDirectory(),
				fileId))
			.withFileName(fileName)
			.build();
	}

	private String urlEncodeString(String value) {
		return URLEncoder.encode(value, StandardCharsets.UTF_8);
	}
}
