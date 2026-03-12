package se.sundsvall.byggrintegrator.service.scheduler;

import java.time.OffsetDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import se.sundsvall.byggrintegrator.integration.db.FileAccessTokenRepository;
import se.sundsvall.dept44.scheduling.Dept44Scheduled;

@Component
public class FileAccessTokenScheduler {

	private static final Logger LOG = LoggerFactory.getLogger(FileAccessTokenScheduler.class);

	private final FileAccessTokenRepository repository;

	public FileAccessTokenScheduler(final FileAccessTokenRepository repository) {
		this.repository = repository;
	}

	@Dept44Scheduled(cron = "${file-access-token.cron}", name = "FileAccessTokenCleanup", lockAtMostFor = "PT10M", maximumExecutionTime = "PT10M")
	@Transactional
	public void execute() {
		LOG.info("Starting cleanup of expired file access tokens");
		repository.deleteByExpiresAtBefore(OffsetDateTime.now());
		LOG.info("Cleanup of expired file access tokens completed");
	}
}
