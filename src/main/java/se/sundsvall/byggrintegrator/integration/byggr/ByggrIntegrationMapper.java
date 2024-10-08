package se.sundsvall.byggrintegrator.integration.byggr;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.xml.datatype.XMLGregorianCalendar;

import org.springframework.stereotype.Component;

import generated.se.sundsvall.arendeexport.Arende;
import generated.se.sundsvall.arendeexport.ArendeIntressent;
import generated.se.sundsvall.arendeexport.ArrayOfArendeIntressent2;
import generated.se.sundsvall.arendeexport.ArrayOfHandelse;
import generated.se.sundsvall.arendeexport.ArrayOfHandelseHandling;
import generated.se.sundsvall.arendeexport.ArrayOfHandelseIntressent2;
import generated.se.sundsvall.arendeexport.ArrayOfString2;
import generated.se.sundsvall.arendeexport.GetArende;
import generated.se.sundsvall.arendeexport.GetArendeResponse;
import generated.se.sundsvall.arendeexport.GetDocument;
import generated.se.sundsvall.arendeexport.GetRelateradeArendenByPersOrgNrAndRole;
import generated.se.sundsvall.arendeexport.GetRelateradeArendenByPersOrgNrAndRoleResponse;
import generated.se.sundsvall.arendeexport.GetRoller;
import generated.se.sundsvall.arendeexport.Handelse;
import generated.se.sundsvall.arendeexport.HandelseHandling;
import generated.se.sundsvall.arendeexport.HandelseIntressent;
import generated.se.sundsvall.arendeexport.ObjectFactory;
import generated.se.sundsvall.arendeexport.RollTyp;
import generated.se.sundsvall.arendeexport.StatusFilter;
import se.sundsvall.byggrintegrator.model.ByggrErrandDto;
import se.sundsvall.byggrintegrator.model.ByggrErrandDto.Event;
import se.sundsvall.byggrintegrator.model.ByggrErrandDto.Stakeholder;

/**
 * Mapper for handling mappings between ByggR responses and the internal dto class used in the service layer
 */
@Component
public class ByggrIntegrationMapper {
	private static final ObjectFactory OBJECT_FACTORY = new ObjectFactory();
	private static final List<String> DOCUMENT_TYPES_TO_OMIT = List.of("ANS", "ANM"); // Document type to filter out

	public GetRoller createGetRolesRequest() {
		return OBJECT_FACTORY.createGetRoller()
			.withRollTyp(RollTyp.INTRESSENT)
			.withStatusfilter(StatusFilter.AKTIV);
	}

	public GetRelateradeArendenByPersOrgNrAndRole mapToGetRelateradeArendenRequest(String id) {
		return OBJECT_FACTORY.createGetRelateradeArendenByPersOrgNrAndRole()
			.withStatusfilter(StatusFilter.AKTIV)
			.withPersOrgNr(id);
	}

	public GetArende mapToGetArendeRequest(String dnr) {
		return OBJECT_FACTORY.createGetArende()
			.withDnr(dnr);
	}

	public GetDocument mapToGetDocumentRequest(String documentId) {
		return new GetDocument()
			.withDocumentId(documentId)
			.withInkluderaFil(true);
	}

	public List<ByggrErrandDto> mapToByggErrandDtos(List<GetRelateradeArendenByPersOrgNrAndRoleResponse> responses) {
		return ofNullable(responses).orElse(emptyList()).stream()
			.map(this::extractErrands)
			.flatMap(Collection::stream)
			.map(this::toByggErrandDto)
			.toList();
	}

	public ByggrErrandDto mapToByggErrandDto(GetArendeResponse response) {
		return ofNullable(response)
			.map(GetArendeResponse::getGetArendeResult)
			.map(this::toByggErrandDto)
			.orElse(null);
	}

	private List<Arende> extractErrands(GetRelateradeArendenByPersOrgNrAndRoleResponse response) {
		return ofNullable(response.getGetRelateradeArendenByPersOrgNrAndRoleResult())
			.flatMap(result -> ofNullable(result.getArende())).stream()
			.flatMap(Collection::stream)
			.toList();
	}

	private ByggrErrandDto toByggErrandDto(Arende arende) {
		return ByggrErrandDto.builder()
			.withByggrCaseNumber(arende.getDnr())
			.withEvents(toEvents(arende.getHandelseLista()))
			.withStakeholders(toStakeholders(arende.getIntressentLista()))
			.build();
	}

	private List<Event> toEvents(ArrayOfHandelse handelser) {
		return ofNullable(handelser)
			.map(wrapper -> ofNullable(wrapper.getHandelse()).orElse(emptyList()))
			.stream()
			.flatMap(Collection::stream)
			.map(this::toEvent)
			.toList();
	}

	private Event toEvent(Handelse handelse) {
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

	private Map<String, String> toFiles(ArrayOfHandelseHandling handlingar) {
		return ofNullable(handlingar)
			.map(wrapper -> ofNullable(wrapper.getHandling()).orElse(emptyList()))
			.stream()
			.flatMap(Collection::stream)
			.filter(handling -> !DOCUMENT_TYPES_TO_OMIT.contains(handling.getTyp())) // Remove all documents of type ANS/ANM
			.map(HandelseHandling::getDokument)
			.filter(Objects::nonNull)
			.filter(document -> Objects.nonNull(document.getDokId()))
			.filter(document -> Objects.nonNull(document.getNamn()))
			.map(document -> Map.entry(document.getDokId(), document.getNamn()))
			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	private List<Stakeholder> toStakeholders(ArrayOfHandelseIntressent2 intressenter) {
		return ofNullable(intressenter)
			.map(wrapper -> ofNullable(wrapper.getIntressent()).orElse(emptyList()))
			.stream()
			.flatMap(Collection::stream)
			.map(this::toStakeholder)
			.toList();
	}

	private Stakeholder toStakeholder(HandelseIntressent intressent) {
		return Stakeholder.builder()
			.withLegalId(intressent.getPersOrgNr())
			.withRoles(toRoles(intressent.getRollLista()))
			.build();
	}

	private List<Stakeholder> toStakeholders(ArrayOfArendeIntressent2 intressenter) {
		return ofNullable(intressenter)
			.map(wrapper -> ofNullable(wrapper.getIntressent()).orElse(emptyList()))
			.stream()
			.flatMap(Collection::stream)
			.map(this::toStakeholder)
			.toList();
	}

	private Stakeholder toStakeholder(ArendeIntressent intressent) {
		return Stakeholder.builder()
			.withLegalId(intressent.getPersOrgNr())
			.withRoles(toRoles(intressent.getRollLista()))
			.build();
	}

	private List<String> toRoles(ArrayOfString2 roller) {
		return ofNullable(roller)
			.map(ArrayOfString2::getRoll)
			.orElse(emptyList());
	}
}
