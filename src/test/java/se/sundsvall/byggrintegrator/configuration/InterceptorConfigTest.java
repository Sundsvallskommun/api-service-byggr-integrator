package se.sundsvall.byggrintegrator.configuration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

import se.sundsvall.byggrintegrator.api.interceptor.OpeneHtmlInterceptor;

@ExtendWith(MockitoExtension.class)
class InterceptorConfigTest {

	private static final String PATH_PATTERN = "/{municipalityId}/opene/cases/*/neighborhood-notifications";

	@Mock
	private InterceptorRegistry mockRegistry;

	@Mock
	private InterceptorRegistration mockRegistration;

	private final InterceptorConfig config = new InterceptorConfig();

	@Test
	void test() {
		when(mockRegistry.addInterceptor(any())).thenReturn(mockRegistration);

		config.addInterceptors(mockRegistry);

		verify(mockRegistry).addInterceptor(any(OpeneHtmlInterceptor.class));
		verify(mockRegistration).addPathPatterns(PATH_PATTERN);
		verifyNoMoreInteractions(mockRegistry, mockRegistration);
	}
}
