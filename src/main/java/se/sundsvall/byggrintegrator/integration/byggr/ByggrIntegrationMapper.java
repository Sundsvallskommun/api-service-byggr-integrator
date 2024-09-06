package se.sundsvall.byggrintegrator.integration.byggr;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import se.sundsvall.byggrintegrator.model.ByggrErrandDto;

import generated.se.sundsvall.arendeexport.AbstractArendeObjekt;
import generated.se.sundsvall.arendeexport.Arende;
import generated.se.sundsvall.arendeexport.ArendeFastighet;
import generated.se.sundsvall.arendeexport.ArendeIntressent;
import generated.se.sundsvall.arendeexport.ArrayOfString2;
import generated.se.sundsvall.arendeexport.Dokument;
import generated.se.sundsvall.arendeexport.GetArende;
import generated.se.sundsvall.arendeexport.GetArendeResponse;
import generated.se.sundsvall.arendeexport.GetRelateradeArendenByPersOrgNrAndRole;
import generated.se.sundsvall.arendeexport.GetRelateradeArendenByPersOrgNrAndRoleResponse;
import generated.se.sundsvall.arendeexport.GetRoller;
import generated.se.sundsvall.arendeexport.Handelse;
import generated.se.sundsvall.arendeexport.Handling;
import generated.se.sundsvall.arendeexport.ObjectFactory;
import generated.se.sundsvall.arendeexport.RollTyp;
import generated.se.sundsvall.arendeexport.StatusFilter;

@Component
public class ByggrIntegrationMapper {
	private static final Logger LOG = LoggerFactory.getLogger(ByggrIntegrationMapper.class);

	private static final String WANTED_HANDELSETYP = "GRANHO";
	private static final String WANTED_HANDELSESLAG = "GRAUTS";
	private static final String UNWANTED_HANDELSESLAG = "GRASVA";
	private static final ObjectFactory OBJECT_FACTORY = new ObjectFactory();

	private final List<String> roles;

	public ByggrIntegrationMapper(@Value("${integration.byggr.mapper.applicant.roles}") List<String> roles) {
		this.roles = roles;
	}

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

	/**
	 * Maps the response from Byggr and maps it to a narrowed down list of ByggrErrandDtos containing all neighborhood
	 * notifications
	 *
	 * @param response The response from Byggr
	 * @return A list of ByggrErrandDtos containing neighborhood notifications
	 */
	public List<ByggrErrandDto> mapToNeighborhoodNotifications(GetRelateradeArendenByPersOrgNrAndRoleResponse response) {
		final var errands = extractErrands(response);

		// Collect the info we want from errands that have a valid event
		return errands.stream()
			.filter(arende -> hasValidHandelseList(arende.getHandelseLista().getHandelse()))
			.map(this::toByggErrandDto)
			.toList();
	}

	/**
	 * Maps the response from Byggr and maps it to a narrowed down list of ByggrErrandDtos where sent in legal id is
	 * applicant
	 *
	 * @param response The response from Byggr
	 * @param legalId  The legal id for the applicant party to fetch errands for
	 * @return A list of ByggrErrandDtos containing errands where sent in legal id is applicant
	 */
	public List<ByggrErrandDto> mapToApplicantErrands(GetRelateradeArendenByPersOrgNrAndRoleResponse response, String legalId) {
		final var errands = extractErrands(response);

		// Collect the info we want from errands that have a valid event
		return errands.stream()
			.filter(arende -> isApplicant(arende.getIntressentLista().getIntressent(), legalId))
			.map(this::toByggErrandDto)
			.toList();
	}

	/**
	 * Maps the list of AbstractArendeObjekt to a list of PropertyDesignation
	 *
	 * @param abstractArendeObjektList The list of AbstractArendeObjekt
	 * @return A list of PropertyDesignation to be added to the NeighborhoodNotificationsDto
	 */
	private List<ByggrErrandDto.PropertyDesignation> mapToPropertyDesignations(List<AbstractArendeObjekt> abstractArendeObjektList) {
		return abstractArendeObjektList.stream()
			.filter(ArendeFastighet.class::isInstance)
			.map(ArendeFastighet.class::cast)
			.map(ArendeFastighet::getFastighet)
			.filter(Objects::nonNull)
			.map(fastighet -> ByggrErrandDto.PropertyDesignation.builder()
				.withProperty(fastighet.getTrakt())
				.withDesignation(fastighet.getFbetNr())
				.build())
			.toList();
	}

