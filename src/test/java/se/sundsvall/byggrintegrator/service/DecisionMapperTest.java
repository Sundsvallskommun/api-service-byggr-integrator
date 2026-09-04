package se.sundsvall.byggrintegrator.service;

import generated.se.sundsvall.arendeexport.v8.Arende;
import generated.se.sundsvall.arendeexport.v8.ArrayOfHandelse;
import generated.se.sundsvall.arendeexport.v8.Handelse;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.byggrintegrator.api.model.Decision;
import se.sundsvall.byggrintegrator.api.model.DecisionDocument;
import se.sundsvall.byggrintegrator.service.template.FileUrlService;
import se.sundsvall.byggrintegrator.service.util.ByggrFilterProperties;
import se.sundsvall.byggrintegrator.service.util.ByggrFilterProperties.DecisionProperties;
import se.sundsvall.byggrintegrator.service.util.ByggrFilterUtility;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.byggrintegrator.TestObjectFactory.DECISION_DATE;
import static se.sundsvall.byggrintegrator.TestObjectFactory.DECISION_DOCUMENT_DATE;
import static se.sundsvall.byggrintegrator.TestObjectFactory.DECISION_DOCUMENT_DESCRIPTION;
import static se.sundsvall.byggrintegrator.TestObjectFactory.DECISION_DOCUMENT_ID;
import static se.sundsvall.byggrintegrator.TestObjectFactory.DECISION_DOCUMENT_NAME;
import static se.sundsvall.byggrintegrator.TestObjectFactory.DECISION_EVENT_ID;
import static se.sundsvall.byggrintegrator.TestObjectFactory.DECISION_HEADING;
import static se.sundsvall.byggrintegrator.TestObjectFactory.DECISION_INSTANCE_TYPE;
import static se.sundsvall.byggrintegrator.TestObjectFactory.DECISION_NUMBER;
import static se.sundsvall.byggrintegrator.TestObjectFactory.DECISION_OUTCOME;
import static se.sundsvall.byggrintegrator.TestObjectFactory.DECISION_VALID_UNTIL;
import static se.sundsvall.byggrintegrator.TestObjectFactory.FBET_NR_1;
import static se.sundsvall.byggrintegrator.TestObjectFactory.HANDELSETYP_BESLUT;
import static se.sundsvall.byggrintegrator.TestObjectFactory.HANDLINGSTYP_BESLUT;
import static se.sundsvall.byggrintegrator.TestObjectFactory.OLDER_DECISION_DATE;
import static se.sundsvall.byggrintegrator.TestObjectFactory.OLDER_DECISION_DOCUMENT_ID;
import static se.sundsvall.byggrintegrator.TestObjectFactory.OLDER_DECISION_EVENT_ID;
import static se.sundsvall.byggrintegrator.TestObjectFactory.createDecisionEvents;
import static se.sundsvall.byggrintegrator.TestObjectFactory.generateArendeResponseWithDecisions;

@ExtendWith(MockitoExtension.class)
class DecisionMapperTest {

	private static final String MUNICIPALITY_ID = "2281";
	private static final String DNR = "BYGG 2024-000123";
	private static final String URL_TEMPLATE = "https://somewhere.com/2281/files/%s?token=token";
	private static final Map<String, String> HANDLINGTYPER = Map.of(HANDLINGSTYP_BESLUT, "Beslut");

	@Mock
	private FileUrlService mockFileUrlService;

	private ByggrFilterUtility filterUtility;
	private DecisionMapper mapper;

	@BeforeEach
	void setUp() {
		filterUtility = new ByggrFilterUtility(new ByggrFilterProperties(null, null, null, new DecisionProperties(List.of(HANDELSETYP_BESLUT), List.of(HANDLINGSTYP_BESLUT))));
		mapper = new DecisionMapper(mockFileUrlService, filterUtility);
	}

	/**
	 * The mapper expects the events to already be filtered to decision events (the service uses the filter utility for
	 * that), hence the events are filtered the same way here
	 */
	private List<Handelse> createFilteredDecisionEvents() throws Exception {
		return filterUtility.filterDecisionEvents(new Arende().withHandelseLista(new ArrayOfHandelse().withHandelse(createDecisionEvents())));
	}

