package se.sundsvall.byggintegrator.apptest;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import se.sundsvall.byggrintegrator.Application;
import se.sundsvall.byggrintegrator.integration.db.FileAccessTokenRepository;
import se.sundsvall.byggrintegrator.integration.db.model.FileAccessTokenEntity;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;

@WireMockAppTestSuite(files = "classpath:/fileIT/", classes = Application.class)
@ActiveProfiles("it")
class FileIT extends AbstractAppTest {

	private static final String RESPONSE_FILE = "response.json";
	private static final String RESPONSE_FILE_BINARY = "situationsplan.pdf";

	@Autowired
	private FileAccessTokenRepository fileAccessTokenRepository;

	private String validToken;
	private String validTokenForNonExisting;
	private String validTokenForNonNumeric;

	@BeforeEach
	void setupTokens() {
		fileAccessTokenRepository.deleteAll();

		validToken = fileAccessTokenRepository.save(FileAccessTokenEntity.create()
			.withFileId("123456789")
			.withMunicipalityId("2281")
			.withExpiresAt(OffsetDateTime.now().plusHours(24))).getId();

		validTokenForNonExisting = fileAccessTokenRepository.save(FileAccessTokenEntity.create()
			.withFileId("123123123")
			.withMunicipalityId("2281")
			.withExpiresAt(OffsetDateTime.now().plusHours(24))).getId();

		validTokenForNonNumeric = fileAccessTokenRepository.save(FileAccessTokenEntity.create()
			.withFileId("ABCABCABC")
			.withMunicipalityId("2281")
			.withExpiresAt(OffsetDateTime.now().plusHours(24))).getId();
	}

	@Test
	void test01_getFile() throws IOException {
		setupCall()
			.withServicePath("/2281/files/123456789?token=" + validToken)
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponseHeader(CONTENT_DISPOSITION, List.of("attachment; filename=\"situationsplan.pdf\""))
			.withExpectedResponseHeader(CONTENT_TYPE, List.of("application/pdf"))
			.withExpectedBinaryResponse(RESPONSE_FILE_BINARY)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test02_getNonExistingFile() {
		setupCall()
			.withServicePath("/2281/files/123123123?token=" + validTokenForNonExisting)
			.withHttpMethod(GET)
			.withExpectedResponseStatus(NOT_FOUND)
			.withExpectedResponseHeader(CONTENT_TYPE, List.of(APPLICATION_PROBLEM_JSON_VALUE))
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test03_getFileForNonNumericId() {
		setupCall()
			.withServicePath("/2281/files/ABCABCABC?token=" + validTokenForNonNumeric)
			.withHttpMethod(GET)
			.withExpectedResponseStatus(NOT_FOUND)
			.withExpectedResponseHeader(CONTENT_TYPE, List.of(APPLICATION_PROBLEM_JSON_VALUE))
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

}