	private List<Arende> extractErrands(GetRelateradeArendenByPersOrgNrAndRoleResponse response) {
		return Optional.ofNullable(response.getGetRelateradeArendenByPersOrgNrAndRoleResult())
			.flatMap(result -> Optional.ofNullable(result.getArende())).stream()
			.flatMap(Collection::stream)
			.toList();
	}

	private ByggrErrandDto toByggErrandDto(Arende arende) {
		return ByggrErrandDto.builder()
			.withByggrCaseNumber(arende.getDnr())
			.withPropertyDesignation(mapToPropertyDesignations(arende.getObjektLista().getAbstractArendeObjekt()))
			.build();
	}

	private boolean isApplicant(List<ArendeIntressent> arendeIntressentList, String identifier) {
		return Optional.ofNullable(arendeIntressentList).orElse(Collections.emptyList()).stream()
			.filter(intressent -> StringUtils.equals(intressent.getPersOrgNr(), identifier))
			.map(ArendeIntressent::getRollLista)
			.map(ArrayOfString2::getRoll)
			.anyMatch(roller -> roller.stream().anyMatch(roles::contains));
	}

	private boolean hasValidHandelseList(List<Handelse> handelseList) {
		var hasValidEvent = false;
		var hasInvalidEvent = false;

		for (final Handelse handelse : handelseList) {
			if (hasValidEvent(handelse)) {
				hasValidEvent = true;
			}
			if (hasInvalidEvent(handelse)) {
				hasInvalidEvent = true;
			}
		}

		return hasValidEvent && !hasInvalidEvent;
	}

	private boolean hasValidEvent(Handelse event) {
		if (event.getHandelsetyp().equals(WANTED_HANDELSETYP) && event.getHandelseslag().equals(WANTED_HANDELSESLAG)) {
			LOG.info("Valid eventid with handelsetyp GRANHO and handelseslag GRAUTS found: {}", event.getHandelseId());
			return true;
		}
		return false;
	}

	private boolean hasInvalidEvent(Handelse event) {
		if (event.getHandelsetyp().equals(WANTED_HANDELSETYP) && event.getHandelseslag().equals(UNWANTED_HANDELSESLAG)) {
			LOG.info("Unwanted eventid with handelsetyp GRANHO and handelseslag GRASVA found: {}", event.getHandelseId());
			return true;
		}
		return false;
	}

	public GetArende createGetArendeRequest(String caseNumber) {
		return OBJECT_FACTORY.createGetArende()
			.withDnr(caseNumber);
	}

	/**
	 * Takes the response from Byggr and maps it to a map of Document ID to Document name
	 * and adds it to a ByggErrandDto
	 * Only files from a valid event are included
	 *
	 * @param response The response from Byggr
	 * @return a ByggrErrandDto
	 */
	public ByggrErrandDto mapToNeighborhoodNotificationFiles(GetArendeResponse response) {
		var errandDto = ByggrErrandDto.builder()
			.withFiles(new HashMap<>())
			.build();

		if (response == null
			|| response.getGetArendeResult() == null
			|| response.getGetArendeResult().getHandelseLista() == null
			|| response.getGetArendeResult().getHandelseLista().getHandelse() == null
			|| response.getGetArendeResult().getHandelseLista().getHandelse().isEmpty()) {
			// Just return a dto with an empty map for files
			return errandDto;
		}

		// Now we know that we have an event (handelse) in the response
		// Map the Document ID to the Document name (if we can).
		var fileMap = response.getGetArendeResult().getHandelseLista().getHandelse().stream()
			.filter(handelse -> handelse.getHandlingLista() != null) //Not a list
			.filter(handelse -> !CollectionUtils.isEmpty(handelse.getHandlingLista().getHandling())) //Is a list
			.filter(this::hasValidEvent)    // Only include files from valid events
			.flatMap(handelse -> handelse.getHandlingLista().getHandling().stream())
			.map(Handling::getDokument)
			.filter(Objects::nonNull)
			.filter(dokument -> isNotBlank(dokument.getDokId()))
			.filter(dokument -> isNotBlank(dokument.getNamn()))
			.collect(Collectors.toMap(
				Dokument::getDokId,
				Dokument::getNamn
			));

		errandDto.setByggrCaseNumber(response.getGetArendeResult().getDnr());
		errandDto.setFiles(fileMap);

		return errandDto;
	}
}