	@Test
	void toErrandDecisions() throws Exception {
		// Arrange
		final var errand = generateArendeResponseWithDecisions(DNR).getGetArendeResult().withBeskrivning("description");
		final var decisionEvents = createFilteredDecisionEvents();
		when(mockFileUrlService.parseFileUrl(eq(MUNICIPALITY_ID), anyInt())).thenAnswer(invocation -> URL_TEMPLATE.formatted(invocation.getArgument(1, Integer.class)));

		// Act
		final var result = mapper.toErrandDecisions(MUNICIPALITY_ID, errand, decisionEvents, HANDLINGTYPER);

		// Assert
		assertThat(result.caseNumber()).isEqualTo(DNR);
		assertThat(result.description()).isEqualTo("description");
		assertThat(result.propertyDesignation()).isEqualTo("ANKEBORG " + FBET_NR_1);
		assertThat(result.decisions()).hasSize(2).satisfiesExactly(decision -> {
			assertThat(decision.id()).isEqualTo(DECISION_EVENT_ID);
			assertThat(decision.decisionNumber()).isEqualTo(DECISION_NUMBER);
			assertThat(decision.heading()).isEqualTo(DECISION_HEADING);
			assertThat(decision.decisionDate()).isEqualTo(DECISION_DATE);
			assertThat(decision.outcome()).isEqualTo(DECISION_OUTCOME);
			assertThat(decision.mainDecision()).isTrue();
			assertThat(decision.instanceType()).isEqualTo(DECISION_INSTANCE_TYPE);
			assertThat(decision.validUntil()).isEqualTo(DECISION_VALID_UNTIL);
			assertThat(decision.documents()).containsExactly(new DecisionDocument(
				DECISION_DOCUMENT_ID,
				DECISION_DOCUMENT_NAME,
				DECISION_DOCUMENT_DESCRIPTION,
				HANDLINGSTYP_BESLUT,
				"Beslut",
				DECISION_DOCUMENT_DATE,
				URL_TEMPLATE.formatted(DECISION_DOCUMENT_ID)));
		}, decision -> {
			assertThat(decision.id()).isEqualTo(OLDER_DECISION_EVENT_ID);
			assertThat(decision.decisionNumber()).isNull();
			assertThat(decision.decisionDate()).isEqualTo(OLDER_DECISION_DATE);
			assertThat(decision.mainDecision()).isFalse();
			assertThat(decision.instanceType()).isNull();
			assertThat(decision.validUntil()).isNull();
			assertThat(decision.documents()).extracting(DecisionDocument::id, DecisionDocument::url)
				.containsExactly(tuple(OLDER_DECISION_DOCUMENT_ID, URL_TEMPLATE.formatted(OLDER_DECISION_DOCUMENT_ID)));
		});

		verify(mockFileUrlService).parseFileUrl(MUNICIPALITY_ID, Integer.parseInt(DECISION_DOCUMENT_ID));
		verify(mockFileUrlService).parseFileUrl(MUNICIPALITY_ID, Integer.parseInt(OLDER_DECISION_DOCUMENT_ID));
		verifyNoMoreInteractions(mockFileUrlService);
	}

	@Test
	void toErrandDecisionsWithoutTypeDescriptions() throws Exception {
		// Arrange
		final var errand = new Arende().withDnr(DNR);
		final var decisionEvents = createFilteredDecisionEvents();
		when(mockFileUrlService.parseFileUrl(eq(MUNICIPALITY_ID), anyInt())).thenReturn("url");

		// Act
		final var result = mapper.toErrandDecisions(MUNICIPALITY_ID, errand, decisionEvents, null);

		// Assert
		assertThat(result.caseNumber()).isEqualTo(DNR);
		assertThat(result.description()).isNull();
		assertThat(result.propertyDesignation()).isNull();
		assertThat(result.decisions()).hasSize(2)
			.flatExtracting(Decision::documents)
			.extracting(DecisionDocument::typeDescription, DecisionDocument::url)
			.containsOnly(tuple(null, "url"));
	}

	@Test
	void toErrandDecisionsWithoutDecisionEvents() {
		// Arrange
		final var errand = new Arende().withDnr(DNR);

		// Act
		final var result = mapper.toErrandDecisions(MUNICIPALITY_ID, errand, null, HANDLINGTYPER);

		// Assert
		assertThat(result.caseNumber()).isEqualTo(DNR);
		assertThat(result.decisions()).isEmpty();
		verifyNoInteractions(mockFileUrlService);
	}

	@Test
	void toErrandDecisionsWithEventWithoutDocuments() {
		// Arrange
		final var errand = new Arende().withDnr(DNR);
		final var events = List.of(new Handelse().withHandelseId(1).withHandelsetyp(HANDELSETYP_BESLUT));

		// Act
		final var result = mapper.toErrandDecisions(MUNICIPALITY_ID, errand, events, HANDLINGTYPER);

		// Assert
		assertThat(result.decisions()).isEmpty();
		verifyNoInteractions(mockFileUrlService);
	}
}
