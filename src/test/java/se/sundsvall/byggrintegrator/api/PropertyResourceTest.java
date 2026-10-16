package se.sundsvall.byggrintegrator.api;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import se.sundsvall.byggrintegrator.Application;
import se.sundsvall.byggrintegrator.api.model.OVKProtocol;
import se.sundsvall.byggrintegrator.service.ByggrIntegratorService;
import se.sundsvall.dept44.problem.Problem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.MediaType.ALL;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;

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
	private static final String DESCRIPTION = "Uppdatering OVK";
	private static final byte[] PDF_CONTENT = "%PDF-1.7\n%%EOF".getBytes();

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

	@Test
	void createOvkErrand() {
		final var body = new MultipartBodyBuilder();
		body.part("file", PDF_CONTENT).filename("protokoll.pdf").contentType(MediaType.APPLICATION_PDF);
		body.part("description", DESCRIPTION);

		webTestClient.post()
			.uri(OVK_URL, MUNICIPALITY_ID, PROPERTY_DESIGNATION)
			.body(BodyInserters.fromMultipartData(body.build()))
			.exchange()
			.expectStatus().isCreated()
			.expectHeader().contentType(ALL)
			.expectHeader().valueMatches(LOCATION, "^/2281/properties/.+/errands/ovk$")
			.expectBody().isEmpty();

		final var fileCaptor = ArgumentCaptor.forClass(MultipartFile.class);
		verify(mockByggrIntegratorService).createOVKErrand(eq(MUNICIPALITY_ID), eq(PROPERTY_DESIGNATION), eq(DESCRIPTION), fileCaptor.capture());
		assertThat(fileCaptor.getValue().getOriginalFilename()).isEqualTo("protokoll.pdf");
		verifyNoMoreInteractions(mockByggrIntegratorService);
	}

	@Test
	void createOvkErrand_withoutDescription() {
		final var body = new MultipartBodyBuilder();
		body.part("file", PDF_CONTENT).filename("protokoll.pdf").contentType(MediaType.APPLICATION_PDF);

		webTestClient.post()
			.uri(OVK_URL, MUNICIPALITY_ID, PROPERTY_DESIGNATION)
			.contentType(MULTIPART_FORM_DATA)
			.body(BodyInserters.fromMultipartData(body.build()))
			.exchange()
			.expectStatus().isCreated();

		verify(mockByggrIntegratorService).createOVKErrand(eq(MUNICIPALITY_ID), eq(PROPERTY_DESIGNATION), isNull(), any(MultipartFile.class));
		verifyNoMoreInteractions(mockByggrIntegratorService);
	}
}
