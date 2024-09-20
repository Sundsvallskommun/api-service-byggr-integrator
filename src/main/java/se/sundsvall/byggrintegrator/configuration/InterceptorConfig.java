package se.sundsvall.byggrintegrator.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import se.sundsvall.byggrintegrator.api.interceptor.OpeneHtmlInterceptor;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

	/**
	 * Intercept all html responses from the "/{municipalityId}/opene/cases/{wildcard}/neighborhood-notifications" endpoint.
	 * @param registry the interceptor registry
	 */
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(new OpeneHtmlInterceptor())
			.addPathPatterns("/{municipalityId}/opene/cases/*/neighborhood-notifications");
	}
}
