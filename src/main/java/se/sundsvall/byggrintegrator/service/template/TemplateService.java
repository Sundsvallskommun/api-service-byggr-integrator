package se.sundsvall.byggrintegrator.service.template;

import generated.se.sundsvall.arendeexport.v4.HandelseHandling;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;
import se.sundsvall.byggrintegrator.model.ByggrErrandDto;

import static java.util.Optional.ofNullable;

@Service
public class TemplateService {

	// Name of the file Thymeleaf should use as a template
	private static final String TEMPLATE_FILE = "neighborhood-notification-file-list";
	private static final String DESCRIPTION_PROPERTY_DESIGNATION_TEMPLATE_FILE = "description-property-designation";
	private static final Locale LOCALE = Locale.of("sv", "SE");

	private final ITemplateEngine templateEngine;
	private final TemplateMapper mapper;

	public TemplateService(final ITemplateEngine templateEngine, TemplateMapper mapper) {
		this.templateEngine = templateEngine;
		this.mapper = mapper;
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
			.map(errandDto -> mapper.mapByggrFilesToList(municipalityId, handlingtyper, handling))
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
}
