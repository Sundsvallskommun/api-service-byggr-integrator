package se.sundsvall.byggrintegrator.service;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.byggrintegrator.TestObjectFactory.generateNeighborNotificationsDtos;

import java.util.List;

import org.junit.jupiter.api.Test;

import se.sundsvall.byggrintegrator.api.model.KeyValue;

class ApiResponseMapperTest {

	private final ApiResponseMapper apiResponseMapper = new ApiResponseMapper();

	@Test
	void testMapNeighborNotificationsDtosToKeyValueList() {
		// Arrange
		var errands = generateNeighborNotificationsDtos();

		// Act
		var keyValues = apiResponseMapper.mapToKeyValueResponseList(errands);

		// Assert
		assertThat(keyValues).hasSize(4);
		assertThat(keyValues).extracting(KeyValue::key).containsExactly("dnr123", "dnr123", "dnr456", "dnr456");
		assertThat(keyValues).extracting(KeyValue::value).containsExactly("dnr123, des-1 type1", "dnr123, des-2 type2", "dnr456, des-3 type3", "dnr456, des-4 type4");
	}

	@Test
	void testMapNeighborNotificationsDtosToKeyValueListEmpty() {
		// Act
		var keyValues = apiResponseMapper.mapToKeyValueResponseList(List.of());

		//Assert
		assertThat(keyValues).isNotNull().isEmpty();
	}
}
