package se.sundsvall.byggrintegrator.service;

import generated.se.sundsvall.arendeexport.v4.ArrayOfString2;
import generated.se.sundsvall.arendeexport.v4.HandelseIntressent;
import generated.se.sundsvall.arendeexport.v4.Remiss;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import se.sundsvall.byggrintegrator.api.model.KeyValue;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.byggrintegrator.TestObjectFactory.generateArendeResponse;
import static se.sundsvall.byggrintegrator.TestObjectFactory.generateByggrErrandDtos;

class ApiResponseMapperTest {

	private final ApiResponseMapper apiResponseMapper = new ApiResponseMapper();

	@Test
	void testMapNeighborNotificationsDtosToKeyValueList() {
		// Arrange
		final var errands = generateByggrErrandDtos();

		// Act
		final var keyValues = apiResponseMapper.mapToKeyValueResponseList(errands);

		// Assert
		assertThat(keyValues).hasSize(2).satisfiesExactlyInAnyOrder(keyVal -> {
			assertThat(keyVal.key()).isEqualTo("1");
			assertThat(keyVal.value()).isEqualTo("dnr123");
		}, keyVal -> {
			assertThat(keyVal.key()).isEqualTo("2");
			assertThat(keyVal.value()).isEqualTo("dnr456");
		});
	}

	@Test
	void testMapNeighborNotificationsDtosToKeyValueListEmpty() {
		// Act
		final var keyValues = apiResponseMapper.mapToKeyValueResponseList(List.of());

		// Assert
		assertThat(keyValues).isNotNull().isEmpty();
	}

	@Test
	void testMapToWeight() throws Exception {
		// Act
		final var response = apiResponseMapper.mapToWeight(generateArendeResponse("dnr"));

		assertThat(response.getValue()).isEqualTo("11"); // BL translated to integer value according to the CaseTypeEnum
	}

	@Test
	void testMapToWeight_remissNeighbour() {
		final var remiss = new Remiss().withMottagare(new HandelseIntressent()
			.withRollLista(new ArrayOfString2().withRoll("GRAN")));

		final var response = apiResponseMapper.mapToWeight(remiss);

		assertThat(response.getValue()).isEqualTo("1");
	}

	@Test
	void testMapToWeight_remissPropertyOwner() {
		final var remiss = new Remiss().withMottagare(new HandelseIntressent()
			.withRollLista(new ArrayOfString2().withRoll("FAG")));

		final var response = apiResponseMapper.mapToWeight(remiss);

		assertThat(response.getValue()).isEqualTo("2");
	}

	@Test
	void testMapToWeight_remissPicksGranOverNoise() {
		final var remiss = new Remiss().withMottagare(new HandelseIntressent()
			.withRollLista(new ArrayOfString2().withRoll("SAMF", "GRAN")));

		final var response = apiResponseMapper.mapToWeight(remiss);

		assertThat(response.getValue()).isEqualTo("1");
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"KPER", "SAMF", "OTHER"
	})
	void testMapToWeight_remissUnknownRoles(final String role) {
		final var remiss = new Remiss().withMottagare(new HandelseIntressent()
			.withRollLista(new ArrayOfString2().withRoll(role)));

		final var response = apiResponseMapper.mapToWeight(remiss);

		assertThat(response.getValue()).isEqualTo("0");
	}

	@Test
	void testMapToWeight_remissEmptyRollLista() {
		final var remiss = new Remiss().withMottagare(new HandelseIntressent()
			.withRollLista(new ArrayOfString2()));

		final var response = apiResponseMapper.mapToWeight(remiss);

		assertThat(response.getValue()).isEqualTo("0");
	}

	@Test
	void testMapToWeight_remissNullMottagare() {
		final var remiss = new Remiss().withMottagare(null);

		final var response = apiResponseMapper.mapToWeight(remiss);

		assertThat(response.getValue()).isEqualTo("0");
	}

	@Test
	void mapStringIntegerMapToKeyValue() {
		final Map<String, Map<Integer, String>> myMap = Map.of("key1", Map.of(1, "GRAN"), "key2", Map.of(2, "FAG"));

		final List<KeyValue> keyValues = apiResponseMapper.mapToKeyValue(myMap);

		assertThat(keyValues).hasSize(2).satisfiesExactlyInAnyOrder(
			keyVal -> assertThat(keyVal.value()).isEqualTo("key1 – Lämna svar som granne [1]"),
			keyVal -> assertThat(keyVal.value()).isEqualTo("key2 – Lämna svar som fastighetsägare [2]"));
	}

	@Test
	void mapStringIntegerMapToKeyValueFiltersUnknownRoles() {
		final Map<String, Map<Integer, String>> myMap = Map.of("key1", Map.of(1, "GRAN"), "key2", Map.of(2, "OTHER"));

		final List<KeyValue> keyValues = apiResponseMapper.mapToKeyValue(myMap);

		assertThat(keyValues).hasSize(1).satisfiesExactly(
			keyVal -> assertThat(keyVal.value()).isEqualTo("key1 – Lämna svar som granne [1]"));
	}
}
