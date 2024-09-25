package se.sundsvall.byggintegrator.apptest;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_XML_VALUE;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;
import static org.springframework.http.MediaType.TEXT_HTML_VALUE;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import se.sundsvall.byggrintegrator.Application;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

@WireMockAppTestSuite(files = "classpath:/openeIT/", classes = Application.class)
@ActiveProfiles("it")
class OpeneIT extends AbstractAppTest {

	private static final String INFO_QUERY_RESPONSE_HEADER_NAME = "InfoQueryResponse";
	private static final String RESPONSE_FILE = "response.xml";

	@Test
	void test01_getTypeForExistingErrand() {
		setupCall()
			.withServicePath("/2281/opene/cases/BYGG 2024-000666/type")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponseHeader(CONTENT_TYPE, List.of(APPLICATION_XML_VALUE))
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test02_getTypeForNonExistingErrand() {
		setupCall()
			.withServicePath("/2281/opene/cases/BYGG 2024-000667/type")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(NOT_FOUND)
			.withExpectedResponseHeader(CONTENT_TYPE, List.of(APPLICATION_PROBLEM_XML_VALUE))
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test03_getNeighborhoodNotificationFilenamesForErrandAndEvent() {
		setupCall()
			.withServicePath("/2281/opene/neighborhood-notifications/BYGG 2024-000668 [1465852]/filenames")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponseHeader(CONTENT_TYPE, List.of(TEXT_HTML_VALUE + ";" + "charset=UTF-8"))
			.withExpectedResponseHeader(INFO_QUERY_RESPONSE_HEADER_NAME, List.of("true"))
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test04_getNeighborhoodNotificationFilenamesForNonExistingErrand() {
		setupCall()
			.withServicePath("/2281/opene/neighborhood-notifications/BYGG 2024-000669 [1465852]/filenames")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponseHeader(CONTENT_TYPE, List.of(TEXT_HTML_VALUE + ";" + "charset=UTF-8"))
			.withExpectedResponseHeader(INFO_QUERY_RESPONSE_HEADER_NAME, List.of("true"))
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test05_getNeighborhoodNotificationFilenamesForNonExistingEventId() {
		setupCall()
			.withServicePath("/2281/opene/neighborhood-notifications/BYGG 2024-000670 [1465890]/filenames")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponseHeader(CONTENT_TYPE, List.of(TEXT_HTML_VALUE + ";" + "charset=UTF-8"))
			.withExpectedResponseHeader(INFO_QUERY_RESPONSE_HEADER_NAME, List.of("true"))
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}
}
