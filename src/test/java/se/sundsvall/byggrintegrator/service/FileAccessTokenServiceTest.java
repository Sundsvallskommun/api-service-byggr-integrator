package se.sundsvall.byggrintegrator.service;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.byggrintegrator.configuration.FileAccessTokenProperties;
import se.sundsvall.byggrintegrator.integration.db.FileAccessTokenRepository;
import se.sundsvall.byggrintegrator.integration.db.model.FileAccessTokenEntity;
import se.sundsvall.dept44.problem.ThrowableProblem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.GONE;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@ExtendWith(MockitoExtension.class)
class FileAccessTokenServiceTest {

	private static final String MUNICIPALITY_ID = "2281";
	private static final String FILE_ID = "12345";

	@Mock
	private FileAccessTokenRepository mockRepository;

	@Mock
	private FileAccessTokenProperties mockProperties;

	@Captor
	private ArgumentCaptor<FileAccessTokenEntity> entityCaptor;

	@InjectMocks
	private FileAccessTokenService service;

	@Test
	void testCreateToken() {
		final var expiration = Duration.ofHours(24);
		final var savedEntity = FileAccessTokenEntity.create()
			.withId("generated-uuid")
			.withFileId(FILE_ID)
			.withMunicipalityId(MUNICIPALITY_ID);

		when(mockProperties.expiration()).thenReturn(expiration);
		when(mockRepository.save(any(FileAccessTokenEntity.class))).thenReturn(savedEntity);

		final var token = service.createToken(MUNICIPALITY_ID, FILE_ID);

		assertThat(token).isEqualTo("generated-uuid");

		verify(mockProperties).expiration();
		verify(mockRepository).save(entityCaptor.capture());
		verifyNoMoreInteractions(mockRepository, mockProperties);

		final var captured = entityCaptor.getValue();
		assertThat(captured.getFileId()).isEqualTo(FILE_ID);
		assertThat(captured.getMunicipalityId()).isEqualTo(MUNICIPALITY_ID);
		assertThat(captured.getExpiresAt()).isCloseTo(OffsetDateTime.now().plusHours(24), within(1, java.time.temporal.ChronoUnit.MINUTES));
	}

	@Test
	void testValidateToken_validToken() {
		final var tokenId = "valid-token";
		final var entity = FileAccessTokenEntity.create()
			.withId(tokenId)
			.withFileId(FILE_ID)
			.withMunicipalityId(MUNICIPALITY_ID)
			.withExpiresAt(OffsetDateTime.now().plusHours(24));

		when(mockRepository.findByIdAndFileIdAndMunicipalityId(tokenId, FILE_ID, MUNICIPALITY_ID))
			.thenReturn(Optional.of(entity));

		service.validateToken(MUNICIPALITY_ID, FILE_ID, tokenId);

		verify(mockRepository).findByIdAndFileIdAndMunicipalityId(tokenId, FILE_ID, MUNICIPALITY_ID);
		verifyNoMoreInteractions(mockRepository);
	}

	@Test
	void testValidateToken_tokenNotFound() {
		final var tokenId = "nonexistent-token";

		when(mockRepository.findByIdAndFileIdAndMunicipalityId(tokenId, FILE_ID, MUNICIPALITY_ID))
			.thenReturn(Optional.empty());

		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> service.validateToken(MUNICIPALITY_ID, FILE_ID, tokenId))
			.satisfies(throwableProblem -> {
				assertThat(throwableProblem.getStatus()).isEqualTo(NOT_FOUND);
				assertThat(throwableProblem.getTitle()).isEqualTo(NOT_FOUND.getReasonPhrase());
				assertThat(throwableProblem.getDetail()).isEqualTo("File access token was not found");
			});

		verify(mockRepository).findByIdAndFileIdAndMunicipalityId(tokenId, FILE_ID, MUNICIPALITY_ID);
		verifyNoMoreInteractions(mockRepository);
	}

	@Test
	void testValidateToken_nullToken() {
		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> service.validateToken(MUNICIPALITY_ID, FILE_ID, null))
			.satisfies(throwableProblem -> {
				assertThat(throwableProblem.getStatus()).isEqualTo(NOT_FOUND);
				assertThat(throwableProblem.getTitle()).isEqualTo(NOT_FOUND.getReasonPhrase());
				assertThat(throwableProblem.getDetail()).isEqualTo("File access token was not found");
			});

		verifyNoMoreInteractions(mockRepository);
	}

	@Test
	void testValidateToken_expiredToken() {
		final var tokenId = "expired-token";
		final var entity = FileAccessTokenEntity.create()
			.withId(tokenId)
			.withFileId(FILE_ID)
			.withMunicipalityId(MUNICIPALITY_ID)
			.withExpiresAt(OffsetDateTime.now().minusHours(1));

		when(mockRepository.findByIdAndFileIdAndMunicipalityId(tokenId, FILE_ID, MUNICIPALITY_ID))
			.thenReturn(Optional.of(entity));

		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> service.validateToken(MUNICIPALITY_ID, FILE_ID, tokenId))
			.satisfies(throwableProblem -> {
				assertThat(throwableProblem.getStatus()).isEqualTo(GONE);
				assertThat(throwableProblem.getTitle()).isEqualTo(GONE.getReasonPhrase());
				assertThat(throwableProblem.getDetail()).isEqualTo("File access token has expired");
			});

		verify(mockRepository).findByIdAndFileIdAndMunicipalityId(tokenId, FILE_ID, MUNICIPALITY_ID);
		verifyNoMoreInteractions(mockRepository);
	}
}
