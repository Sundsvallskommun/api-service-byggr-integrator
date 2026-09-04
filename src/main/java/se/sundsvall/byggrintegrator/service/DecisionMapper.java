package se.sundsvall.byggrintegrator.service;

import generated.se.sundsvall.arendeexport.v8.Arende;
import generated.se.sundsvall.arendeexport.v8.ArrayOfHandelseHandling;
import generated.se.sundsvall.arendeexport.v8.Beslut;
import generated.se.sundsvall.arendeexport.v8.Handelse;
import generated.se.sundsvall.arendeexport.v8.HandelseHandling;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import javax.xml.datatype.XMLGregorianCalendar;
import org.springframework.stereotype.Component;
import se.sundsvall.byggrintegrator.api.model.Decision;
import se.sundsvall.byggrintegrator.api.model.DecisionDocument;
import se.sundsvall.byggrintegrator.api.model.ErrandDecisions;
import se.sundsvall.byggrintegrator.service.template.FileUrlService;
import se.sundsvall.byggrintegrator.service.util.ByggrFilterUtility;

import static java.util.Collections.emptyList;
import static java.util.Comparator.nullsLast;
import static java.util.Comparator.reverseOrder;
import static java.util.Optional.ofNullable;
import static se.sundsvall.byggrintegrator.integration.byggr.ByggrIntegrationMapper.toPropertyDesignation;

/**
 * Mapper for creating the API response containing decisions and decision documents from a ByggR errand
 */
@Component
public class DecisionMapper {

	private static final Pattern NUMERIC_PATTERN = Pattern.compile("\\d{1,9}");

	private final FileUrlService fileUrlService;
	private final ByggrFilterUtility filterUtility;

	public DecisionMapper(final FileUrlService fileUrlService, final ByggrFilterUtility filterUtility) {
		this.fileUrlService = fileUrlService;
		this.filterUtility = filterUtility;
	}

	/**
	 * Maps the errand and its decision events to the API response
	 *
	 * @param  municipalityId the municipality id, used when creating the document urls
	 * @param  errand         the errand as returned from ByggR
	 * @param  decisionEvents the events on the errand that are to be interpreted as decisions
	 * @param  handlingtyper  map with document type code as key and description as value
	 * @return                the API response. Decisions without decision documents are omitted
	 */
	public ErrandDecisions toErrandDecisions(final String municipalityId, final Arende errand, final List<Handelse> decisionEvents, final Map<String, String> handlingtyper) {
		return new ErrandDecisions(
			errand.getDnr(),
			errand.getBeskrivning(),
			toPropertyDesignation(errand),
			toDecisions(municipalityId, decisionEvents, handlingtyper));
	}

	private List<Decision> toDecisions(final String municipalityId, final List<Handelse> decisionEvents, final Map<String, String> handlingtyper) {
		return ofNullable(decisionEvents).orElse(emptyList()).stream()
			.filter(Objects::nonNull)
			.map(event -> toDecision(municipalityId, event, handlingtyper))
			.filter(decision -> !decision.documents().isEmpty())
			.sorted(Comparator.comparing(Decision::decisionDate, nullsLast(reverseOrder())))
			.toList();
	}

	private Decision toDecision(final String municipalityId, final Handelse event, final Map<String, String> handlingtyper) {
		final var decision = ofNullable(event.getBeslut());

		return new Decision(
			event.getHandelseId(),
			decision.map(Beslut::getBeslutNr).orElse(null),
			event.getRubrik(),
			toLocalDate(event.getStartDatum()),
			event.getHandelseutfall(),
			decision.map(Beslut::isArHuvudbeslut).orElse(false),
			decision.map(Beslut::getInstanstyp).orElse(null),
			decision.map(Beslut::getGiltigTillDatum).map(DecisionMapper::toLocalDate).orElse(null),
			toDocuments(municipalityId, event.getHandlingLista(), handlingtyper));
	}

	private List<DecisionDocument> toDocuments(final String municipalityId, final ArrayOfHandelseHandling documents, final Map<String, String> handlingtyper) {
		final var seenDocumentIds = new HashSet<String>();

		return ofNullable(documents)
			.map(ArrayOfHandelseHandling::getHandling)
			.orElse(emptyList())
			.stream()
			.filter(filterUtility::isDecisionDocument)
			.filter(document -> NUMERIC_PATTERN.matcher(document.getDokument().getDokId()).matches())
			.filter(document -> seenDocumentIds.add(document.getDokument().getDokId())) // Skip duplicates of the same document within the event
			.map(document -> toDocument(municipalityId, document, handlingtyper))
			.toList();
	}

	private DecisionDocument toDocument(final String municipalityId, final HandelseHandling document, final Map<String, String> handlingtyper) {
		final var dokument = document.getDokument();

		return new DecisionDocument(
			dokument.getDokId(),
			dokument.getNamn(),
			dokument.getBeskrivning(),
			document.getTyp(),
			ofNullable(handlingtyper).map(types -> types.get(document.getTyp())).orElse(null),
			toLocalDate(document.getHandlingDatum()),
			fileUrlService.parseFileUrl(municipalityId, Integer.parseInt(dokument.getDokId())));
	}

	private static LocalDate toLocalDate(final XMLGregorianCalendar calendar) {
		return ofNullable(calendar)
			.map(value -> LocalDate.of(value.getYear(), value.getMonth(), value.getDay()))
			.orElse(null);
	}
}
