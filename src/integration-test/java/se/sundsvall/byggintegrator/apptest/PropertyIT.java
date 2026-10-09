package se.sundsvall.byggintegrator.apptest;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import se.sundsvall.byggrintegrator.Application;
import se.sundsvall.byggrintegrator.service.FileAccessTokenService;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;

@WireMockAppTestSuite(files = "classpath:/propertyIT/", classes = Application.class)
@ActiveProfiles("it")
class PropertyIT extends AbstractAppTest {

	private static final String FIXED_TOKEN = "b0000000-a7b1-4c45-9cbb-000000000001";
	private static final String RESPONSE_FILE = "response.json";

	@MockitoBean
	private FileAccessTokenService fileAccessTokenService;

	@BeforeEach
	void setUp() {
		when(fileAccessTokenService.createToken(anyString(), anyString())).thenReturn(FIXED_TOKEN);
	}

	@Test
	void test01_getAllOvkProtocols() {
		setupCall()
			.withServicePath("/2281/properties/TESTÖN 1:123/errands/ovk")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponseHeader(CONTENT_TYPE, List.of(APPLICATION_JSON_VALUE))
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test02_getLatestOvkProtocol() {
		setupCall()
			.withServicePath("/2281/properties/TESTÖN 1:123/errands/ovk/latest")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponseHeader(CONTENT_TYPE, List.of(APPLICATION_JSON_VALUE))
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test03_getAllOvkProtocolsForUnknownProperty() {
		setupCall()
			.withServicePath("/2281/properties/OKÄND 1:1/errands/ovk")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponseHeader(CONTENT_TYPE, List.of(APPLICATION_JSON_VALUE))
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test04_getLatestOvkProtocolForUnknownProperty() {
		setupCall()
			.withServicePath("/2281/properties/OKÄND 1:1/errands/ovk/latest")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(NOT_FOUND)
			.withExpectedResponseHeader(CONTENT_TYPE, List.of(APPLICATION_PROBLEM_JSON_VALUE))
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}
}
