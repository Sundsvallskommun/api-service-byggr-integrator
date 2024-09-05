package se.sundsvall.byggrintegrator.service;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.byggrintegrator.TestObjectFactory.generateArendeResponse;
import static se.sundsvall.byggrintegrator.TestObjectFactory.generateByggrErrandDtos;

import java.util.List;

import org.junit.jupiter.api.Test;

import se.sundsvall.byggrintegrator.TestObjectFactory;
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
		assertThat(keyValues).hasSize(4);
		assertThat(keyValues).extracting(KeyValue::key).containsExactlyInAnyOrder("dnr123", "dnr123", "dnr456", "dnr456");
		assertThat(keyValues).extracting(KeyValue::value).containsExactlyInAnyOrder("dnr123, des-1 type1", "dnr123, des-2 type2", "dnr456, des-3 type3", "dnr456, des-4 type4");
	}

	@Test
	void testMapNeighborNotificationsDtosToKeyValueListEmpty() {
		// Act
		final var keyValues = apiResponseMapper.mapToKeyValueResponseList(List.of());

		// Assert
		assertThat(keyValues).isNotNull().isEmpty();
	}

	@Test
	void testMapToWeight() {
		// Act
		final var response = apiResponseMapper.mapToWeight(generateArendeResponse("dnr"));

		assertThat(response.getValue()).isEqualTo(TestObjectFactory.ARENDE_TYP_LH);
	}
}
