package se.sundsvall.byggrintegrator.service;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReferralRoleEnumTest {

	@Test
	void testApiNumberValues() {
		assertThat(Stream.of(ReferralRoleEnum.values())
			.map(ReferralRoleEnum::getApiNumber)
			.toList()).containsExactlyInAnyOrder(1, 2);
	}

	@Test
	void testByggrRoleValues() {
		assertThat(Stream.of(ReferralRoleEnum.values())
			.map(ReferralRoleEnum::getByggrRole)
			.toList()).containsExactlyInAnyOrder("GRAN", "FAG");
	}

	@Test
	void testTranslations() {
		assertThat(ReferralRoleEnum.translate("GRAN")).isEqualTo("1");
		assertThat(ReferralRoleEnum.translate("FAG")).isEqualTo("2");
		assertThat(ReferralRoleEnum.translate("gran")).isEqualTo("1");
		assertThat(ReferralRoleEnum.translate("fag")).isEqualTo("2");
		assertThat(ReferralRoleEnum.translate("OTHER")).isEqualTo("0");
		assertThat(ReferralRoleEnum.translate("")).isEqualTo("0");
		assertThat(ReferralRoleEnum.translate(null)).isEqualTo("0");
	}
}
