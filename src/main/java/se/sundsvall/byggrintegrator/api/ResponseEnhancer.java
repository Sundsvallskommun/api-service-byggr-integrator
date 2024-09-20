package se.sundsvall.byggrintegrator.api;

import java.util.Optional;

import jakarta.servlet.http.HttpServletResponse;

public final class ResponseEnhancer {

	private static final String INFO_QUERY_RESPONSE_HEADER_NAME = "InfoQueryResponse";
	private static final String INFO_QUERY_RESPONSE_HEADER_VALUE = "true";

	private ResponseEnhancer() {
		//Shouldn't be instantiated
	}

	public static void addInfoQueryResponseHeader(HttpServletResponse response) {
		Optional.ofNullable(response)
			.ifPresent(httpServletResponse -> httpServletResponse.addHeader(INFO_QUERY_RESPONSE_HEADER_NAME, INFO_QUERY_RESPONSE_HEADER_VALUE));
	}
}
