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
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;
import static org.springframework.http.MediaType.TEXT_HTML_VALUE;

@WireMockAppTestSuite(files = "classpath:/openeIT/", classes = Application.class)
@ActiveProfiles("it")
class OpeneIT extends AbstractAppTest {

	private static final String INFO_QUERY_RESPONSE_HEADER_NAME = "InfoQueryResponse";
	private static final String FIXED_TOKEN = "b9d310c5-a7b1-4c45-9cbb-402b4c5c8de0";
	private static final String RESPONSE_FILE_XML = "response.xml";
	private static final String RESPONSE_FILE_HTML = "response.html";

	@MockitoBean
	private FileAccessTokenService fileAccessTokenService;

	@BeforeEach
	void setUp() {
		when(fileAccessTokenService.createToken(anyString(), anyString())).thenReturn(FIXED_TOKEN);
	}

	@Test
	void test01_getTypeForExistingErrandWithRequestParameter() {
		setupCall()
			.withServicePath("/2281/opene/cases/type?caseNumber=BYGG 2024-000666")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponseHeader(CONTENT_TYPE, List.of(APPLICATION_XML_VALUE))
			.withExpectedResponse(RESPONSE_FILE_XML)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test02_getNeighborhoodNotificationFilenamesForErrandWithEventsByRequestParameter() {
		setupCall()
			.withServicePath("/2281/opene/neighborhood-notifications/filenames?identifier=190304056789&caseNumber=BYGG 2024-000668&referralReference=SKÖNSMON 1:1 - besvarad 2024-09-30 [305903]")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponseHeader(CONTENT_TYPE, List.of(TEXT_HTML_VALUE + ";" + "charset=UTF-8"))
			.withExpectedResponseHeader(INFO_QUERY_RESPONSE_HEADER_NAME, List.of("true"))
			.withExpectedResponse(RESPONSE_FILE_HTML)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test03_getPropertyDesignationForExistingErrandWithRequestParameter() {
		setupCall()
			.withServicePath("/2281/opene/cases/property-designation?caseNumber=BYGG 2024-000666")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponseHeader(CONTENT_TYPE, List.of(TEXT_HTML_VALUE + ";" + "charset=UTF-8"))
			.withExpectedResponseHeader(INFO_QUERY_RESPONSE_HEADER_NAME, List.of("true"))
			.withExpectedResponse(RESPONSE_FILE_HTML)
			.sendRequestAndVerifyResponse();
	}

}
