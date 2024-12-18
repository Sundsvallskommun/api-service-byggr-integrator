package se.sundsvall.byggrintegrator.service.template;

import static java.util.Optional.ofNullable;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;
import se.sundsvall.byggrintegrator.model.ByggrErrandDto;
import se.sundsvall.byggrintegrator.model.FileTemplateDto;
import se.sundsvall.byggrintegrator.service.util.ByggrFilterUtility;

@Component
public class TemplateMapper {

	// Name of the file Thymeleaf should use as template
	private static final String TEMPLATE_FILE = "neighborhood-notification-file-list";
	private static final String DESCRIPTION_PROPERTY_DESIGNATION_TEMPLATE_FILE = "description-property-designation";

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
	public String generateFileList(final String municipalityId, final ByggrErrandDto byggrErrandDto, final Map<String, String> handlingtyper, final int eventId) {
		var fileTemplateDtoList = ofNullable(byggrErrandDto)
			.map(errandDto -> mapByggrFilesToList(municipalityId, errandDto, handlingtyper))
			.orElse(List.of());

		var context = new Context(Locale.of("sv", "SE"));
		// Add the list of files to the context (as "fileList") so Thymeleaf can use it
		context.setVariable("fileList", fileTemplateDtoList);
		context.setVariable("heading", getHeading(byggrErrandDto, eventId));
		context.setVariable("supplementaryHeader", createSupplementaryHeader(byggrErrandDto));

		return templateEngine.process(TEMPLATE_FILE, context);
	}

	public String getDescriptionAndPropertyDesignation(final ByggrErrandDto byggrErrandDto) {
		var context = new Context(Locale.of("sv", "SE"));
		context.setVariable("descriptionAndPropertyDesignation", createSupplementaryHeader(byggrErrandDto));

		return templateEngine.process(DESCRIPTION_PROPERTY_DESIGNATION_TEMPLATE_FILE, context);
	}

	/**
	 * Get the heading for the event
	 * 
	 * @param  byggrErrandDto The errand containing the event to get the heading from
	 * @param  eventId        The id of the event to get the heading from
	 * @return                The heading of the event
	 */
	private String getHeading(final ByggrErrandDto byggrErrandDto, final int eventId) {
		return ofNullable(byggrErrandDto)
			.map(ByggrErrandDto::getEvents)
			.flatMap(events -> events.stream()
				.filter(event -> event.getId() == eventId)
				.findFirst())
			.map(ByggrErrandDto.Event::getHeading)
			.orElse("");
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
	private List<FileTemplateDto> mapByggrFilesToList(final String municipalityId, final ByggrErrandDto byggrErrandDto, final Map<String, String> handlingtyper) {
		return ofNullable(byggrErrandDto.getEvents()).orElse(Collections.emptyList()).stream()
			.filter(ByggrFilterUtility::hasValidEvent)
			.map(event -> ofNullable(event.getFiles()).orElse(Collections.emptyMap()))
			.map(file -> file.entrySet().stream()
				.map(entry -> mapToUrl(municipalityId, entry.getKey(), entry.getValue(), handlingtyper))
				.toList())
			.flatMap(List::stream)
			.sorted((o1, o2) -> o2.getFileUrl().compareTo(o1.getFileUrl()))
			.toList();
	}

	// Create a FileTemplateDto containing the URL to the file and the file name as the display name
	private FileTemplateDto mapToUrl(final String municipalityId, final String fileId, final ByggrErrandDto.Event.DocumentNameAndType documentNameAndType, final Map<String, String> handlingtyper) {
		return FileTemplateDto.builder()
			.withFileUrl(String.format("%s%s%s%s", // [domain] [municipalityid] [subdirectory] [file identificator]
				templateProperties.domain(),
				municipalityId,
				templateProperties.subDirectory(),
				fileId))
			.withFileName(String.format("%s (%s)", handlingtyper.get(documentNameAndType.getDocumentType()), documentNameAndType.getDocumentName()))
			.build();
	}
}
