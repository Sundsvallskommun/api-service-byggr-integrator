package se.sundsvall.byggrintegrator.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_XML;
import static org.springframework.http.MediaType.APPLICATION_XML;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import se.sundsvall.byggrintegrator.Application;
import se.sundsvall.byggrintegrator.api.model.Weight;
import se.sundsvall.byggrintegrator.service.ByggrIntegratorService;

@ActiveProfiles("junit")
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OpeneXmlResourceTest {

	@MockBean
	private ByggrIntegratorService mockByggrIntegratorService;

	@Autowired
	private WebTestClient webTestClient;

	private static final String INVALID_MUNICIPALITY_ID = "InvalidMunicipalityId";
	private static final String VALID_MUNICIPALITY_ID = "2281";
	private static final String DNR = "diaryNumber";
	private static final String VALUE = "value";

	private static final String ERRAND_TYPE_URL = "/{municipalityId}/opene/errand/{dnr}/type";

	// ----------------------------------------------------------------
	// ErrandType resources tests
	// ----------------------------------------------------------------
	@Test
	void testGetErrandType() {
		final var weight = Weight.builder().withValue(VALUE).build();

		when(mockByggrIntegratorService.getErrandType(DNR)).thenReturn(weight);

		final var responseBody = webTestClient.get()
			.uri(ERRAND_TYPE_URL, VALID_MUNICIPALITY_ID, DNR)
			.exchange()
			.expectStatus().isOk()
			.expectHeader()
			.contentType(APPLICATION_XML)
			.expectBody(String.class)
			.returnResult()
			.getResponseBody();

		assertThat(responseBody).isNotNull().isEqualToIgnoringNewLines("<Weight>value</Weight>");

		verify(mockByggrIntegratorService).getErrandType(DNR);
		verifyNoMoreInteractions(mockByggrIntegratorService);
	}

	@Test
	void testGetErrandType_faultyMunicipalityId_shouldThrowException() {
		final var responseBody = webTestClient.get()
			.uri(ERRAND_TYPE_URL, INVALID_MUNICIPALITY_ID, DNR)
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_XML)
			.expectBody(String.class)
			.returnResult()
			.getResponseBody();

		assertThat(responseBody).isNotNull().contains("getErrandType.municipalityId: not a valid municipality ID");

		verifyNoInteractions(mockByggrIntegratorService);
	}

}