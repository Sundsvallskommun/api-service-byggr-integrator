package se.sundsvall.byggrintegrator.service.util;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.byggrintegrator.service.util.LegalIdUtility.addHyphen;
import static se.sundsvall.byggrintegrator.service.util.LegalIdUtility.prefixOrgnbr;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class LegalIdUtilityTest {

	@ParameterizedTest
	@MethodSource("prefixArgumentProvider")
	void testPrefixOrgnbr(String value, String expected) {
		assertThat(prefixOrgnbr(value)).isEqualTo(expected);
	}

	private static Stream<Arguments> prefixArgumentProvider() {
		return Stream.of(
			Arguments.of(null, null),
			Arguments.of("", ""),
			Arguments.of("1", "1"),
			Arguments.of("12", "12"),
			Arguments.of("123", "123"),
			Arguments.of("1234", "1234"),
			Arguments.of("12345", "12345"),
			Arguments.of("123456", "123456"),
			Arguments.of("1234567", "1234567"),
			Arguments.of("12345678", "12345678"),
			Arguments.of("123456789", "123456789"),
			Arguments.of("1234567890", "161234567890"), // This is the only string that should be tampered with
			Arguments.of("12345678901", "12345678901"),
			Arguments.of("123456789012", "123456789012"),
			Arguments.of("1234567890123", "1234567890123"));
	}

	@Test
	void testAddHyphenOnNull() {
		assertThat(LegalIdUtility.addHyphen(null)).isNull();
	}

	@Test
	void testAddHyphenOnEmptyString() {
		assertThat(addHyphen("")).isEmpty();
	}

	@Test
	void testAddHyphenOnStringWithHyphen() {
		assertThat(addHyphen("12345-67890")).isEqualTo("12345-67890");
	}

	@ParameterizedTest
	@MethodSource("hyphensArgumentProvider")
	void testAddHyphenOnStrings(String value, String expected) {
		assertThat(addHyphen(value)).isEqualTo(expected);
	}

	private static Stream<Arguments> hyphensArgumentProvider() {
		return Stream.of(
			Arguments.of("1", "1"),
			Arguments.of("12", "12"),
			Arguments.of("123", "123"),
			Arguments.of("1234", "1234"),
			Arguments.of("12345", "1-2345"),
			Arguments.of("123456", "12-3456"),
			Arguments.of("1234567", "123-4567"),
			Arguments.of("12345678", "1234-5678"),
			Arguments.of("123456789", "12345-6789"),
			Arguments.of("1234567890", "123456-7890"),
			Arguments.of("12345678901", "1234567-8901"),
			Arguments.of("123456789012", "12345678-9012"),
			Arguments.of("1234567890123", "123456789-0123"));
	}
}
