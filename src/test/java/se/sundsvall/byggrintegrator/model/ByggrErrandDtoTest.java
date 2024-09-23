package se.sundsvall.byggrintegrator.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class ByggrErrandDtoTest {

	@Test
	void testBean() {
		assertThat(ByggrErrandDto.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testInnerClassBean() {
		assertThat(ByggrErrandDto.PropertyDesignation.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {
		final var designation = "designation";
		final var dnr = "123";
		final var files = Map.of("file1", "file1.txt");
		final var neighborhoodEventIds = List.of(123, 456);
		final var property = "property";

		final var propertyDesignation = ByggrErrandDto.PropertyDesignation.builder()
			.withDesignation(designation)
			.withProperty(property)
			.build();

		final var dto = ByggrErrandDto.builder()
			.withByggrCaseNumber(dnr)
			.withFiles(files)
			.withNeighborhoodEventIds(neighborhoodEventIds)
			.withPropertyDesignation(List.of(propertyDesignation))
			.build();

		assertThat(propertyDesignation.getDesignation()).isEqualTo(designation);
		assertThat(propertyDesignation.getProperty()).isEqualTo(property);
		assertThat(dto).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(dto.getFiles()).isEqualTo(files);
		assertThat(dto.getByggrCaseNumber()).isEqualTo(dnr);
		assertThat(dto.getPropertyDesignation()).containsExactly(propertyDesignation);
		assertThat(dto.getNeighborhoodEventIds()).isEqualTo(neighborhoodEventIds);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(ByggrErrandDto.builder().build()).isNotNull().hasAllNullFieldsOrProperties();
	}

	@Test
	void testNoDirtOnCreatedInnerBean() {
		assertThat(ByggrErrandDto.PropertyDesignation.builder().build()).isNotNull().hasAllNullFieldsOrProperties();
	}
}
