package se.sundsvall.byggrintegrator.api;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.byggrintegrator.api.ResponseEnhancer.addHeaderToResponse;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.mock.web.MockHttpServletResponse;

import jakarta.servlet.http.HttpServletResponse;

class ResponseEnhancerTest {

	private static final String INFO_QUERY_RESPONSE_HEADER_NAME = "InfoQueryResponse";
	private static final String INFO_QUERY_RESPONSE_HEADER_VALUE = "true";
	private static final String SOME_HEADER = "someHeader";
	private static final String SOME_HEADER_VALUE = "someHeaderValue";

	@Test
	void testAddResponseHeader() {
		var response = new MockHttpServletResponse();
		addHeaderToResponse(response, SOME_HEADER, SOME_HEADER_VALUE);

		assertThat(response.getHeader(SOME_HEADER)).isEqualTo(SOME_HEADER_VALUE);
	}

	@Test
	void testAddInfoQueryResponseHeader() {
		var response = new MockHttpServletResponse();
		addHeaderToResponse(response, INFO_QUERY_RESPONSE_HEADER_NAME, INFO_QUERY_RESPONSE_HEADER_VALUE);

		assertThat(response.getHeader(INFO_QUERY_RESPONSE_HEADER_NAME)).isEqualTo(INFO_QUERY_RESPONSE_HEADER_VALUE);
	}

	@ParameterizedTest
	@MethodSource("provideFaultyHeaders")
	void testAddFaultyHeaders(HttpServletResponse response, String headerName, String headerValue) {
		addHeaderToResponse(response, headerName, headerValue);

		//Check that we didn't add or remove any headers.
		assertThat(response.getHeaderNames()).containsExactly(SOME_HEADER);
		assertThat(response.getHeader(SOME_HEADER)).isEqualTo(SOME_HEADER_VALUE);
	}

	public static Stream<Arguments> provideFaultyHeaders() {
		var response = new MockHttpServletResponse();
		response.addHeader(SOME_HEADER, SOME_HEADER_VALUE); //Add a header so we can verify that it's not removed
		return Stream.of(
			Arguments.of(response, SOME_HEADER, null),
			Arguments.of(response, SOME_HEADER, ""),
			Arguments.of(response, SOME_HEADER, " "),
			Arguments.of(response, null, SOME_HEADER_VALUE),
			Arguments.of(response, "", SOME_HEADER_VALUE),
			Arguments.of(response, " ", SOME_HEADER_VALUE)
		);
	}
}
