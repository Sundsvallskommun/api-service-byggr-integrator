package se.sundsvall.byggrintegrator.api;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.sundsvall.byggrintegrator.Application;
import se.sundsvall.byggrintegrator.api.model.OVKProtocol;
import se.sundsvall.byggrintegrator.service.ByggrIntegratorService;
import se.sundsvall.dept44.problem.Problem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@ActiveProfiles("junit")
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class PropertyResourceTest {

	@MockitoBean
	private ByggrIntegratorService mockByggrIntegratorService;

	@Autowired
	private WebTestClient webTestClient;

	private static final String PROPERTY_DESIGNATION = "TESTÖN 1:123";
	private static final String MUNICIPALITY_ID = "2281";
	private static final String OVK_URL = "/{municipalityId}/properties/{propertyDesignation}/errands/ovk";
	private static final String OVK_LATEST_URL = OVK_URL + "/latest";
	private static final String FILE_URL = "http://test.se/2.6/2281/files/1234567?token=abc";
	private static final String NO_OVK_FOUND = "No OVK was found for :" + PROPERTY_DESIGNATION;

	@Test
	void getAllOVKProtocols() {
		final var protocol = new OVKProtocol("OVK 12345", "Description", LocalDate.of(2026, Month.JUNE, 5), "OVK_protokoll", "12345", FILE_URL);

		when(mockByggrIntegratorService.getOVKprotocols(MUNICIPALITY_ID, PROPERTY_DESIGNATION)).thenReturn(List.of(protocol));

		final var response = webTestClient.get()
			.uri(OVK_URL, MUNICIPALITY_ID, PROPERTY_DESIGNATION)
			.exchange()
			.expectStatus().isOk()
			.expectBodyList(OVKProtocol.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).containsExactly(protocol);
		verify(mockByggrIntegratorService).getOVKprotocols(MUNICIPALITY_ID, PROPERTY_DESIGNATION);
		verifyNoMoreInteractions(mockByggrIntegratorService);
	}

	@Test
	void getLatestOVKProtocol() {
		final var protocol = new OVKProtocol("OVK 12345", "Description", LocalDate.of(2026, Month.JUNE, 5), "OVK_protokoll", "12345", FILE_URL);

		when(mockByggrIntegratorService.getLatestOVKprotocol(MUNICIPALITY_ID, PROPERTY_DESIGNATION)).thenReturn(protocol);

		final var response = webTestClient.get()
			.uri(OVK_LATEST_URL, MUNICIPALITY_ID, PROPERTY_DESIGNATION)
			.exchange()
			.expectStatus().isOk()
			.expectBody(OVKProtocol.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isEqualTo(protocol);
		verify(mockByggrIntegratorService).getLatestOVKprotocol(MUNICIPALITY_ID, PROPERTY_DESIGNATION);
		verifyNoMoreInteractions(mockByggrIntegratorService);
	}

	@Test
	void getLatestOVKProtocol_notFound() {
		when(mockByggrIntegratorService.getLatestOVKprotocol(MUNICIPALITY_ID, PROPERTY_DESIGNATION)).thenThrow(Problem.valueOf(NOT_FOUND, NO_OVK_FOUND));

		final var response = webTestClient.get()
			.uri(OVK_LATEST_URL, MUNICIPALITY_ID, PROPERTY_DESIGNATION)
			.exchange()
			.expectStatus().isNotFound()
			.expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
			.expectBody(Problem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getStatus()).isEqualTo(NOT_FOUND);
		assertThat(response.getDetail()).isEqualTo(NO_OVK_FOUND);
		verify(mockByggrIntegratorService).getLatestOVKprotocol(MUNICIPALITY_ID, PROPERTY_DESIGNATION);
		verifyNoMoreInteractions(mockByggrIntegratorService);
	}
}
