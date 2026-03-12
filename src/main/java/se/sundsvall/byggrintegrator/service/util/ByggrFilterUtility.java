package se.sundsvall.byggrintegrator.service.util;

import generated.se.sundsvall.arendeexport.v8.HandelseHandling;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import se.sundsvall.byggrintegrator.model.ByggrErrandDto;
import se.sundsvall.byggrintegrator.model.ByggrErrandDto.Event;
import se.sundsvall.byggrintegrator.model.ByggrErrandDto.Stakeholder;
import se.sundsvall.byggrintegrator.service.util.ByggrFilterProperties.ApplicantProperties;
import se.sundsvall.byggrintegrator.service.util.ByggrFilterProperties.DocumentProperties;
import se.sundsvall.byggrintegrator.service.util.ByggrFilterProperties.NotificationProperties;

import static java.time.LocalDate.now;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

/**
 * The filter utility has three configurable settings:
 * <p>
 * <code>
 * service: byggr: filter-utility: applicant: roles: - Example - Example2 notifications: unwanted-event-types: - Example - Example2 - Example3 document-types: unwanted-document-types: - Example - Example2
 * </code>
 * <p>
 * The first setting (applicant roles) contains the list of the roles that will be matched against to establish if the
 * stakeholder is to be interpreted as applicant for the errand or not. If the stakeholder matches one of the values in
 * the list, it is
 * interpreted as applicant for the errand.
 * <p>
 * The second setting (notification unwanted-event-types) is used to filter out errands when collecting neighborhood
 * notifications. If an errand contains an event with event type matching one of the defined value(s), the errand is
 * filtered out from the
 * returned response. If property is not set, then no filtering is made. Observe that filtering is always done regarding
 * that the errand must have a valid event type/subtype pair (GRANHO/GRAUTS or KOMFAST/KOMFASVA) to be returned in the
 * response.
 * <p>
 * The third setting (document-types unwanted-document-types) is used to filter documents when fetching a
 * neighborhood-notification. There is some documents that should not be included in the response and this setting is
 * used to filter out those documents.
 * If property is not set, then no filtering is made.
 */
@Component
public class ByggrFilterUtility {

	private static final Logger LOG = LoggerFactory.getLogger(ByggrFilterUtility.class);

	private static final Map<String, String> WANTED_TYPE_SUBTYPE_PAIRS = Map.of(
		"GRANHO", "GRAUTS",
		"KOMFAST", "KOMFASVA");

	private final List<String> applicantRoles;
	private final List<String> unwantedSubtypes;
	private final List<String> unwantedDocumentTypes;

	public ByggrFilterUtility(final ByggrFilterProperties byggrProperties) {
		this.applicantRoles = ofNullable(byggrProperties)
			.map(ByggrFilterProperties::applicant)
			.map(ApplicantProperties::roles)
			.orElse(null);
		this.unwantedSubtypes = ofNullable(byggrProperties)
			.map(ByggrFilterProperties::notifications)
			.map(NotificationProperties::unwantedEventTypes)
			.orElse(null);
		this.unwantedDocumentTypes = ofNullable(byggrProperties)
			.map(ByggrFilterProperties::documentTypes)
			.map(DocumentProperties::unwantedDocumentTypes)
			.orElse(null);
	}

	public static boolean isValidEvent(final Event event) {
		if (Objects.nonNull(event) && isWantedTypePair(event.getEventType(), event.getEventSubtype())) {
			LOG.info("Valid event with type {} and subtype {} having id {} found", event.getEventType(), event.getEventSubtype(), event.getId());
			return true;
		}
		return false;
	}

	private static boolean isWantedTypePair(final String type, final String subtype) {
		return WANTED_TYPE_SUBTYPE_PAIRS.entrySet().stream()
			.anyMatch(entry -> entry.getKey().equalsIgnoreCase(type) && entry.getValue().equalsIgnoreCase(subtype));
	}

