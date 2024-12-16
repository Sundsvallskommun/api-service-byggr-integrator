package se.sundsvall.byggrintegrator.integration.byggr;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

import generated.se.sundsvall.arendeexport.Arende;
import generated.se.sundsvall.arendeexport.ArendeFastighet;
import generated.se.sundsvall.arendeexport.ArendeIntressent;
import generated.se.sundsvall.arendeexport.ArrayOfAbstractArendeObjekt2;
import generated.se.sundsvall.arendeexport.ArrayOfArendeIntressent2;
import generated.se.sundsvall.arendeexport.ArrayOfHandelse;
import generated.se.sundsvall.arendeexport.ArrayOfHandelseHandling;
import generated.se.sundsvall.arendeexport.ArrayOfHandelseIntressent2;
import generated.se.sundsvall.arendeexport.ArrayOfString2;
import generated.se.sundsvall.arendeexport.GetArende;
import generated.se.sundsvall.arendeexport.GetArendeResponse;
import generated.se.sundsvall.arendeexport.GetDocument;
import generated.se.sundsvall.arendeexport.GetHandlingTyper;
import generated.se.sundsvall.arendeexport.GetRelateradeArendenByPersOrgNrAndRole;
import generated.se.sundsvall.arendeexport.GetRelateradeArendenByPersOrgNrAndRoleResponse;
import generated.se.sundsvall.arendeexport.GetRoller;
import generated.se.sundsvall.arendeexport.Handelse;
import generated.se.sundsvall.arendeexport.HandelseIntressent;
import generated.se.sundsvall.arendeexport.ObjectFactory;
import generated.se.sundsvall.arendeexport.RollTyp;
import generated.se.sundsvall.arendeexport.StatusFilter;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.xml.datatype.XMLGregorianCalendar;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import se.sundsvall.byggrintegrator.model.ByggrErrandDto;
import se.sundsvall.byggrintegrator.model.ByggrErrandDto.Event;
import se.sundsvall.byggrintegrator.model.ByggrErrandDto.Stakeholder;
import se.sundsvall.byggrintegrator.service.util.ByggrFilterUtility;

/**
 * Mapper for handling mappings between ByggR responses and the internal dto class used in the service layer
 */
@Component
public class ByggrIntegrationMapper {
	private static final ObjectFactory OBJECT_FACTORY = new ObjectFactory();
	private final ByggrFilterUtility filterUtility;

	public ByggrIntegrationMapper(final ByggrFilterUtility filterUtility) {
		this.filterUtility = filterUtility;
	}

	public GetRoller createGetRolesRequest() {
		return OBJECT_FACTORY.createGetRoller()
			.withRollTyp(RollTyp.INTRESSENT)
			.withStatusfilter(StatusFilter.AKTIV);
	}

	public GetRelateradeArendenByPersOrgNrAndRole mapToGetRelateradeArendenRequest(final String id) {
		return OBJECT_FACTORY.createGetRelateradeArendenByPersOrgNrAndRole()
			.withStatusfilter(StatusFilter.AKTIV)
			.withPersOrgNr(id);
	}

	public GetArende mapToGetArendeRequest(final String dnr) {
		return OBJECT_FACTORY.createGetArende()
			.withDnr(dnr);
	}

	public GetDocument mapToGetDocumentRequest(final String documentId) {
		return new GetDocument()
			.withDocumentId(documentId)
			.withInkluderaFil(true);
	}

	public GetHandlingTyper createGetHandlingTyperRequest() {
		return new GetHandlingTyper()
			.withStatusfilter(StatusFilter.NONE);
	}

	public List<ByggrErrandDto> mapToByggrErrandDtos(final List<GetRelateradeArendenByPersOrgNrAndRoleResponse> responses) {
		return ofNullable(responses).orElse(emptyList()).stream()
			.map(this::extractErrands)
			.flatMap(Collection::stream)
			.map(this::toByggrErrandDto)
			.toList();
	}

	public ByggrErrandDto mapToByggrErrandDto(final GetArendeResponse response) {
		return ofNullable(response)
			.map(GetArendeResponse::getGetArendeResult)
			.map(this::toByggrErrandDto)
			.orElse(null);
	}

	private List<Arende> extractErrands(final GetRelateradeArendenByPersOrgNrAndRoleResponse response) {
		return ofNullable(response.getGetRelateradeArendenByPersOrgNrAndRoleResult())
			.flatMap(result -> ofNullable(result.getArende())).stream()
			.flatMap(Collection::stream)
			.toList();
	}

