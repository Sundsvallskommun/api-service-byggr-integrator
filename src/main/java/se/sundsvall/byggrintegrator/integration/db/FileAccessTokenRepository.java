package se.sundsvall.byggrintegrator.integration.db;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import se.sundsvall.byggrintegrator.integration.db.model.FileAccessTokenEntity;

@CircuitBreaker(name = "fileAccessTokenRepository")
public interface FileAccessTokenRepository extends JpaRepository<FileAccessTokenEntity, String> {

	Optional<FileAccessTokenEntity> findByIdAndFileIdAndMunicipalityId(String id, String fileId, String municipalityId);

	@Modifying
	void deleteByExpiresAtBefore(OffsetDateTime timestamp);
}
