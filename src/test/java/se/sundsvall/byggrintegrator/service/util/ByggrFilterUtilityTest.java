package se.sundsvall.byggrintegrator.service.util;

import generated.se.sundsvall.arendeexport.v8.Arende;
import generated.se.sundsvall.arendeexport.v8.ArrayOfHandelse;
import generated.se.sundsvall.arendeexport.v8.Dokument;
import generated.se.sundsvall.arendeexport.v8.Handelse;
import generated.se.sundsvall.arendeexport.v8.HandelseHandling;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.byggrintegrator.model.ByggrErrandDto;
import se.sundsvall.byggrintegrator.model.ByggrErrandDto.Event;
import se.sundsvall.byggrintegrator.model.ByggrErrandDto.Stakeholder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mockStatic;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ExtendWith(MockitoExtension.class)
class ByggrFilterUtilityTest {
	private static final String STAKEHOLDER_LEGAL_ID = "stakeholderLegalId";
	private static final LocalDate FIXED_TODAY = LocalDate.of(2026, Month.JUNE, 11);

	@Mock
	private ByggrFilterProperties byggrFilterPropertiesMock;

	@InjectMocks
	private ByggrFilterUtility byggrFilterUtility;

	private static Stream<Arguments> validEventArgumentProvider() {
		return Stream.of(
			Arguments.of(null, false),
			Arguments.of(createEvent(null, null), false),
			Arguments.of(createEvent("GRANHO", null), false),
			Arguments.of(createEvent(null, "GRAUTS"), false),
			Arguments.of(createEvent("type", "subtype"), false),
			Arguments.of(createEvent("granho", "grauts"), true),
			Arguments.of(createEvent("GRANHO", "GRAUTS"), true),
			Arguments.of(createEvent("KOMFAST", null), false),
			Arguments.of(createEvent(null, "KOMFASUTS"), false),
			Arguments.of(createEvent("komfast", "komfasuts"), true),
			Arguments.of(createEvent("KOMFAST", "KOMFASUTS"), true),
			Arguments.of(createEvent("GRANHO", "KOMFASUTS"), false),
			Arguments.of(createEvent("KOMFAST", "GRAUTS"), false));
	}

	private static Stream<Arguments> filterNeighborhoodNotificationsArgumentProvider() {
		return Stream.of(
			Arguments.of(List.of(createEvent(null, "GRAUTS", FIXED_TODAY)), STAKEHOLDER_LEGAL_ID, 0, 0),
			Arguments.of(List.of(createEvent("GRANHO", null, FIXED_TODAY)), STAKEHOLDER_LEGAL_ID, 0, 0),
			Arguments.of(List.of(createEvent("GRANHO", "GRAUTS", FIXED_TODAY)), "otherId", 0, 0),
			Arguments.of(List.of(createEvent("type", "subtype", FIXED_TODAY)), STAKEHOLDER_LEGAL_ID, 0, 0),
			Arguments.of(List.of(createEvent("GRANHO", "GRAUTS", FIXED_TODAY.minusDays(61))), STAKEHOLDER_LEGAL_ID, 0, 0),
			Arguments.of(List.of(createEvent("GRANHO", "GRAUTS", FIXED_TODAY.minusDays(30))), STAKEHOLDER_LEGAL_ID, 1, 1),
			Arguments.of(List.of(createEvent("granho", "grauts", FIXED_TODAY)), STAKEHOLDER_LEGAL_ID, 1, 1),
			Arguments.of(List.of(
				createEvent("granho", "grauts", FIXED_TODAY),
				createEvent("granho", "grauts", FIXED_TODAY)), STAKEHOLDER_LEGAL_ID, 1, 2),
			Arguments.of(List.of(createEvent(null, "KOMFASUTS", FIXED_TODAY)), STAKEHOLDER_LEGAL_ID, 0, 0),
			Arguments.of(List.of(createEvent("KOMFAST", null, FIXED_TODAY)), STAKEHOLDER_LEGAL_ID, 0, 0),
			Arguments.of(List.of(createEvent("KOMFAST", "KOMFASUTS", FIXED_TODAY)), "otherId", 0, 0),
			Arguments.of(List.of(createEvent("KOMFAST", "KOMFASUTS", FIXED_TODAY.minusDays(61))), STAKEHOLDER_LEGAL_ID, 0, 0),
			Arguments.of(List.of(createEvent("KOMFAST", "KOMFASUTS", FIXED_TODAY.minusDays(30))), STAKEHOLDER_LEGAL_ID, 1, 1),
			Arguments.of(List.of(createEvent("komfast", "komfasuts", FIXED_TODAY)), STAKEHOLDER_LEGAL_ID, 1, 1),
			Arguments.of(List.of(
				createEvent("KOMFAST", "KOMFASUTS", FIXED_TODAY),
				createEvent("GRANHO", "GRAUTS", FIXED_TODAY)), STAKEHOLDER_LEGAL_ID, 1, 2));
	}

