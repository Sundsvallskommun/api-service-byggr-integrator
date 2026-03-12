package se.sundsvall.byggrintegrator.service;

import java.time.OffsetDateTime;
import org.springframework.stereotype.Service;
import se.sundsvall.byggrintegrator.configuration.FileAccessTokenProperties;
import se.sundsvall.byggrintegrator.integration.db.FileAccessTokenRepository;
import se.sundsvall.byggrintegrator.integration.db.model.FileAccessTokenEntity;
import se.sundsvall.dept44.problem.Problem;

import static java.util.Optional.ofNullable;
import static org.springframework.http.HttpStatus.GONE;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class FileAccessTokenService {

	static final String ERROR_TOKEN_NOT_FOUND = "File access token was not found";
	static final String ERROR_TOKEN_EXPIRED = "File access token has expired";

	private final FileAccessTokenRepository repository;
	private final FileAccessTokenProperties properties;

	public FileAccessTokenService(final FileAccessTokenRepository repository, final FileAccessTokenProperties properties) {
		this.repository = repository;
		this.properties = properties;
	}

	public String createToken(final String municipalityId, final String fileId) {
		final var entity = FileAccessTokenEntity.create()
			.withMunicipalityId(municipalityId)
			.withFileId(fileId)
			.withExpiresAt(OffsetDateTime.now().plus(properties.expiration()));

		return repository.save(entity).getId();
	}

	public void validateToken(final String municipalityId, final String fileId, final String token) {
		final var entity = ofNullable(token)
			.flatMap(t -> repository.findByIdAndFileIdAndMunicipalityId(t, fileId, municipalityId))
			.orElseThrow(() -> Problem.builder()
				.withStatus(NOT_FOUND)
				.withTitle(NOT_FOUND.getReasonPhrase())
				.withDetail(ERROR_TOKEN_NOT_FOUND)
				.build());

		if (entity.getExpiresAt().isBefore(OffsetDateTime.now())) {
			throw Problem.builder()
				.withStatus(GONE)
				.withTitle(GONE.getReasonPhrase())
				.withDetail(ERROR_TOKEN_EXPIRED)
				.build();
		}
	}
}
