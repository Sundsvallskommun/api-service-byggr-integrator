package se.sundsvall.byggrintegrator.integration.db.model;

import java.time.OffsetDateTime;
import java.util.Random;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static com.google.code.beanmatchers.BeanMatchers.registerValueGenerator;
import static java.time.OffsetDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.MatcherAssert.assertThat;

class FileAccessTokenEntityTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> now().plusDays(new Random().nextInt()), OffsetDateTime.class);
	}

	@Test
	void testBean() {
		assertThat(FileAccessTokenEntity.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {
		final var id = "id";
		final var fileId = "fileId";
		final var municipalityId = "2281";
		final var expiresAt = now().plusHours(24);
		final var created = now();

		final var entity = FileAccessTokenEntity.create()
			.withId(id)
			.withFileId(fileId)
			.withMunicipalityId(municipalityId)
			.withExpiresAt(expiresAt)
			.withCreated(created);

		assertThat(entity.getId()).isEqualTo(id);
		assertThat(entity.getFileId()).isEqualTo(fileId);
		assertThat(entity.getMunicipalityId()).isEqualTo(municipalityId);
		assertThat(entity.getExpiresAt()).isEqualTo(expiresAt);
		assertThat(entity.getCreated()).isEqualTo(created);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(FileAccessTokenEntity.create()).hasAllNullFieldsOrProperties();
		assertThat(new FileAccessTokenEntity()).hasAllNullFieldsOrProperties();
	}
}
