package se.sundsvall.byggrintegrator.api.interceptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import se.sundsvall.byggrintegrator.api.ResponseEnhancer;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class OpeneHtmlInterceptorTest {

	@Mock
	private HttpServletRequest mockRequest;

	@Mock
	private HttpServletResponse mockResponse;

	@Mock
	private Object mockHandler;

	private final OpeneHtmlInterceptor mockInterceptor = new OpeneHtmlInterceptor();

	@Test
	void testPreHandle() {
		try (MockedStatic<ResponseEnhancer> mockedStatic = mockStatic(ResponseEnhancer.class)) {

			assertThat(mockInterceptor.preHandle(mockRequest, mockResponse, mockHandler)).isTrue();
			mockedStatic.verify(() -> ResponseEnhancer.addInfoQueryResponseHeader(mockResponse));
		}

		verifyNoInteractions(mockRequest, mockResponse, mockHandler);
	}
}
