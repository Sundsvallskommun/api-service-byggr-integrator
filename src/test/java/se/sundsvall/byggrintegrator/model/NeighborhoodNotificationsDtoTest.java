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

import org.junit.jupiter.api.Test;

class NeighborhoodNotificationsDtoTest {

	@Test
	void testBean() {
		assertThat(NeighborhoodNotificationsDto.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testInnerClassBean() {
		assertThat(NeighborhoodNotificationsDto.PropertyDesignation.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {
		var dnr = "123";
		var designation = "designation";
		var property = "property";

		var propertyDesignation = NeighborhoodNotificationsDto.PropertyDesignation.builder()
			.withDesignation(designation)
			.withProperty(property)
			.build();

		var dto = NeighborhoodNotificationsDto.builder()
			.withByggrErrandNumber(dnr)
			.withPropertyDesignation(List.of(propertyDesignation))
			.build();

		assertThat(propertyDesignation.getDesignation()).isEqualTo(designation);
		assertThat(propertyDesignation.getProperty()).isEqualTo(property);
		assertThat(dto).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(dto.getByggrErrandNumber()).isEqualTo(dnr);
		assertThat(dto.getPropertyDesignation()).containsExactly(propertyDesignation);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(NeighborhoodNotificationsDto.builder().build()).isNotNull().hasAllNullFieldsOrProperties();
	}

	@Test
	void testNoDirtOnCreatedInnerBean() {
		assertThat(NeighborhoodNotificationsDto.PropertyDesignation.builder().build()).isNotNull().hasAllNullFieldsOrProperties();
	}
}
