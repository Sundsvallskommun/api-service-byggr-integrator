package se.sundsvall.byggrintegrator.service.util;

import java.time.LocalDate;
import java.time.Month;
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
			localDateMock.when(LocalDate::now).thenReturn(FIXED_TODAY);

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

	@ParameterizedTest
	@MethodSource("filterErrandsForApplicantArgumentProvider")
	void filterErrandsForApplicant(final ByggrErrandDto errand, final int expetedErrandSize) {
		// Prepare list of applicant roles
		setField(byggrFilterUtility, "applicantRoles", List.of("SOK", "KPER"));

		// Act and assert
		assertThat(byggrFilterUtility.filterCasesForApplicant(List.of(errand), STAKEHOLDER_LEGAL_ID)).hasSize(expetedErrandSize);
	}
}
