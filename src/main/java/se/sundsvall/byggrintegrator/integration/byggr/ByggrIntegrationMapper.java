package se.sundsvall.byggrintegrator.integration.byggr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import se.sundsvall.byggrintegrator.model.NeighborhoodNotificationsDto;

import generated.se.sundsvall.arendeexport.AbstractArendeObjekt;
import generated.se.sundsvall.arendeexport.ArendeFastighet;
import generated.se.sundsvall.arendeexport.GetRelateradeArendenByPersOrgNrAndRole;
import generated.se.sundsvall.arendeexport.GetRelateradeArendenByPersOrgNrAndRoleResponse;
import generated.se.sundsvall.arendeexport.GetRoller;
import generated.se.sundsvall.arendeexport.Handelse;
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

	/**
	 * Maps the response from Byggr and maps it to a narrowed down list of NeighborhoodNotificationsDtos
	 * @param response The response from Byggr
	 * @return A list of NeighborhoodNotificationsDtos
	 */
	public List<NeighborhoodNotificationsDto> mapToNeighborhoodNotificationsDto(GetRelateradeArendenByPersOrgNrAndRoleResponse response) {
		var errands = Optional.ofNullable(response.getGetRelateradeArendenByPersOrgNrAndRoleResult())
			.flatMap(result -> Optional.ofNullable(result.getArende())).stream()
				.flatMap(Collection::stream)
			.toList();

		List<NeighborhoodNotificationsDto> neighborhoodNotificationsDtoList = new ArrayList<>();

		// Collect the info we want from errands that have a valid event
		errands.forEach(arende -> {
			if(hasValidHandelseList(arende.getHandelseLista().getHandelse())) {
				neighborhoodNotificationsDtoList.add(NeighborhoodNotificationsDto.builder()
					.withByggrErrandNumber(arende.getDnr())
					.withPropertyDesignation(mapToPropertyDesignations(arende.getObjektLista().getAbstractArendeObjekt()))
				.build());
			}
		});

		return neighborhoodNotificationsDtoList;
	}

	/**
	 * Maps the list of AbstractArendeObjekt to a list of PropertyDesignation
	 * @param abstractArendeObjektList The list of AbstractArendeObjekt
	 * @return A list of PropertyDesignation to be added to the NeighborhoodNotificationsDto
	 */
	private List<NeighborhoodNotificationsDto.PropertyDesignation> mapToPropertyDesignations(List<AbstractArendeObjekt> abstractArendeObjektList) {
		return abstractArendeObjektList.stream()
			.filter(ArendeFastighet.class::isInstance)
			.map(ArendeFastighet.class::cast)
			.map(ArendeFastighet::getFastighet)
			.filter(Objects::nonNull)
			.map(fastighet -> NeighborhoodNotificationsDto.PropertyDesignation.builder()
				.withProperty(fastighet.getTrakt())
				.withDesignation(fastighet.getFbetNr())
			.build())
			.toList();
	}

	private boolean hasValidHandelseList(List<Handelse> handelseList) {
		var hasValidEvent = false;
		var hasInvalidEvent = false;

		for (Handelse handelse : handelseList) {
			if(hasValidEvent(handelse)) {
				hasValidEvent = true;
			}
			if(hasInvalidEvent(handelse)) {
				hasInvalidEvent = true;
			}
		}

		return hasValidEvent && !hasInvalidEvent;
	}

	private boolean hasValidEvent(Handelse event) {
		if(event.getHandelsetyp().equals(WANTED_HANDELSETYP) && event.getHandelseslag().equals(WANTED_HANDELSESLAG)) {
			LOG.info("Valid eventid with handelsetyp GRANHO and handelseslag GRAUTS found: {}", event.getHandelseId());
			return true;
		}
		return false;
	}

	private boolean hasInvalidEvent(Handelse event) {
		if(event.getHandelsetyp().equals(WANTED_HANDELSETYP) && event.getHandelseslag().equals(UNWANTED_HANDELSESLAG)) {
			LOG.info("Unwanted eventid with handelsetyp GRANHO and handelseslag GRASVA found: {}", event.getHandelseId());
			return true;
		}
		return false;
	}
}
