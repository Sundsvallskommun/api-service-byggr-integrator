package se.sundsvall.byggrintegrator.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.sundsvall.byggrintegrator.Application;
import se.sundsvall.byggrintegrator.service.ByggrIntegratorService;
import se.sundsvall.dept44.problem.violations.ConstraintViolationProblem;
import se.sundsvall.dept44.problem.violations.Violation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;

@ActiveProfiles("junit")
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class PropertyResourceFailureTest {

	@MockitoBean
	private ByggrIntegratorService mockByggrIntegratorService;

	@Autowired
	private WebTestClient webTestClient;

	private static final String INVALID_MUNICIPALITY_ID = "invalid";
	private static final String PROPERTY_DESIGNATION = "TESTÖN 1:123";
	private static final String OVK_URL = "/{municipalityId}/properties/{propertyDesignation}/errands/ovk";
	private static final String OVK_LATEST_URL = OVK_URL + "/latest";

	@Test
	void getAllOVKProtocols_invalidMunicipalityId() {
		final var response = webTestClient.get()
			.uri(OVK_URL, INVALID_MUNICIPALITY_ID, PROPERTY_DESIGNATION)
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getViolations())
			.extracting(Violation::message)
			.containsExactly("not a valid municipality ID");

		verifyNoInteractions(mockByggrIntegratorService);
	}

	@Test
	void getLatestOVKProtocol_invalidMunicipalityId() {
		final var response = webTestClient.get()
			.uri(OVK_LATEST_URL, INVALID_MUNICIPALITY_ID, PROPERTY_DESIGNATION)
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getViolations())
			.extracting(Violation::message)
			.containsExactly("not a valid municipality ID");

		verifyNoInteractions(mockByggrIntegratorService);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"TESTÖN",
		"TESTÖN123",
		"TESTÖN1:123",
		" "
	})
	void getAllOVKProtocols_invalidPropertyDesignation(final String propertyDesignation) {
		final var response = webTestClient.get()
			.uri(OVK_URL, "2281", propertyDesignation)
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations())
			.extracting(Violation::field, Violation::message)
			.containsExactlyInAnyOrder(tuple("getAllOVKProtocols.propertyDesignation", "must be a property designation(fastighetsbeteckning), e.g. 'TESTÖN 1:123'"));

		verifyNoInteractions(mockByggrIntegratorService);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"TESTÖN",
		"TESTÖN1123",
		"TESTÖN1:123",
		" "
	})
	void getLatestOVKProtocol_invalidPropertyDesignation(final String propertyDesignation) {
		final var response = webTestClient.get()
			.uri(OVK_LATEST_URL, "2281", propertyDesignation)
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations())
			.extracting(Violation::field, Violation::message)
			.containsExactlyInAnyOrder(tuple("getLatestOVKProtocol.propertyDesignation", "must be a property designation(fastighetsbeteckning), e.g. 'TESTÖN 1:123'"));

		verifyNoInteractions(mockByggrIntegratorService);
	}
}
