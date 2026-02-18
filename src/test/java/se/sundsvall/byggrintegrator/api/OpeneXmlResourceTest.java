package se.sundsvall.byggrintegrator.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.sundsvall.byggrintegrator.Application;
import se.sundsvall.byggrintegrator.api.model.Weight;
import se.sundsvall.byggrintegrator.service.ByggrIntegratorService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_XML;
import static org.springframework.http.MediaType.APPLICATION_XML;

@ActiveProfiles("junit")
@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
class OpeneXmlResourceTest {

	private static final String INVALID_MUNICIPALITY_ID = "InvalidMunicipalityId";
	private static final String VALID_MUNICIPALITY_ID = "2281";
	private static final String CASE_NUMBER = "diaryNumber";
	private static final String VALUE = "value";
	private static final String ERRAND_TYPE_WITH_REQUEST_PARAMETER_URL = "/{municipalityId}/opene/cases/type";

	@MockitoBean
	private ByggrIntegratorService mockByggrIntegratorService;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void testGetErrandTypeWithRequestParameter() {
		final var weight = Weight.builder().withValue(VALUE).build();

		when(mockByggrIntegratorService.getErrandType(CASE_NUMBER)).thenReturn(weight);

		final var responseBody = webTestClient.get()
			.uri(builder -> builder.path(ERRAND_TYPE_WITH_REQUEST_PARAMETER_URL).queryParam("caseNumber", CASE_NUMBER).build(VALID_MUNICIPALITY_ID))
			.exchange()
			.expectStatus().isOk()
			.expectHeader()
			.contentType(APPLICATION_XML)
			.expectBody(String.class)
			.returnResult()
			.getResponseBody();

		assertThat(responseBody).isNotNull().isEqualToIgnoringNewLines("<Weight>value</Weight>");

		verify(mockByggrIntegratorService).getErrandType(CASE_NUMBER);
		verifyNoMoreInteractions(mockByggrIntegratorService);
	}

	@Test
	void testGetErrandTypeWithRequestParameter_faultyMunicipalityId_shouldThrowException() {
		final var responseBody = webTestClient.get()
			.uri(builder -> builder.path(ERRAND_TYPE_WITH_REQUEST_PARAMETER_URL).queryParam("caseNumber", CASE_NUMBER).build(INVALID_MUNICIPALITY_ID))
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_XML)
			.expectBody(String.class)
			.returnResult()
			.getResponseBody();

		assertThat(responseBody).isNotNull().contains("getErrandTypeWithRequestParameter.municipalityId: not a valid municipality ID");

		verifyNoInteractions(mockByggrIntegratorService);
	}
}
