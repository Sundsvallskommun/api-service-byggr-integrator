package se.sundsvall.byggrintegrator.service.util;

import static java.time.LocalDate.now;
import static java.util.Optional.ofNullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import se.sundsvall.byggrintegrator.model.ByggrErrandDto;
import se.sundsvall.byggrintegrator.model.ByggrErrandDto.Event;
import se.sundsvall.byggrintegrator.model.ByggrErrandDto.Stakeholder;
import se.sundsvall.byggrintegrator.service.util.ByggrFilterProperties.ApplicantProperties;
import se.sundsvall.byggrintegrator.service.util.ByggrFilterProperties.NotificationProperties;

/**
 * The filter utility has two configuratble settings:
 *
 * <code>
 *   service:
 *     byggr:
 *       filter-utility:
 *         applicant:
 *           roles: <comma separated list of role>
 *         notifications:
 *           unwanted-event-types: <comma separated list of unwanted event type>
 * </code>
 *
 * The first setting (applicant roles) contains the list of the roles that will be matched against to establish if the
 * stakeholder is to be interpreted as applicant for the errand or not. If the stakeholder matches one of the values in
 * the list, it is interpreted as applicant for the errand.
 *
 * The second setting (notification unwanted-event-types) is used to filter out errands when collecting neighborhood
 * notifications. If an errand contains an event with event type matching one of the defined value(s), the errand is
 * filtered out from the returned response. If property is not set, then no filtering is made. Observe that filtering is
 * always done regarding that the errand must have a GRANHO event with event type GRAUTS to be returned in the response.
 *
 */
@Component
public class ByggrFilterUtility {
	private static final Logger LOG = LoggerFactory.getLogger(ByggrFilterUtility.class);

	private static final String WANTED_TYPE = "GRANHO";
	private static final String WANTED_SUBTYPE = "GRAUTS";

	private final List<String> applicantRoles;
	private final List<String> unwantedSubtypes;

	public ByggrFilterUtility(final ByggrFilterProperties byggrProperties) {
		this.applicantRoles = ofNullable(byggrProperties)
			.map(ByggrFilterProperties::applicant)
			.map(ApplicantProperties::roles)
			.orElse(null);
		this.unwantedSubtypes = ofNullable(byggrProperties)
			.map(ByggrFilterProperties::notifications)
			.map(NotificationProperties::unwantedEventTypes)
			.orElse(null);
	}

	public static boolean hasValidEvent(Event event) {
		if (Objects.nonNull(event) && WANTED_TYPE.equalsIgnoreCase(event.getEventType()) && WANTED_SUBTYPE.equalsIgnoreCase(event.getEventSubtype())) {
			LOG.info("Valid event with type {} and subtype {} having id {} found", WANTED_TYPE, WANTED_SUBTYPE, event.getId());
			return true;
		}
		return false;
	}

	/**
	 * Filters the incoming list to a narrowed down list containing all neighborhood notifications for a specific
	 * stakeholder
	 *
	 * @param errands    A list containing the full response from Byggr
	 * @param identifier The identifier for the neighbor stakeholder to use when filtering errands
	 * @return A list containing neighborhood notifications where neighborhood legal id matches sent in identfier
	 */
	public List<ByggrErrandDto> filterNeighborhoodNotifications(List<ByggrErrandDto> errands, String identifier) {
		return errands.stream()
			.filter(errand -> hasValidEvent(errand.getByggrCaseNumber(), errand.getEvents()))
			.map(errand -> filterEvents(identifier, errand))
			.filter(errand -> !errand.getEvents().isEmpty())
			.toList();
	}

	private boolean hasValidEvent(String dnr, List<Event> events) {
		LOG.info("Validating case with dnr {}", dnr);

		var hasValidEvent = false;
		var hasInvalidEvent = false;

		for (final Event event : events) {
			if (hasValidEvent(event)) {
				hasValidEvent = true;
			}
			if (hasInvalidEvent(event)) {
				hasInvalidEvent = true;
			}
		}

		return hasValidEvent && !hasInvalidEvent;
	}

	private boolean hasInvalidEvent(Event event) {
		final boolean unwantedEvent = ofNullable(unwantedSubtypes)
			.map(list -> WANTED_TYPE.equalsIgnoreCase(event.getEventType()) && list.stream().anyMatch(event.getEventSubtype()::equalsIgnoreCase))
			.orElse(false);

		if (unwantedEvent) {
			LOG.info("Unwanted eventid with type {} and subtype matching one of {} found: {}", WANTED_TYPE, unwantedSubtypes, event.getId());
		}

		return unwantedEvent;
	}

	private ByggrErrandDto filterEvents(String identifier, ByggrErrandDto errand) {
		final var filteredEvents = errand.getEvents().stream()
			.filter(event -> WANTED_TYPE.equalsIgnoreCase(event.getEventType()))
			.filter(event -> WANTED_SUBTYPE.equalsIgnoreCase(event.getEventSubtype()))
			.filter(event -> ofNullable(event.getEventDate()).map(eventDate -> eventDate.isAfter(now().minusDays(31))).orElse(false)) // Events must have a date and not be older than 30 days to be included
			.filter(event -> event.getStakeholders().stream().anyMatch(stakeholder -> identifier.equals(stakeholder.getLegalId())))
			.toList();

		errand.setEvents(filteredEvents);
		return errand;
	}

	/**
	 * Filters the incoming list to a narrowed down list with errands where sent in legal id is applicant
	 *
	 * @param errands A list containing the full response from Byggr
	 * @param legalId The legal id for the applicant party to filter out errands on
	 * @return A list containing errands where sent in legal id is applicant
	 */
	public List<ByggrErrandDto> filterCasesForApplicant(List<ByggrErrandDto> errands, String legalId) {
		return errands.stream()
			.filter(errand -> isApplicant(errand.getStakeholders(), legalId))
			.toList();
	}

	private boolean isApplicant(List<Stakeholder> stakeholders, String identifier) {
		return ofNullable(stakeholders).orElse(Collections.emptyList()).stream()
			.filter(stakeholder -> StringUtils.equals(stakeholder.getLegalId(), identifier))
			.map(Stakeholder::getRoles)
			.anyMatch(roller -> roller.stream().anyMatch(applicantRoles::contains));
	}

	/**
	 * Filters the incoming list to a narrowed down list containing only the event matching the
	 * sent in event id (and it's parent case)
	 *
	 * @param errand  The full response from Byggr
	 * @param eventId The id to filter on regarding which event that should be present in the response
	 * @return An errand where only the event matching the provided event id is present
	 */
	public ByggrErrandDto filterEvent(ByggrErrandDto errand, int eventId) {
		if (Objects.isNull(errand)) {
			return errand;
		}

		final var filteredEvents = errand.getEvents().stream()
			.filter(event -> event.getId() == eventId)
			.toList();

		errand.setEvents(filteredEvents);
		return errand;
	}
}
