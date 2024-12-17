package se.sundsvall.byggrintegrator.configuration;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cache")
record CacheProperties(boolean enabled, List<Cache> caches) {

	record Cache(@NotBlank String name, @NotNull Integer maximumSize, @NotNull Duration expireAfterWrite) {}
}
