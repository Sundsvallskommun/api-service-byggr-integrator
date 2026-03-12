package se.sundsvall.byggrintegrator.configuration;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "file-access-token")
public record FileAccessTokenProperties(Duration expiration, String cron) {
}
