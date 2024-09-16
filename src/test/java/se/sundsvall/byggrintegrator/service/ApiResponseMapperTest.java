package se.sundsvall.byggrintegrator.service;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.byggrintegrator.TestObjectFactory.generateArendeResponse;
import static se.sundsvall.byggrintegrator.TestObjectFactory.generateByggrErrandDtos;

import java.util.List;

import org.junit.jupiter.api.Test;

import se.sundsvall.byggrintegrator.TestObjectFactory;

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
	void testMapToWeight() {
		// Act
		final var response = apiResponseMapper.mapToWeight(generateArendeResponse("dnr"));

		assertThat(response.getValue()).isEqualTo(TestObjectFactory.ARENDE_TYP_LH);
	}
}
