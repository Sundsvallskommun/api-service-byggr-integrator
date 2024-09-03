package se.sundsvall.byggrintegrator.service;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.byggrintegrator.service.LegalIdUtility.addHyphen;

import org.junit.jupiter.api.Test;

class LegalIdUtilityTest {

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

	@Test
	void testAddHyphenOnStringsWithDifferentLength() {
		assertThat(addHyphen("1")).isEqualTo("1");
		assertThat(addHyphen("12")).isEqualTo("12");
		assertThat(addHyphen("123")).isEqualTo("123");
		assertThat(addHyphen("1234")).isEqualTo("1234");
		assertThat(addHyphen("12345")).isEqualTo("1-2345");
		assertThat(addHyphen("123456")).isEqualTo("12-3456");
		assertThat(addHyphen("1234567")).isEqualTo("123-4567");
		assertThat(addHyphen("12345678")).isEqualTo("1234-5678");
		assertThat(addHyphen("123456789")).isEqualTo("12345-6789");
		assertThat(addHyphen("1234567890")).isEqualTo("123456-7890");
		assertThat(addHyphen("12345678901")).isEqualTo("1234567-8901");
		assertThat(addHyphen("123456789012")).isEqualTo("12345678-9012");
		assertThat(addHyphen("1234567890123")).isEqualTo("123456789-0123");
	}
}
