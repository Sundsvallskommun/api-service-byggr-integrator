package se.sundsvall.byggrintegrator.integration.byggr;

import generated.se.sundsvall.arendeexport.v4.GetRemisserByPersOrgNr;
import generated.se.sundsvall.arendeexport.v8.AbstractArendeFastighet;
import generated.se.sundsvall.arendeexport.v8.Arende;
import generated.se.sundsvall.arendeexport.v8.ArendeFastighet;
import generated.se.sundsvall.arendeexport.v8.ArendeIntressent;
import generated.se.sundsvall.arendeexport.v8.ArkiveringStatus;
import generated.se.sundsvall.arendeexport.v8.ArrayOfAbstractArendeObjekt2;
import generated.se.sundsvall.arendeexport.v8.ArrayOfArendeIntressent2;
import generated.se.sundsvall.arendeexport.v8.ArrayOfHandelse;
import generated.se.sundsvall.arendeexport.v8.ArrayOfHandelseHandling;
import generated.se.sundsvall.arendeexport.v8.ArrayOfHandelseIntressent2;
import generated.se.sundsvall.arendeexport.v8.ArrayOfHandling;
import generated.se.sundsvall.arendeexport.v8.ArrayOfString2;
import generated.se.sundsvall.arendeexport.v8.Dokument;
import generated.se.sundsvall.arendeexport.v8.GetArende;
import generated.se.sundsvall.arendeexport.v8.GetArendeResponse;
import generated.se.sundsvall.arendeexport.v8.GetDocument;
import generated.se.sundsvall.arendeexport.v8.GetHandlingTyper;
import generated.se.sundsvall.arendeexport.v8.GetRelateradeArendenByFastighet;
import generated.se.sundsvall.arendeexport.v8.GetRelateradeArendenByFastighetResponse;
import generated.se.sundsvall.arendeexport.v8.GetRelateradeArendenByPersOrgNrAndRole;
import generated.se.sundsvall.arendeexport.v8.GetRelateradeArendenByPersOrgNrAndRoleResponse;
import generated.se.sundsvall.arendeexport.v8.GetRoller;
import generated.se.sundsvall.arendeexport.v8.Handelse;
import generated.se.sundsvall.arendeexport.v8.HandelseIntressent;
import generated.se.sundsvall.arendeexport.v8.ObjectFactory;
import generated.se.sundsvall.arendeexport.v8.RollTyp;
import generated.se.sundsvall.arendeexport.v8.SaveNewArende;
import generated.se.sundsvall.arendeexport.v8.StatusFilter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import se.sundsvall.byggrintegrator.model.ByggrErrandDto;
import se.sundsvall.byggrintegrator.model.ByggrErrandDto.Event;
import se.sundsvall.byggrintegrator.model.ByggrErrandDto.Stakeholder;
import se.sundsvall.byggrintegrator.service.util.ByggrFilterUtility;
import se.sundsvall.dept44.problem.Problem;

import static generated.se.sundsvall.arendeexport.v4.RemissStatusFilter.EJ_BESVARAD;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

/**
 * Mapper for handling mappings between ByggR responses and the internal dto class used in the service layer
 */
@Component
public class ByggrIntegrationMapper {
	private static final ObjectFactory OBJECT_FACTORY = new ObjectFactory();
	private final ByggrFilterUtility filterUtility;

	private static final String CASE_PREFIX_OVK = "OVK";
	private static final String ERRAND_GROUP_OVK = "OVK";
	private static final String UNIT_CODE_OVK = "OVK";
	private static final String EVENT_TYPE_OVK = "OVK";
	private static final String DOCUMENT_TYPE_OVK = "OVK";
	private static final String ERRAND_TYPE_FUNCTIONAL_CONTROL = "Funk";
	private static final String ERRAND_SUBTYPE_PROTOCOL = "PRT";
	private static final String EVENT_SUBTYPE_PROTOCOL = "PROT";
	private static final String NAME_CODE_STADBYGGNADSNAMDEN = "SBN";
	private static final String DIRECTION_INCOMING = "In";
	private static final String ERRAND_PROTOCOL_RUBRICATION = "Protokoll";
	private static final String DOCUMENT_STATUS_RECIEVED = "Inkommen";
	private static final String OVK_DOCUMENT_DESCRIPTION = "OVK-protokoll";

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

	public GetRelateradeArendenByFastighet mapToGetRelateradeArendenByFastighetRequest(final String propertyDesignation) {
		final var normalizedString = propertyDesignation.strip().replaceAll("\\s+", " ");
		final var indexOfSpace = normalizedString.lastIndexOf(" ");
		if (indexOfSpace < 1) {
			throw Problem.valueOf(BAD_REQUEST, "Invalid property designation: %s".formatted(propertyDesignation));
		}
		final var area = normalizedString.substring(0, indexOfSpace).strip();
		final var propertyId = normalizedString.substring(indexOfSpace + 1).strip();

		return OBJECT_FACTORY.createGetRelateradeArendenByFastighet()
			.withTrakt(area)
			.withFBetNr(propertyId)
			.withFnr(null)
			.withArHuvudObjekt(null)
			.withStatusFilter(StatusFilter.NONE);

	}

	public GetArende mapToGetArendeRequest(final String dnr) {
		return OBJECT_FACTORY.createGetArende()
			.withDnr(dnr);
	}