	private ByggrErrandDto toByggrErrandDto(final Arende arende) {
		return ByggrErrandDto.builder()
			.withByggrCaseNumber(arende.getDnr())
			.withDescription(arende.getBeskrivning())
			.withPropertyDesignation(toPropertyDesignation(arende))
			.withEvents(toEvents(arende.getHandelseLista()))
			.withStakeholders(toStakeholders(arende.getIntressentLista()))
			.build();
	}

	private String toPropertyDesignation(final Arende arende) {
		return ofNullable(arende)
			.map(Arende::getObjektLista)
			.map(ArrayOfAbstractArendeObjekt2::getAbstractArendeObjekt)
			.filter(CollectionUtils::isNotEmpty)
			.map(List::getFirst)
			.filter(ArendeFastighet.class::isInstance)
			.map(ArendeFastighet.class::cast)
			.map(ArendeFastighet::getFastighet)
			.map(fastighet -> fastighet.getTrakt() + " " + fastighet.getFbetNr())
			.orElse(null);
	}

	private List<Event> toEvents(final ArrayOfHandelse handelser) {
		return ofNullable(handelser)
			.map(wrapper -> ofNullable(wrapper.getHandelse()).orElse(emptyList()))
			.stream()
			.flatMap(Collection::stream)
			.map(this::toEvent)
			.toList();
	}

	private Event toEvent(final Handelse handelse) {
		return Event.builder()
			.withId(handelse.getHandelseId())
			.withEventType(handelse.getHandelsetyp())
			.withEventSubtype(handelse.getHandelseslag())
			.withEventDate(ofNullable(handelse.getStartDatum())
				.map(XMLGregorianCalendar::toGregorianCalendar)
				.map(GregorianCalendar::toZonedDateTime)
				.map(ZonedDateTime::toLocalDate)
				.orElse(null))
			.withFiles(toFiles(handelse.getHandlingLista()))
			.withStakeholders(toStakeholders(handelse.getIntressentLista()))
			.withHeading(handelse.getRubrik())
			.build();
	}

	private Map<String, Event.DocumentNameAndType> toFiles(final ArrayOfHandelseHandling handlingar) {
		return ofNullable(handlingar)
			.map(wrapper -> ofNullable(wrapper.getHandling()).orElse(emptyList()))
			.stream()
			.flatMap(Collection::stream)
			.filter(filterUtility::hasValidDocumentType) // Filter out unwanted document types
			.map(handelseHandling -> new Triple(handelseHandling.getDokument().getDokId(), handelseHandling.getDokument().getNamn(), handelseHandling.getTyp()))
			.filter(triple -> Objects.nonNull(triple.dokumentId()))
			.filter(triple -> Objects.nonNull(triple.dokumentNamn()))
			.map(triple -> Map.entry(triple.dokumentId, new Event.DocumentNameAndType(triple.dokumentNamn, triple.handlingTyp)))
			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a1, a2) -> a1)); // In case of duplicate dokIds within the event, just pick the dokumentId one of them
	}

	private record Triple(String dokumentId, String dokumentNamn, String handlingTyp) {}

	private List<Stakeholder> toStakeholders(final ArrayOfHandelseIntressent2 intressenter) {
		return ofNullable(intressenter)
			.map(wrapper -> ofNullable(wrapper.getIntressent()).orElse(emptyList()))
			.stream()
			.flatMap(Collection::stream)
			.map(this::toStakeholder)
			.toList();
	}

	private Stakeholder toStakeholder(final HandelseIntressent intressent) {
		return Stakeholder.builder()
			.withLegalId(intressent.getPersOrgNr())
			.withRoles(toRoles(intressent.getRollLista()))
			.build();
	}

	private List<Stakeholder> toStakeholders(final ArrayOfArendeIntressent2 intressenter) {
		return ofNullable(intressenter)
			.map(wrapper -> ofNullable(wrapper.getIntressent()).orElse(emptyList()))
			.stream()
			.flatMap(Collection::stream)
			.map(this::toStakeholder)
			.toList();
	}

	private Stakeholder toStakeholder(final ArendeIntressent intressent) {
		return Stakeholder.builder()
			.withLegalId(intressent.getPersOrgNr())
			.withRoles(toRoles(intressent.getRollLista()))
			.build();
	}

	private List<String> toRoles(final ArrayOfString2 roller) {
		return ofNullable(roller)
			.map(ArrayOfString2::getRoll)
			.orElse(emptyList());
	}
}