	private static Stream<Arguments> filterErrandsForApplicantArgumentProvider() {
		return Stream.of(
			Arguments.of(createErrand(List.of(Stakeholder.builder().withLegalId(STAKEHOLDER_LEGAL_ID).withRoles(List.of("SOK")).build())), 1),
			Arguments.of(createErrand(List.of(Stakeholder.builder().withLegalId(STAKEHOLDER_LEGAL_ID).withRoles(List.of("KPER")).build())), 1),
			Arguments.of(createErrand(List.of(Stakeholder.builder().withLegalId("OTHER_ID").withRoles(List.of("SOK")).build())), 0),
			Arguments.of(createErrand(List.of(Stakeholder.builder().withLegalId("OTHER_ID").withRoles(List.of("KPER")).build())), 0),
			Arguments.of(createErrand(List.of(Stakeholder.builder().withLegalId(STAKEHOLDER_LEGAL_ID).withRoles(List.of("OTHER_ROLE")).build())), 0),
			Arguments.of(createErrand(null), 0));
	}

	private static ByggrErrandDto createErrand(final List<Stakeholder> stakeholders) {
		return ByggrErrandDto.builder()
			.withStakeholders(stakeholders)
			.build();
	}

	private static Event createEvent(final String type, final String subtype) {
		return Event.builder()
			.withEventType(type)
			.withEventSubtype(subtype)
			.build();
	}

	private static Event createEvent(final String type, final String subtype, final LocalDate date) {
		return Event.builder()
			.withEventType(type)
			.withEventSubtype(subtype)
			.withEventDate(date)
			.withStakeholders(List.of(Stakeholder.builder()
				.withLegalId(ByggrFilterUtilityTest.STAKEHOLDER_LEGAL_ID)
				.build()))
			.build();
	}

	@ParameterizedTest
	@MethodSource("validEventArgumentProvider")
	void isValidEvent(final Event event, final boolean expectedResult) {
		// Act and assert
		assertThat(ByggrFilterUtility.isValidEvent(event)).isEqualTo(expectedResult);
	}

	@ParameterizedTest
	@MethodSource("filterNeighborhoodNotificationsArgumentProvider")
	void filterNeighborhoodNotifications(final List<Event> events, final String identifier, final int expectedErrandsSize, final int expectedEventsSize) {
		// Act - pin the clock so the 60-day cutoff in the filter is deterministic
		try (final var localDateMock = mockStatic(LocalDate.class, CALLS_REAL_METHODS)) {
			localDateMock.when(() -> LocalDate.now(ZoneId.systemDefault())).thenReturn(FIXED_TODAY);

			final var errands = byggrFilterUtility.filterNeighborhoodNotifications(List.of(
				ByggrErrandDto.builder()
					.withEvents(events)
					.build()), identifier);

			// Assert
			assertThat(errands).hasSize(expectedErrandsSize);
			if (expectedErrandsSize > 0) {
				assertThat(errands.getFirst().getEvents()).hasSize(expectedEventsSize);
			}
		}
	}

