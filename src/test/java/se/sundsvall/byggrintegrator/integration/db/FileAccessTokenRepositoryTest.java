package se.sundsvall.byggrintegrator.integration.db;

import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;
import se.sundsvall.byggrintegrator.integration.db.model.FileAccessTokenEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
@ActiveProfiles("junit")
class FileAccessTokenRepositoryTest {

	@Autowired
	private FileAccessTokenRepository repository;

	@Test
	void testFindByIdAndFileIdAndMunicipalityId() {
		final var entity = FileAccessTokenEntity.create()
			.withFileId("12345")
			.withMunicipalityId("2281")
			.withExpiresAt(OffsetDateTime.now().plusHours(24));

		final var saved = repository.save(entity);

		final var result = repository.findByIdAndFileIdAndMunicipalityId(saved.getId(), "12345", "2281");
		assertThat(result).isPresent();
		assertThat(result.get().getFileId()).isEqualTo("12345");
		assertThat(result.get().getMunicipalityId()).isEqualTo("2281");
	}

	@Test
	void testFindByIdAndFileIdAndMunicipalityId_notFound() {
		final var result = repository.findByIdAndFileIdAndMunicipalityId("nonexistent", "12345", "2281");
		assertThat(result).isEmpty();
	}

	@Test
	void testFindByIdAndFileIdAndMunicipalityId_wrongFileId() {
		final var entity = FileAccessTokenEntity.create()
			.withFileId("12345")
			.withMunicipalityId("2281")
			.withExpiresAt(OffsetDateTime.now().plusHours(24));

		final var saved = repository.save(entity);

		final var result = repository.findByIdAndFileIdAndMunicipalityId(saved.getId(), "wrong", "2281");
		assertThat(result).isEmpty();
	}

	@Test
	void testDeleteByExpiresAtBefore() {
		final var expiredEntity = FileAccessTokenEntity.create()
			.withFileId("expired")
			.withMunicipalityId("2281")
			.withExpiresAt(OffsetDateTime.now().minusHours(1));

		final var validEntity = FileAccessTokenEntity.create()
			.withFileId("valid")
			.withMunicipalityId("2281")
			.withExpiresAt(OffsetDateTime.now().plusHours(24));

		repository.save(expiredEntity);
		repository.save(validEntity);

		assertThat(repository.findAll()).hasSize(2);

		repository.deleteByExpiresAtBefore(OffsetDateTime.now());

		final var remaining = repository.findAll();
		assertThat(remaining).hasSize(1);
		assertThat(remaining.getFirst().getFileId()).isEqualTo("valid");
	}
}
