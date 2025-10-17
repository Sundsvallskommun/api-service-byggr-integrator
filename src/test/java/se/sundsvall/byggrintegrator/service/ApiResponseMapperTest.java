package se.sundsvall.byggrintegrator.service;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.byggrintegrator.TestObjectFactory.generateArendeResponse;
import static se.sundsvall.byggrintegrator.TestObjectFactory.generateByggrErrandDtos;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import se.sundsvall.byggrintegrator.api.model.KeyValue;

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
	void mapStringIntegerMapToKeyValue() {
		final Map<String, Map<Integer, String>> myMap = Map.of("key1", Map.of(1, "1234"), "key2", Map.of(2, ""));

		final List<KeyValue> keyValues = apiResponseMapper.mapToKeyValue(myMap);

		assertThat(keyValues).hasSize(2).satisfiesExactlyInAnyOrder(
			keyVal -> assertThat(keyVal.value()).isEqualTo("key1 - besvarad 1234 [1]"),
			keyVal -> assertThat(keyVal.value()).isEqualTo("key2 - ej besvarad [2]"));
	}
}
