package se.sundsvall.byggrintegrator.service.scheduler;

import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.byggrintegrator.integration.db.FileAccessTokenRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class FileAccessTokenSchedulerTest {

	@Mock
	private FileAccessTokenRepository mockRepository;

	@Captor
	private ArgumentCaptor<OffsetDateTime> timestampCaptor;

	@InjectMocks
	private FileAccessTokenScheduler scheduler;

	@Test
	void testExecute() {
		scheduler.execute();

		verify(mockRepository).deleteByExpiresAtBefore(timestampCaptor.capture());
		verifyNoMoreInteractions(mockRepository);

		assertThat(timestampCaptor.getValue()).isCloseTo(OffsetDateTime.now(), within(1, java.time.temporal.ChronoUnit.MINUTES));
	}
}
