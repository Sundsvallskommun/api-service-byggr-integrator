package se.sundsvall.byggrintegrator.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;

class ParameterUtilityTest {
	private static final String ERROR_NON_INTERPRETABLE_VALUE = "Bad Request: Parameter caseNumberAndEventId with value '%s' is not interpretable. Valid format is '<diarynumber> [<numeric eventId>]', for example 'BYGG 2024-000001 [123456]'";

	@ParameterizedTest
	@MethodSource("parseDiaryNumberArgumentProvider")
	void parseDiaryNumber(String input, String expectedResult) {
		assertThat(ParameterUtility.parseDiaryNumber(input)).isEqualTo(expectedResult);
	}

	@ParameterizedTest
	@MethodSource("parseFaultyInputArgumentProvider")
	void parseFaultyDiaryNumber(String input) {
		final var e = assertThrows(ThrowableProblem.class, () -> ParameterUtility.parseDiaryNumber(input));
		assertThat(e.getMessage()).isEqualTo(ERROR_NON_INTERPRETABLE_VALUE.formatted(input));
	}

	@ParameterizedTest
	@MethodSource("parseEventIdArgumentProvider")
	void parseEventId(String input, int expectedResult) {
		assertThat(ParameterUtility.parseEventId(input)).isEqualTo(expectedResult);
	}

	@ParameterizedTest
	@MethodSource("parseFaultyInputArgumentProvider")
	void parseFaultyEventId(String input) {
		final var e = assertThrows(ThrowableProblem.class, () -> ParameterUtility.parseEventId(input));
		assertThat(e.getStatus()).isEqualTo(Status.BAD_REQUEST);
		assertThat(e.getMessage()).isEqualTo(ERROR_NON_INTERPRETABLE_VALUE.formatted(input));
	}

	@Test
	void parseNonNumericEventId() {
		final var input = "DIARYNBR [ABC]";
		final var e = assertThrows(ThrowableProblem.class, () -> ParameterUtility.parseEventId(input));
		assertThat(e.getStatus()).isEqualTo(Status.BAD_REQUEST);
		assertThat(e.getMessage()).isEqualTo(ERROR_NON_INTERPRETABLE_VALUE.formatted(input));
	}

	private static Stream<Arguments> parseDiaryNumberArgumentProvider() {
		return Stream.of(
			Arguments.of("DIARYNBR [123]", "DIARYNBR"),
			Arguments.of("DIARY NR [123]", "DIARY NR"),
			Arguments.of("   DIARY NR    [   123   ]   ", "DIARY NR"),
			Arguments.of("   diary    nr    [   123   ]   ", "diary    nr"));
	}

	private static Stream<Arguments> parseEventIdArgumentProvider() {
		return Stream.of(
			Arguments.of("DIARYNBR [123]", 123),
			Arguments.of("DIARY NR [123]", 123),
			Arguments.of("   DIARY NR    [   123   ]   ", 123),
			Arguments.of("   diary    nr    [   123   ]   ", 123));
	}

	private static Stream<Arguments> parseFaultyInputArgumentProvider() {
		return Stream.of(
			Arguments.of(""),
			Arguments.of(" "),
			Arguments.of("[]"),
			Arguments.of("DIARYNBR"),
			Arguments.of("X[123]"),
			Arguments.of("[123]"),
			Arguments.of("[DIARYNBR  123]"));
	}
}