	private static boolean isWantedType(final String type) {
		return WANTED_TYPE_SUBTYPE_PAIRS.keySet().stream()
			.anyMatch(key -> key.equalsIgnoreCase(type));
	}

	/**
	 * Filters the incoming list to a narrowed down list containing all neighborhood notifications for a specific
	 * stakeholder
	 *
	 * @param  errands    A list containing the full response from Byggr
	 * @param  identifier The identifier for the neighbor stakeholder to use when filtering errands
	 * @return            A list containing neighborhood notifications where neighborhood legal id matches sent in identfier
	 */
	public List<ByggrErrandDto> filterNeighborhoodNotifications(final List<ByggrErrandDto> errands, final String identifier) {
		return errands.stream()
			.filter(errand -> isNotEmpty(errand.getEvents()))
			.filter(errand -> hasValidEvent(errand.getByggrCaseNumber(), errand.getEvents()))
			.map(errand -> filterEvents(identifier, errand))
			.filter(errand -> isNotEmpty(errand.getEvents()))
			.toList();
	}

	private boolean hasValidEvent(final String dnr, final List<Event> events) {
		LOG.info("Validating case with dnr {}", dnr);

		var hasValidEvent = false;
		var hasInvalidEvent = false;

		for (final Event event : events) {
			if (isValidEvent(event)) {
				hasValidEvent = true;
			}
			if (isInvalidEvent(event)) {
				hasInvalidEvent = true;
			}
		}

		return hasValidEvent && !hasInvalidEvent;
	}

	private boolean isInvalidEvent(final Event event) {
		final var unwantedEvent = Objects.nonNull(event) && ofNullable(unwantedSubtypes)
			.map(list -> isWantedType(event.getEventType()) && list.stream().anyMatch(event.getEventSubtype()::equalsIgnoreCase))
			.orElse(false);

		if (unwantedEvent) {
			LOG.info("Unwanted event with type {} and subtype matching one of {} found: {}", event.getEventType(), unwantedSubtypes, event.getId());
		}

		return unwantedEvent;
	}

	public ByggrErrandDto filterEvents(final String identifier, final ByggrErrandDto errand) {
		if (isNull(errand)) {
			return null;
		}

		final var filteredEvents = errand.getEvents().stream()
			.filter(event -> isWantedTypePair(event.getEventType(), event.getEventSubtype()))
			.filter(event -> ofNullable(event.getEventDate()).map(eventDate -> eventDate.isAfter(now().minusDays(61))).orElse(false)) // Events must have a date and not be older than 60 days to be included
			.filter(event -> event.getStakeholders().stream().anyMatch(stakeholder -> LegalIdUtility.isEqual(stakeholder.getLegalId(), identifier)))
			.toList();

		errand.setEvents(filteredEvents);
		return errand;
	}

	public boolean hasValidDocumentType(final HandelseHandling handling) {
		return ofNullable(handling)
			.map(HandelseHandling::getTyp)
			.map(type -> !unwantedDocumentTypes.contains(type))
			.orElse(true);
	}

	/**
	 * Filters the incoming list to a narrowed down list with errands where sent in legal id is applicant
	 *
	 * @param  errands A list containing the full response from Byggr
	 * @param  legalId The legal id for the applicant party to filter out errands on
	 * @return         A list containing errands where sent in legal id is applicant
	 */
	public List<ByggrErrandDto> filterCasesForApplicant(final List<ByggrErrandDto> errands, final String legalId) {
		return errands.stream()
			.filter(errand -> isApplicant(errand.getStakeholders(), legalId))
			.toList();
	}

	private boolean isApplicant(final List<Stakeholder> stakeholders, final String identifier) {
		return ofNullable(stakeholders).orElse(Collections.emptyList()).stream()
			.filter(stakeholder -> LegalIdUtility.isEqual(stakeholder.getLegalId(), identifier))
			.map(Stakeholder::getRoles)
			.anyMatch(roller -> roller.stream().anyMatch(applicantRoles::contains));
	}
}
