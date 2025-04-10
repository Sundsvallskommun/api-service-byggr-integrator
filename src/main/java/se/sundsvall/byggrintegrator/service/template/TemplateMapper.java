package se.sundsvall.byggrintegrator.service.template;

import static java.util.Optional.ofNullable;

import java.util.Collections;
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
import se.sundsvall.byggrintegrator.service.util.ByggrFilterUtility;
import se.sundsvall.byggrintegrator.service.util.LegalIdUtility;

@Component
public class TemplateMapper {

	// Name of the file Thymeleaf should use as template
	private static final String TEMPLATE_FILE = "neighborhood-notification-file-list";
	private static final String DESCRIPTION_PROPERTY_DESIGNATION_TEMPLATE_FILE = "description-property-designation";
	private static final Locale LOCALE = Locale.of("sv", "SE");

	private final TemplateProperties templateProperties;
	private final ITemplateEngine templateEngine;

	public TemplateMapper(TemplateProperties templateProperties, ITemplateEngine templateEngine) {
		this.templateProperties = templateProperties;
		this.templateEngine = templateEngine;
	}

	/**
	 * Generate a list of files as HTML
	 *
	 * @param  byggrErrandDto The errand to generate the file list for
	 * @return                The HTML as a string
	 */
	public String generateFileList(final String municipalityId, final ByggrErrandDto byggrErrandDto, final Map<String, String> handlingtyper, String identifier) {
		final var fileTemplateDtoList = ofNullable(byggrErrandDto)
			.map(errandDto -> mapByggrFilesToList(municipalityId, errandDto, handlingtyper, identifier))
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
	 * Create a supplementary header for the file list
	 * The supplementary header is the description of the errand concatenated with the property designation
	 *
	 * @param  byggrErrandDto The errand
	 * @return
	 */
	private String createSupplementaryHeader(final ByggrErrandDto byggrErrandDto) {
		return ofNullable(byggrErrandDto)
			.map(header -> byggrErrandDto.getDescription() + " (" + byggrErrandDto.getPropertyDesignation() + ")")
			.orElse("");
	}

	// Create a list that we iterate over with Thymeleaf
	private List<FileTemplateDto> mapByggrFilesToList(final String municipalityId, final ByggrErrandDto byggrErrandDto, final Map<String, String> handlingtyper, String identifier) {
		return ofNullable(byggrErrandDto.getEvents()).orElse(Collections.emptyList()).stream()
			.filter(ByggrFilterUtility::hasValidEvent)
			.filter(event -> ofNullable(event.getStakeholders()) // Only act on events that has a stakeholder with legal id matching sent in identifier
				.orElse(Collections.emptyList())
				.stream()
				.anyMatch(stakeholder -> LegalIdUtility.isEqual(stakeholder.getLegalId(), identifier)))
			.map(event -> ofNullable(event.getFiles()).orElse(Collections.emptyMap()))
			.map(Map::entrySet)
			.flatMap(Set::stream)
			.map(entry -> mapToUrl(municipalityId, entry.getKey(), entry.getValue(), handlingtyper))
			.filter(distinctByKey(FileTemplateDto::getFileName)) // Remove duplicate file names if such exists
			.sorted((o1, o2) -> o2.getFileUrl().compareTo(o1.getFileUrl()))
			.toList();
	}

	private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
		final Set<Object> seen = ConcurrentHashMap.newKeySet();
		return t -> seen.add(keyExtractor.apply(t));
	}

	// Create a FileTemplateDto containing the URL to the file and the file name as the display name
	private FileTemplateDto mapToUrl(final String municipalityId, final String fileId, final ByggrErrandDto.Event.DocumentNameAndType documentNameAndType, final Map<String, String> handlingtyper) {
		return FileTemplateDto.builder()
			.withFileUrl(String.format("%s%s%s%s", // [domain] [municipalityid] [subdirectory] [file identificator]
				templateProperties.domain(),
				municipalityId,
				templateProperties.subDirectory(),
				fileId))
			.withFileName(parseFilename(documentNameAndType, handlingtyper))
			.build();
	}

	private String parseFilename(final ByggrErrandDto.Event.DocumentNameAndType documentNameAndType, final Map<String, String> handlingtyper) {
		return "%s (%s)".formatted(
			handlingtyper.get(documentNameAndType.getDocumentType()),
			ofNullable(documentNameAndType.getDocumentName())
				.orElse(null));
	}
}
