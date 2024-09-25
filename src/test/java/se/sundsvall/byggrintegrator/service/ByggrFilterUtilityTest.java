package se.sundsvall.byggrintegrator.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import se.sundsvall.byggrintegrator.model.ByggrErrandDto;
import se.sundsvall.byggrintegrator.model.ByggrErrandDto.Event;
import se.sundsvall.byggrintegrator.model.ByggrErrandDto.Stakeholder;

@ExtendWith(MockitoExtension.class)
class ByggrFilterUtilityTest {
	private static final String STAKEHOLDER_LEGAL_ID = "stakeholderLegalId";

	@Mock
	private ByggrFilterProperties byggrFilterPropertiesMock;

	@InjectMocks
	private ByggrFilterUtility byggrFilterUtility;

	@ParameterizedTest
	@MethodSource("validEventArgumentProvider")
	void hasValidEvent(Event event, boolean expectedResult) {
		// Act and assert
		assertThat(ByggrFilterUtility.hasValidEvent(event)).isEqualTo(expectedResult);
	}

	private static Stream<Arguments> validEventArgumentProvider() {
		return Stream.of(
			Arguments.of(null, false),
			Arguments.of(createEvent(null, null), false),
			Arguments.of(createEvent("GRANHO", null), false),
			Arguments.of(createEvent(null, "GRAUTS"), false),
			Arguments.of(createEvent("type", "subtype"), false),
			Arguments.of(createEvent("granho", "grauts"), true),
			Arguments.of(createEvent("GRANHO", "GRAUTS"), true));
	}

	@ParameterizedTest
	@MethodSource("filterNeighborhoodNotificationsArgumentProvider")
	void filterNeighborhoodNotifications(List<Event> events, String identifier, int expectedErrandsSize, int expectedEventsSize) {
		// Act
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

	private static Stream<Arguments> filterNeighborhoodNotificationsArgumentProvider() {
		return Stream.of(
			Arguments.of(List.of(createEvent(null, "GRAUTS", STAKEHOLDER_LEGAL_ID, LocalDate.now())), STAKEHOLDER_LEGAL_ID, 0, 0),
			Arguments.of(List.of(createEvent("GRANHO", null, STAKEHOLDER_LEGAL_ID, LocalDate.now())), STAKEHOLDER_LEGAL_ID, 0, 0),
			Arguments.of(List.of(createEvent("GRANHO", "GRAUTS", STAKEHOLDER_LEGAL_ID, LocalDate.now())), "otherId", 0, 0),
			Arguments.of(List.of(createEvent("type", "subtype", STAKEHOLDER_LEGAL_ID, LocalDate.now())), STAKEHOLDER_LEGAL_ID, 0, 0),
			Arguments.of(List.of(createEvent("GRANHO", "GRAUTS", STAKEHOLDER_LEGAL_ID, LocalDate.now().minusDays(31))), STAKEHOLDER_LEGAL_ID, 0, 0),
			Arguments.of(List.of(createEvent("GRANHO", "GRAUTS", STAKEHOLDER_LEGAL_ID, LocalDate.now().minusDays(30))), STAKEHOLDER_LEGAL_ID, 1, 1),
			Arguments.of(List.of(createEvent("granho", "grauts", STAKEHOLDER_LEGAL_ID, LocalDate.now())), STAKEHOLDER_LEGAL_ID, 1, 1),
			Arguments.of(List.of(
				createEvent("granho", "grauts", STAKEHOLDER_LEGAL_ID, LocalDate.now()),
				createEvent("granho", "grauts", STAKEHOLDER_LEGAL_ID, LocalDate.now())), STAKEHOLDER_LEGAL_ID, 1, 2));
	}

	@Test
	void filterNeighborhoodNotificationsWhenUnwantedEventTypesExists() {
		// Prepare list of unwanted subtype
		setField(byggrFilterUtility, "unwantedSubtypes", List.of("GRASVA"));

		final var errands = List.of(ByggrErrandDto.builder()
			.withEvents(List.of(createEvent("GRANHO", "GRAUTS"), createEvent("GRANHO", "GRASVA")))
			.build());

		// Act and assert
		assertThat(byggrFilterUtility.filterNeighborhoodNotifications(errands, null)).isEmpty();

	}

	@ParameterizedTest
	@MethodSource("filterErrandsForApplicantArgumentProvider")
	void filterErrandsForApplicant(ByggrErrandDto errand, int expetedErrandSize) {
		// Prepare list of applicant roles
		setField(byggrFilterUtility, "applicantRoles", List.of("SOK", "KPER"));

		// Act and assert
		assertThat(byggrFilterUtility.filterCasesForApplicant(List.of(errand), STAKEHOLDER_LEGAL_ID)).hasSize(expetedErrandSize);
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

	@ParameterizedTest
	@MethodSource("filterEventArgumentProvider")
	void filterEvent(ByggrErrandDto input, ByggrErrandDto result) {
		final var eventId = 123;
		assertThat(byggrFilterUtility.filterEvent(input, eventId)).isEqualTo(result);
	}

	private static Stream<Arguments> filterEventArgumentProvider() {
		return Stream.of(
			Arguments.of(null, null),
			Arguments.of(
				ByggrErrandDto.builder().withEvents(List.of(Event.builder().withId(123).build(), Event.builder().withId(234).build())).build(),
				ByggrErrandDto.builder().withEvents(List.of(Event.builder().withId(123).build())).build()),
			Arguments.of(
				ByggrErrandDto.builder().withEvents(List.of(Event.builder().withId(234).build())).build(),
				ByggrErrandDto.builder().withEvents(Collections.emptyList()).build()),
			Arguments.of(
				ByggrErrandDto.builder().withEvents(List.of(Event.builder().withId(123).build())).build(),
				ByggrErrandDto.builder().withEvents(List.of(Event.builder().withId(123).build())).build()));
	}

	private static ByggrErrandDto createErrand(List<Stakeholder> stakeholders) {
		return ByggrErrandDto.builder()
			.withStakeholders(stakeholders)
			.build();
	}

	private static Event createEvent(String type, String subtype) {
		return Event.builder()
			.withEventType(type)
			.withEventSubtype(subtype)
			.build();
	}

	private static Event createEvent(String type, String subtype, String stakeholderLegalId, LocalDate date) {
		return Event.builder()
			.withEventType(type)
			.withEventSubtype(subtype)
			.withEventDate(date)
			.withStakeholders(List.of(Stakeholder.builder()
				.withLegalId(stakeholderLegalId)
				.build()))
			.build();
	}

}
