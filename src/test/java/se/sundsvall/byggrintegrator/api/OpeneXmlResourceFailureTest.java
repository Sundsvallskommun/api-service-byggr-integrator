package se.sundsvall.byggrintegrator.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.sundsvall.byggrintegrator.Application;
import se.sundsvall.byggrintegrator.service.ByggrIntegratorService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_XML;

@ActiveProfiles("junit")
@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
class OpeneXmlResourceFailureTest {

	private static final String INVALID_MUNICIPALITY_ID = "InvalidMunicipalityId";
	private static final String VALID_MUNICIPALITY_ID = "2281";
	private static final String CASE_NUMBER = "diaryNumber";
	private static final String BLANK_CASE_NUMBER = "";
	private static final String IDENTIFIER = "190102031234";
	private static final String BLANK_IDENTIFIER = "";
	private static final String REFERRAL_REFERENCE = "SILJE 2:65 – Lämna svar som fastighetsägare [188115]";
	private static final String BLANK_REFERRAL_REFERENCE = "";
	private static final String ERRAND_TYPE_WITH_REQUEST_PARAMETER_URL = "/{municipalityId}/opene/cases/type";
	private static final String REFERRAL_TYPE_WITH_REQUEST_PARAMETER_URL = "/{municipalityId}/opene/referrals/type";

	@MockitoBean
	private ByggrIntegratorService mockByggrIntegratorService;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void testGetErrandTypeWithRequestParameter_faultyMunicipalityId_shouldThrowException() {
		final var responseBody = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(ERRAND_TYPE_WITH_REQUEST_PARAMETER_URL).queryParam("caseNumber", CASE_NUMBER).build(INVALID_MUNICIPALITY_ID))
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_XML)
			.expectBody(String.class)
			.returnResult()
			.getResponseBody();

		assertThat(responseBody).isNotNull().contains("getErrandTypeWithRequestParameter.municipalityId: not a valid municipality ID");

		verifyNoInteractions(mockByggrIntegratorService);
	}

	@Test
	void testGetErrandTypeWithRequestParameter_blankCaseNumber() {
		final var responseBody = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(ERRAND_TYPE_WITH_REQUEST_PARAMETER_URL).queryParam("caseNumber", BLANK_CASE_NUMBER).build(VALID_MUNICIPALITY_ID))
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_XML)
			.expectBody(String.class)
			.returnResult()
			.getResponseBody();

		assertThat(responseBody).isNotEmpty().isNotNull().contains("getErrandTypeWithRequestParameter.caseNumber: must not be blank");

		verifyNoInteractions(mockByggrIntegratorService);
	}

	@Test
	void testGetErrandTypeWithRequestParameter_missingCaseNumber() {
		final var responseBody = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(ERRAND_TYPE_WITH_REQUEST_PARAMETER_URL)
				.build(VALID_MUNICIPALITY_ID))
			.exchange()
			.expectStatus().is5xxServerError()
			.expectHeader().contentType(APPLICATION_PROBLEM_XML)
			.expectBody(String.class)
			.returnResult()
			.getResponseBody();

		assertThat(responseBody).isNotEmpty().isNotNull().contains("caseNumber", "not present");

		verifyNoInteractions(mockByggrIntegratorService);
	}

	@Test
	void testGetReferralTypeWithRequestParameter_faultyMunicipalityId_shouldThrowException() {
		final var responseBody = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(REFERRAL_TYPE_WITH_REQUEST_PARAMETER_URL)
				.queryParam("identifier", IDENTIFIER)
				.queryParam("referralReference", REFERRAL_REFERENCE)
				.build(INVALID_MUNICIPALITY_ID))
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_XML)
			.expectBody(String.class)
			.returnResult()
			.getResponseBody();

		assertThat(responseBody).isNotNull().contains("getReferralTypeWithRequestParameter.municipalityId: not a valid municipality ID");

		verifyNoInteractions(mockByggrIntegratorService);
	}

	@Test
	void testGetReferralTypeWithRequestParameter_blankIdentifier() {
		final var responseBody = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(REFERRAL_TYPE_WITH_REQUEST_PARAMETER_URL)
				.queryParam("identifier", BLANK_IDENTIFIER)
				.queryParam("referralReference", REFERRAL_REFERENCE)
				.build(VALID_MUNICIPALITY_ID))
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_XML)
			.expectBody(String.class)
			.returnResult()
			.getResponseBody();

		assertThat(responseBody).isNotNull().contains("getReferralTypeWithRequestParameter.identifier: must not be blank");

		verifyNoInteractions(mockByggrIntegratorService);
	}

	@Test
	void testGetReferralTypeWithRequestParameter_missingIdentifier() {
		final var responseBody = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(REFERRAL_TYPE_WITH_REQUEST_PARAMETER_URL)
				.queryParam("referralReference", REFERRAL_REFERENCE)
				.build(VALID_MUNICIPALITY_ID))
			.exchange()
			.expectStatus().is5xxServerError()
			.expectHeader().contentType(APPLICATION_PROBLEM_XML)
			.expectBody(String.class)
			.returnResult()
			.getResponseBody();

		assertThat(responseBody).isNotNull().contains("identifier", "not present");

		verifyNoInteractions(mockByggrIntegratorService);
	}

	@Test
	void testGetReferralTypeWithRequestParameter_blankReferral() {
		final var responseBody = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(REFERRAL_TYPE_WITH_REQUEST_PARAMETER_URL)
				.queryParam("identifier", IDENTIFIER)
				.queryParam("referralReference", BLANK_REFERRAL_REFERENCE)
				.build(VALID_MUNICIPALITY_ID))
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_XML)
			.expectBody(String.class)
			.returnResult()
			.getResponseBody();

		assertThat(responseBody).isNotNull().contains("getReferralTypeWithRequestParameter.referralReference: must not be blank");

		verifyNoInteractions(mockByggrIntegratorService);
	}

	@Test
	void testGetReferralTypeWithRequestParameter_missingReferral() {
		final var responseBody = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(REFERRAL_TYPE_WITH_REQUEST_PARAMETER_URL)
				.queryParam("identifier", IDENTIFIER)
				.build(VALID_MUNICIPALITY_ID))
			.exchange()
			.expectStatus().is5xxServerError()
			.expectHeader().contentType(APPLICATION_PROBLEM_XML)
			.expectBody(String.class)
			.returnResult()
			.getResponseBody();

		assertThat(responseBody).isNotNull().contains("referralReference", "not present");

		verifyNoInteractions(mockByggrIntegratorService);
	}
}
