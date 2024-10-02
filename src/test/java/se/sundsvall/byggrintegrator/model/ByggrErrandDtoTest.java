
package se.sundsvall.byggrintegrator.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static com.google.code.beanmatchers.BeanMatchers.registerValueGenerator;
import static java.time.LocalDate.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import se.sundsvall.byggrintegrator.model.ByggrErrandDto.Event;
import se.sundsvall.byggrintegrator.model.ByggrErrandDto.Stakeholder;

class ByggrErrandDtoTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> now().plusDays(new Random().nextInt()), LocalDate.class);
	}

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
	void testInnerClassBeans() {
		assertThat(ByggrErrandDto.Event.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));

		assertThat(ByggrErrandDto.Stakeholder.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {
		final var dnr = "123";
		final var events = List.of(
			Event.builder().withId(123).build(),
			Event.builder().withId(456).build());

		final var stakeholders = List.of(
			Stakeholder.builder().withLegalId("123456").build(),
			Stakeholder.builder().withLegalId("654321").build());

		final var dto = ByggrErrandDto.builder()
			.withByggrCaseNumber(dnr)
			.withEvents(events)
			.withStakeholders(stakeholders)
			.build();

		assertThat(dto).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(dto.getByggrCaseNumber()).isEqualTo(dnr);
		assertThat(dto.getEvents()).isEqualTo(events);
		assertThat(dto.getStakeholders()).isEqualTo(stakeholders);
	}

	@Test
	void testBuilderMethodsOnInnerStakeholderBean() {
		final var legalId = "legalId";
		final var roles = List.of("role1", "role2");
		final var dto = Stakeholder.builder()
			.withLegalId(legalId)
			.withRoles(roles)
			.build();

		assertThat(dto).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(dto.getLegalId()).isEqualTo(legalId);
		assertThat(dto.getRoles()).isEqualTo(roles);
	}

	@Test
	void testBuilderMethodsOnInnerEventBean() {
		final var date = LocalDate.now();
		final var subtype = "subtype";
		final var type = "type";
		final var files = Map.of("file1", "file1.txt");
		final var id = 123;
		final var stakeholders = List.of(Stakeholder.builder().build());
		final var heading = "heading";

		final var dto = Event.builder()
			.withEventDate(date)
			.withEventSubtype(subtype)
			.withEventType(type)
			.withFiles(files)
			.withId(id)
			.withStakeholders(stakeholders)
			.withHeading(heading)
			.build();

		assertThat(dto).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(dto.getEventDate()).isEqualTo(date);
		assertThat(dto.getEventSubtype()).isEqualTo(subtype);
		assertThat(dto.getEventType()).isEqualTo(type);
		assertThat(dto.getFiles()).isEqualTo(files);
		assertThat(dto.getId()).isEqualTo(id);
		assertThat(dto.getStakeholders()).isEqualTo(stakeholders);
		assertThat(dto.getHeading()).isEqualTo(heading);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(ByggrErrandDto.builder().build()).isNotNull().hasAllNullFieldsOrProperties();
	}

	@Test
	void testNoDirtOnCreatedInnerBeans() {
		assertThat(ByggrErrandDto.Event.builder().build()).isNotNull().hasAllNullFieldsOrPropertiesExcept("id").hasFieldOrPropertyWithValue("id", 0);
		assertThat(ByggrErrandDto.Stakeholder.builder().build()).isNotNull().hasAllNullFieldsOrProperties();
	}
}
