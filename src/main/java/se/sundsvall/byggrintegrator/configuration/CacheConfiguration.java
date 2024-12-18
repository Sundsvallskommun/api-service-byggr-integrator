package se.sundsvall.byggrintegrator.configuration;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
@ConditionalOnProperty(value = "cache.enabled", havingValue = "true", matchIfMissing = true)
class CacheConfiguration {

	private final CacheProperties cacheProperties;

	CacheConfiguration(final CacheProperties cacheProperties) {
		this.cacheProperties = cacheProperties;
	}

	@Bean
	CacheManager cacheManager() {
		var caches = cacheProperties.caches().stream()
			.map(this::buildCache)
			.toList();

		var cacheManager = new SimpleCacheManager();
		cacheManager.setCaches(caches);
		return cacheManager;
	}

	private CaffeineCache buildCache(final CacheProperties.Cache cache) {
		return new CaffeineCache(cache.name(), Caffeine.newBuilder()
			.maximumSize(cache.maximumSize())
			.expireAfterWrite(cache.expireAfterWrite())
			.build());
	}
}