	@Test
	void filterNeighborhoodNotificationsWhenUnwantedEventTypesExistsForGranho() {
		// Prepare list of unwanted subtype
		setField(byggrFilterUtility, "unwantedSubtypes", List.of("GRASVA"));

		final var errands = List.of(ByggrErrandDto.builder()
			.withEvents(List.of(createEvent("GRANHO", "GRAUTS"), createEvent("GRANHO", "GRASVA")))
			.build());

		// Act and assert
		assertThat(byggrFilterUtility.filterNeighborhoodNotifications(errands, null)).isEmpty();
	}

	@Test
	void filterNeighborhoodNotificationsWhenUnwantedEventTypesExistsForKomfast() {
		// Prepare list of unwanted subtype
		setField(byggrFilterUtility, "unwantedSubtypes", List.of("GRASVA"));

		final var errands = List.of(ByggrErrandDto.builder()
			.withEvents(List.of(createEvent("KOMFAST", "KOMFASUTS"), createEvent("KOMFAST", "GRASVA")))
			.build());

		// Act and assert
		assertThat(byggrFilterUtility.filterNeighborhoodNotifications(errands, null)).isEmpty();
	}

	// with null list
	@Test
	void filterNeighborhoodNotificationsWithNullEvent() {
		final List<Event> events = new ArrayList<>();
		events.add(null);

		// Prepare list of unwanted subtype
		setField(byggrFilterUtility, "unwantedSubtypes", List.of("GRASVA"));

		final var errands = List.of(ByggrErrandDto.builder()
			.withEvents(events)
			.build());

		// Act and assert
		assertThat(byggrFilterUtility.filterNeighborhoodNotifications(errands, null)).isEmpty();
	}

	// unwated not ganho or komfast
	@Test
	void filterNeighborhoodNotificationsWithTypeNotWanted() {
		// Prepare list of unwanted subtype
		setField(byggrFilterUtility, "unwantedSubtypes", List.of("KOMFASVA"));

		final var errands = List.of(ByggrErrandDto.builder()
			.withEvents(List.of(createEvent("GRANHO", "GRAUTS"), createEvent("Unwanted", "KOMFASVA")))
			.build());

		// Act and assert
		assertThat(byggrFilterUtility.filterNeighborhoodNotifications(errands, null)).isEmpty();
	}

	@ParameterizedTest
	@MethodSource("filterErrandsForApplicantArgumentProvider")
	void filterErrandsForApplicant(final ByggrErrandDto errand, final int expetedErrandSize) {
		// Prepare list of applicant roles
		setField(byggrFilterUtility, "applicantRoles", List.of("SOK", "KPER"));

		// Act and assert
		assertThat(byggrFilterUtility.filterCasesForApplicant(List.of(errand), STAKEHOLDER_LEGAL_ID)).hasSize(expetedErrandSize);
	}

	@Test
	void filterEvents_errandIsNull() {
		final var identifier = "190102034567";

		final var result = byggrFilterUtility.filterEvents(identifier, null);

		assertThat(result).isNull();
	}

	private static Stream<Arguments> decisionEventArgumentProvider() {
		return Stream.of(
			Arguments.of(createDecisionEvent(1, "BESLUT", false, false), true),
			Arguments.of(createDecisionEvent(2, "beslut", false, false), true),
			Arguments.of(createDecisionEvent(3, "BESLUT", true, false), false),
			Arguments.of(createDecisionEvent(4, "BESLUT", false, true), false),
			Arguments.of(createDecisionEvent(5, "HANDLING", false, false), false),
			Arguments.of(createDecisionEvent(6, null, false, false), false));
	}

