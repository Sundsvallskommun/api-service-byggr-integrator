package se.sundsvall.byggrintegrator.api;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import jakarta.servlet.http.HttpServletResponse;

public final class ResponseEnhancer {

	private static final String INFO_QUERY_RESPONSE_HEADER_NAME = "InfoQueryResponse";
	private static final String INFO_QUERY_RESPONSE_HEADER_VALUE = "true";

	private ResponseEnhancer() {
		//Shouldn't be instantiated
	}

	public static void addHeaderToResponse(HttpServletResponse response, String headerName, String headerValue) {
		if( response != null && isNotBlank(headerName) && isNotBlank(headerValue) ) {
			response.addHeader(headerName, headerValue);
		}
	}

	public static void addInfoQueryResponseHeader(HttpServletResponse response) {
		addHeaderToResponse(response, INFO_QUERY_RESPONSE_HEADER_NAME, INFO_QUERY_RESPONSE_HEADER_VALUE);
	}
}
