package se.sundsvall.byggrintegrator.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

class CaseTypeEnumTest {

	@Test
	void testApiNumberValues() {
		assertThat(Stream.of(CaseTypeEnum.values())
			.map(CaseTypeEnum::getApiNumber)
			.toList()).containsExactlyInAnyOrder(11, 12, 13, 14, 15, 16, 21);
	}

	@Test
	void testByggRTypeValues() {
		assertThat(Stream.of(CaseTypeEnum.values())
			.map(CaseTypeEnum::getByggrType)
			.toList()).containsExactlyInAnyOrder("BL", "RL", "MARK", "FÖRF", "ATTANM", "ANM", "DI");
	}

	@Test
	void testByggRTranslations() {
		assertThat(CaseTypeEnum.translate("BL")).isEqualTo("11");
		assertThat(CaseTypeEnum.translate("RL")).isEqualTo("12");
		assertThat(CaseTypeEnum.translate("MARK")).isEqualTo("13");
		assertThat(CaseTypeEnum.translate("FÖRF")).isEqualTo("14");
		assertThat(CaseTypeEnum.translate("ATTANM")).isEqualTo("15");
		assertThat(CaseTypeEnum.translate("ANM")).isEqualTo("16");
		assertThat(CaseTypeEnum.translate("DI")).isEqualTo("21");
		assertThat(CaseTypeEnum.translate("UNKNOWN")).isEqualTo("999");
	}
}