	private static Stream<Arguments> decisionDocumentArgumentProvider() {
		return Stream.of(
			Arguments.of(createDecisionDocument("BESLUT", "123", false), true),
			Arguments.of(createDecisionDocument("beslut", "123", false), true),
			Arguments.of(createDecisionDocument("BESLUT", "123", true), false),
			Arguments.of(createDecisionDocument("ANSS", "123", false), false),
			Arguments.of(createDecisionDocument(null, "123", false), false),
			Arguments.of(createDecisionDocument("BESLUT", " ", false), false),
			Arguments.of(createDecisionDocument("BESLUT", null, false), false),
			Arguments.of(new HandelseHandling().withTyp("BESLUT"), false));
	}

	private static Handelse createDecisionEvent(final int id, final String type, final boolean cancelled, final boolean secret) {
		return new Handelse()
			.withHandelseId(id)
			.withHandelsetyp(type)
			.withMakulerad(cancelled)
			.withSekretess(secret);
	}

	private static HandelseHandling createDecisionDocument(final String type, final String documentId, final boolean cancelled) {
		return new HandelseHandling()
			.withTyp(type)
			.withMakulerad(cancelled)
			.withDokument(new Dokument().withDokId(documentId));
	}

	@ParameterizedTest
	@MethodSource("decisionEventArgumentProvider")
	void filterDecisionEvents(final Handelse event, final boolean expectedToBeKept) {
		// Prepare list of decision event types
		setField(byggrFilterUtility, "decisionEventTypes", List.of("BESLUT"));

		final var errand = new Arende().withHandelseLista(new ArrayOfHandelse().withHandelse(event));

		// Act
		final var result = byggrFilterUtility.filterDecisionEvents(errand);

		// Assert
		if (expectedToBeKept) {
			assertThat(result).containsExactly(event);
		} else {
			assertThat(result).isEmpty();
		}
	}

	@Test
	void filterDecisionEventsWithNullEvent() {
		setField(byggrFilterUtility, "decisionEventTypes", List.of("BESLUT"));

		final List<Handelse> events = new ArrayList<>();
		events.add(null);
		final var errand = new Arende().withHandelseLista(new ArrayOfHandelse().withHandelse(events));

		assertThat(byggrFilterUtility.filterDecisionEvents(errand)).isEmpty();
	}

	@Test
	void filterDecisionEventsWhenNoDecisionEventTypesAreConfigured() {
		final var errand = new Arende().withHandelseLista(new ArrayOfHandelse().withHandelse(createDecisionEvent(1, "BESLUT", false, false)));

		assertThat(byggrFilterUtility.filterDecisionEvents(errand)).isEmpty();
	}

	@ParameterizedTest
	@NullSource
	@MethodSource("errandWithoutEventsProvider")
	void filterDecisionEventsWithoutEvents(final Arende errand) {
		setField(byggrFilterUtility, "decisionEventTypes", List.of("BESLUT"));

		assertThat(byggrFilterUtility.filterDecisionEvents(errand)).isEmpty();
	}

	private static Stream<Arguments> errandWithoutEventsProvider() {
		return Stream.of(
			Arguments.of(new Arende()),
			Arguments.of(new Arende().withHandelseLista(new ArrayOfHandelse())));
	}

	@ParameterizedTest
	@MethodSource("decisionDocumentArgumentProvider")
	void isDecisionDocument(final HandelseHandling document, final boolean expectedResult) {
		// Prepare list of decision document types
		setField(byggrFilterUtility, "decisionDocumentTypes", List.of("BESLUT"));

		// Act and assert
		assertThat(byggrFilterUtility.isDecisionDocument(document)).isEqualTo(expectedResult);
	}

	@Test
	void isDecisionDocumentWithNullDocument() {
		setField(byggrFilterUtility, "decisionDocumentTypes", List.of("BESLUT"));

		assertThat(byggrFilterUtility.isDecisionDocument(null)).isFalse();
	}

	@Test
	void isDecisionDocumentWhenNoDecisionDocumentTypesAreConfigured() {
		assertThat(byggrFilterUtility.isDecisionDocument(createDecisionDocument("BESLUT", "123", false))).isFalse();
	}
}