	public SaveNewArende mapToSaveOVKArendeRequest(final String municipalityId, final String propertyDesignation, final String description, final MultipartFile file) {
		final var datatypeFactory = DatatypeFactory.newDefaultInstance();
		final var today = datatypeFactory.newXMLGregorianCalendar(LocalDate.now(ZoneId.systemDefault()).toString());
		final var now = datatypeFactory.newXMLGregorianCalendar(GregorianCalendar.from(ZonedDateTime.now(ZoneId.systemDefault())));

		return OBJECT_FACTORY.createSaveNewArende().withMessage(OBJECT_FACTORY.createSaveNewArendeMessage()
			.withArende(createArende(municipalityId, description, today, propertyDesignation))
			.withHandelse(createOVKHandelse(now))
			.withHandlingar(createHandlingar(file, today))
			.withAnkomststamplaHandlingar(true));
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

	public List<ByggrErrandDto> mapRelateradeArendenByFastighetToByggrErrandDtos(final GetRelateradeArendenByFastighetResponse response) {
		return ofNullable(response)
			.map(GetRelateradeArendenByFastighetResponse::getGetRelateradeArendenByFastighetResult)
			.map(result -> ofNullable(result.getArende()).orElse(emptyList()))
			.orElse(emptyList())
			.stream()
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

	public static String toPropertyDesignation(final Arende arende) {
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
			.withConfidential(handelse.isSekretess())
			.withCancelled(handelse.isMakulerad())
			.build();
	}

	private Map<String, Event.DocumentNameAndType> toFiles(final ArrayOfHandelseHandling handlingar) {
		return ofNullable(handlingar)
			.map(wrapper -> ofNullable(wrapper.getHandling()).orElse(emptyList()))
			.stream()
			.flatMap(Collection::stream)
			.filter(filterUtility::hasValidDocumentType) // Filter out unwanted document types
			.filter(handelseHandling -> Objects.nonNull(handelseHandling.getDokument()))
			.map(handelseHandling -> new Triple(handelseHandling.getDokument().getDokId(), handelseHandling.getDokument().getNamn(), handelseHandling.getTyp()))
			.filter(triple -> Objects.nonNull(triple.dokumentId()))
			.filter(triple -> Objects.nonNull(triple.dokumentNamn()))
			.map(triple -> Map.entry(triple.dokumentId, new Event.DocumentNameAndType(triple.dokumentNamn, triple.handlingTyp)))
			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a1, a2) -> a1)); // In case of duplicate dokIds within the event, just pick the dokumentId one of them
	}

	private record Triple(String dokumentId, String dokumentNamn, String handlingTyp) {
	}

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

	public GetRemisserByPersOrgNr toGetRemisserByPersOrgNrRequest(final String identifier) {
		return new GetRemisserByPersOrgNr()
			.withPersOrgNr(identifier)
			.withStatusFilter(EJ_BESVARAD);
	}

	private Arende createArende(String municipalityId, String description, XMLGregorianCalendar today, String propertyDesignation) {
		return OBJECT_FACTORY.createArende()
			.withKomkod(municipalityId)
			.withDiarieprefix(CASE_PREFIX_OVK)
			.withEnhetkod(UNIT_CODE_OVK)
			.withNamndkod(NAME_CODE_STADBYGGNADSNAMDEN)
			.withArendegrupp(ERRAND_GROUP_OVK)
			.withArendetyp(ERRAND_TYPE_FUNCTIONAL_CONTROL)
			.withArendeslag(ERRAND_SUBTYPE_PROTOCOL)
			.withBeskrivning(description)
			.withAnkomstDatum(today)
			.withObjektLista(OBJECT_FACTORY.createArrayOfAbstractArendeObjekt2()
				.withAbstractArendeObjekt(createArendeFastighet(propertyDesignation)));
	}

	private AbstractArendeFastighet createArendeFastighet(String propertyDesignation) {
		final var trimmed = propertyDesignation.trim();
		final var indexOfSpace = trimmed.lastIndexOf(" ");
		final var trakt = trimmed.substring(0, indexOfSpace).trim();
		final var fBetNr = trimmed.substring(indexOfSpace + 1).trim();
		return OBJECT_FACTORY.createArendeFastighet()
			.withArHuvudObjekt(true)
			.withFastighet(OBJECT_FACTORY.createFastighet()
				.withTrakt(trakt)
				.withFbetNr(fBetNr));
	}

	private ArrayOfHandling createHandlingar(MultipartFile file, XMLGregorianCalendar today) {
		return OBJECT_FACTORY.createArrayOfHandling()
			.withHandling(OBJECT_FACTORY.createHandelseHandling()
				.withTyp(DOCUMENT_TYPE_OVK)
				.withStatus(DOCUMENT_STATUS_RECIEVED)
				.withHandlingDatum(today)
				.withArkivStatus(ArkiveringStatus.ARKIVERAS)
				.withDokument(createOVKDokument(file)));
	}

	private Dokument createOVKDokument(MultipartFile file) {
		final var filename = file.getOriginalFilename();

		return OBJECT_FACTORY.createDokument()
			.withNamn(filename)
			.withBeskrivning(OVK_DOCUMENT_DESCRIPTION)
			.withFil(OBJECT_FACTORY.createDokumentFil()
				.withFilBuffer(readContent(file))
				.withFilAndelse(StringUtils.getFilenameExtension(filename)));
	}

	private Handelse createOVKHandelse(final XMLGregorianCalendar now) {
		return OBJECT_FACTORY.createHandelse()
			.withRiktning(DIRECTION_INCOMING)
			.withRubrik(ERRAND_PROTOCOL_RUBRICATION)
			.withHandelsetyp(EVENT_TYPE_OVK)
			.withHandelseslag(EVENT_SUBTYPE_PROTOCOL)
			.withStartDatum(now);
	}

	private static byte[] readContent(final MultipartFile file) {
		try {
			return file.getBytes();
		} catch (final IOException e) {
			throw Problem.valueOf(INTERNAL_SERVER_ERROR, "could not read uploaded file");
		}
	}

}
