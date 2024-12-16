package se.sundsvall.byggrintegrator.configuration;

import jakarta.validation.constraints.NotBlank;
import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "cache")
public record CacheProperties(boolean enabled, List<Cache> caches) {

	public record Cache(@NotBlank String name, @DefaultValue("100") int maximumSize, @DefaultValue("PT5M") Duration expireAfterWrite) {}
}
