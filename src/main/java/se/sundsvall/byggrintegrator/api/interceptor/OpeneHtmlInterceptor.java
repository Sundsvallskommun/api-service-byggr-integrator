package se.sundsvall.byggrintegrator.api.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.servlet.HandlerInterceptor;

import static se.sundsvall.byggrintegrator.api.ResponseEnhancer.addInfoQueryResponseHeader;

public class OpeneHtmlInterceptor implements HandlerInterceptor {

	/**
	 * Add InfoQueryResponse-header to the response.
	 * 
	 * @param  request  request that is being handled
	 * @param  response to add headers to
	 * @param  handler  handler that is being used
	 * @return          true that the request should be handled
	 */
	@Override
	public boolean preHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler) {
		addInfoQueryResponseHeader(response);
		return true;
	}
}
