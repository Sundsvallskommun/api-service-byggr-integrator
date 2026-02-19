package se.sundsvall.byggrintegrator.api;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.byggrintegrator.api.ResponseEnhancer.addInfoQueryResponseHeader;

class ResponseEnhancerTest {

	private static final String INFO_QUERY_RESPONSE_HEADER_NAME = "InfoQueryResponse";
	private static final String INFO_QUERY_RESPONSE_HEADER_VALUE = "true";

	@Test
	void testAddInfoQueryResponseHeader() {
		var response = new MockHttpServletResponse();
		addInfoQueryResponseHeader(response);

		assertThat(response.getHeader(INFO_QUERY_RESPONSE_HEADER_NAME)).isEqualTo(INFO_QUERY_RESPONSE_HEADER_VALUE);
	}
}
