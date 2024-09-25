package se.sundsvall.byggrintegrator.service.template;

import static java.util.Optional.ofNullable;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Component;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

import se.sundsvall.byggrintegrator.model.ByggrErrandDto;
import se.sundsvall.byggrintegrator.model.FileTemplateDto;
import se.sundsvall.byggrintegrator.service.ByggrFilterUtility;

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
	 *
	 * @param byggrErrandDto The errand to generate the file list for
	 * @return The HTML as a string
	 */
	public String generateFileList(String municipalityId, ByggrErrandDto byggrErrandDto) {
		final var fileTemplateDtoList = ofNullable(byggrErrandDto)
			.map(errandDto -> mapByggrFilesToList(municipalityId, errandDto))
			.orElse(List.of());

		final var context = new Context(Locale.of("sv", "SE"));
		// Add the list of files to the context (as "fileList") so Thymeleaf can use it
		context.setVariable("fileList", fileTemplateDtoList);

		return templateEngine.process(TEMPLATE_FILE, context);
	}

	// Create a list that we iterate over with Thymeleaf
	private List<FileTemplateDto> mapByggrFilesToList(String municipalityId, ByggrErrandDto byggrErrandDto) {
		return ofNullable(byggrErrandDto.getEvents()).orElse(Collections.emptyList()).stream()
			.filter(ByggrFilterUtility::hasValidEvent)
			.map(event -> ofNullable(event.getFiles()).orElse(Collections.emptyMap()))
			.map(file -> file.entrySet().stream()
				.map(entry -> mapToUrl(municipalityId, entry.getKey(), entry.getValue()))
				.toList())
			.flatMap(List::stream)
			.sorted((o1, o2) -> o2.getFileUrl().compareTo(o1.getFileUrl()))
			.toList();
	}

	// Create a FileTemplateDto containing the URL to the file and the file name as the display name
	private FileTemplateDto mapToUrl(String municipalityId, String fileId, String fileName) {
		return FileTemplateDto.builder()
			.withFileUrl(String.format("%s%s%s%s", // [domain] [municipalityid] [subdirectory] [file identificator]
				templateProperties.domain(),
				municipalityId,
				templateProperties.subDirectory(),
				fileId))
			.withFileName(fileName)
			.build();
	}
}
