package se.sundsvall.byggrintegrator.service.template;

import static java.util.Optional.ofNullable;

import generated.se.sundsvall.arendeexport.v4.HandelseHandling;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import org.springframework.stereotype.Component;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;
import se.sundsvall.byggrintegrator.model.ByggrErrandDto;
import se.sundsvall.byggrintegrator.model.FileTemplateDto;

@Component
public class TemplateMapper {

	// Name of the file Thymeleaf should use as a template
	private static final String TEMPLATE_FILE = "neighborhood-notification-file-list";
	private static final String DESCRIPTION_PROPERTY_DESIGNATION_TEMPLATE_FILE = "description-property-designation";
	private static final Locale LOCALE = Locale.of("sv", "SE");

	private final TemplateProperties templateProperties;
	private final ITemplateEngine templateEngine;

	public TemplateMapper(final TemplateProperties templateProperties, final ITemplateEngine templateEngine) {
		this.templateProperties = templateProperties;
		this.templateEngine = templateEngine;
	}

	private static <T> Predicate<T> distinctByKey(final Function<? super T, ?> keyExtractor) {
		final Set<Object> seen = ConcurrentHashMap.newKeySet();
		return t -> seen.add(keyExtractor.apply(t));
	}

	/**
	 * Generate a list of files as HTML
	 *
	 * @param  byggrErrandDto The errand to generate the file list for
	 * @param  handlingtyper  The handling typer to use when generating the file list
	 * @return                The HTML as a string
	 */
	public String generateFileList(final String municipalityId, final ByggrErrandDto byggrErrandDto, final Map<String, String> handlingtyper, final List<HandelseHandling> handling) {
		final var fileTemplateDtoList = ofNullable(byggrErrandDto)
			.map(errandDto -> mapByggrFilesToList(municipalityId, handlingtyper, handling))
			.orElse(List.of());

		final var context = new Context(LOCALE);
		// Add the list of files to the context (as "fileList") so Thymeleaf can use it
		context.setVariable("fileList", fileTemplateDtoList);
		context.setVariable("supplementaryHeader", createSupplementaryHeader(byggrErrandDto));

		return templateEngine.process(TEMPLATE_FILE, context);
	}

	public String getDescriptionAndPropertyDesignation(final ByggrErrandDto byggrErrandDto) {
		final var context = new Context(LOCALE);
		context.setVariable("descriptionAndPropertyDesignation", createSupplementaryHeader(byggrErrandDto));

		return templateEngine.process(DESCRIPTION_PROPERTY_DESIGNATION_TEMPLATE_FILE, context);
	}

	/**
	 * Create a supplementary header for the file list The supplementary header is the description of the errand
	 * concatenated with the property designation
	 *
	 * @param  byggrErrandDto The errand
	 * @return                The supplementary header
	 */
	private String createSupplementaryHeader(final ByggrErrandDto byggrErrandDto) {
		return ofNullable(byggrErrandDto)
			.map(header -> byggrErrandDto.getDescription() + " (" + byggrErrandDto.getPropertyDesignation() + ")")
			.orElse("");
	}

	// Create a list that we iterate over with Thymeleaf
	private List<FileTemplateDto> mapByggrFilesToList(final String municipalityId, final Map<String, String> handlingtyper, final List<HandelseHandling> handling) {
		return handling.stream()
			.map(entry -> mapToFileTemplateDto(municipalityId, entry.getHandlingId(), entry, handlingtyper))
			.filter(distinctByKey(FileTemplateDto::getFileName)) // Remove duplicate file names if such exists
			.sorted((o1, o2) -> o2.getFileUrl().compareTo(o1.getFileUrl()))
			.toList();
	}

	private FileTemplateDto mapToFileTemplateDto(final String municipalityId, final int fileId, final HandelseHandling documentNameAndType, final Map<String, String> handlingtyper) {
		return FileTemplateDto.builder()
			.withFileUrl(parseFileUrl(municipalityId, fileId))
			.withFileName(parseFileName(documentNameAndType, handlingtyper))
			.build();
	}

	private String parseFileUrl(final String municipalityId, final int fileId) {
		// String content is divided into the following: [domain][version]/[municipalityid][subdirectory][file identificator]
		return "%s%s/%s%s%s".formatted(
			templateProperties.domain(),
			templateProperties.version(),
			municipalityId,
			templateProperties.subDirectory(),
			fileId);
	}

	private String parseFileName(final HandelseHandling documentNameAndType, final Map<String, String> handlingtyper) {
		// String content is divided into the following: [documentType] ([documentName])
		return "%s (%s)".formatted(
			handlingtyper.get(documentNameAndType.getTyp()),
			documentNameAndType.getDokument().getNamn());
	}
}
