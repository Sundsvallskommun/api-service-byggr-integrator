package se.sundsvall.byggrintegrator;

import generated.se.sundsvall.arendeexport.v8.AbstractArendeObjekt;
import generated.se.sundsvall.arendeexport.v8.Arende;
import generated.se.sundsvall.arendeexport.v8.ArrayOfAbstractArendeObjekt2;
import generated.se.sundsvall.arendeexport.v8.ArrayOfArende1;
import generated.se.sundsvall.arendeexport.v8.ArrayOfHandelse;
import generated.se.sundsvall.arendeexport.v8.ArrayOfHandelseHandling;
import generated.se.sundsvall.arendeexport.v8.Beslut;
import generated.se.sundsvall.arendeexport.v8.Fastighet;
import generated.se.sundsvall.arendeexport.v8.GetArendeResponse;
import generated.se.sundsvall.arendeexport.v8.GetDocumentResponse;
import generated.se.sundsvall.arendeexport.v8.GetRelateradeArendenByPersOrgNrAndRoleResponse;
import generated.se.sundsvall.arendeexport.v8.Handelse;
import generated.se.sundsvall.arendeexport.v8.HandelseHandling;
import generated.se.sundsvall.arendeexport.v8.ObjectFactory;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import se.sundsvall.byggrintegrator.model.ByggrErrandDto;
import se.sundsvall.byggrintegrator.model.ByggrErrandDto.Event;

public final class TestObjectFactory {

	private static final ObjectFactory OBJECT_FACTORY = new ObjectFactory();

	public static final String CASE_APPLICANT = "errandApplicant";
	public static final String NEIGHBORHOOD_NOTIFICATION_STAKEHOLDER = "neighborhoodNotificationStakeholder";
	public static final String APPLICANT_ROLE = "SOK";
	public static final String FBET_NR_1 = "1:1234";
	public static final String FBET_NR_2 = "2:5678";
	public static final String HANDELSETYP_GRANHO = "GRANHO";
	public static final String HANDELSESLAG_GRASVA = "GRASVA";
	public static final String HANDELSESLAG_GRAUTS = "GRAUTS";
	public static final String ARENDE_TYP_BL = "BL";
	public static final String BYGGR_ARENDE_NR_1 = "BYGG 2024-000123";
	public static final String BYGGR_ARENDE_NR_2 = "BYGG 2024-000234";
	public static final String DOCUMENT_FILE_NAME = "random.txt";
	public static final byte[] DOCUMENT_CONTENT = "Some not so interesting text".getBytes();
	public static final String UNWANTED_DOCUMENT_TYPE_GRA = "GRA";
	public static final String UNWANTED_DOCUMENT_TYPE_UNDUT = "UNDUT";
	public static final String WANTED_DOKUMENT_TYPE = "WANTED";
	public static final String WANTED_DOCUMENT_NAME = "wantedDocumentName";
	public static final String WANTED_DOCUMENT_ID = "wantedDocumentId";
	public static final String HANDELSETYP_BESLUT = "BESLUT";
	public static final String HANDLINGSTYP_BESLUT = "BESLUT";
	public static final String HANDLINGSTYP_ANSOKAN = "ANSS";
	public static final int DECISION_EVENT_ID = 10;
	public static final int OLDER_DECISION_EVENT_ID = 14;
	public static final String DECISION_NUMBER = "SBN 2024-000002";
	public static final String DECISION_HEADING = "Bygglov, Beviljas";
	public static final String DECISION_OUTCOME = "BEV";
	public static final String DECISION_INSTANCE_TYPE = "DelgSBN";
	public static final LocalDate DECISION_DATE = LocalDate.of(2024, Month.SEPTEMBER, 2);
	public static final LocalDate DECISION_VALID_UNTIL = LocalDate.of(2029, Month.SEPTEMBER, 2);
	public static final LocalDate OLDER_DECISION_DATE = LocalDate.of(2024, Month.AUGUST, 1);
	public static final LocalDate DECISION_DOCUMENT_DATE = LocalDate.of(2024, Month.SEPTEMBER, 3);
	public static final String DECISION_DOCUMENT_ID = "470583";
	public static final String DECISION_DOCUMENT_NAME = "beslut.pdf";
	public static final String DECISION_DOCUMENT_DESCRIPTION = "Beslut";
	public static final String OLDER_DECISION_DOCUMENT_ID = "470590";
	public static final String CANCELLED_DECISION_DOCUMENT_ID = "470584";
	public static final String NON_NUMERIC_DECISION_DOCUMENT_ID = "ABC123";

	/**
	 * Creates a response with one valid and one invalid event
	 *
	 * @return A populated response
	 */

	public static GetRelateradeArendenByPersOrgNrAndRoleResponse generateRelateradeArendenResponse() throws Exception {
		return generateRelateradeArendenResponse(CASE_APPLICANT, NEIGHBORHOOD_NOTIFICATION_STAKEHOLDER);
	}

	/**
	 * Creates a response with one valid and one invalid event
	 *
	 * @return                                A populated response
	 * @throws DatatypeConfigurationException
	 */
	public static GetRelateradeArendenByPersOrgNrAndRoleResponse generateRelateradeArendenResponse(String caseApplicant,
		String neighborhoodStakeholder) throws Exception {
		final var response = OBJECT_FACTORY.createGetRelateradeArendenByPersOrgNrAndRoleResponse();
		final var arendeList = List.of(createArende(BYGGR_ARENDE_NR_1, true, caseApplicant, neighborhoodStakeholder),
			createArende(BYGGR_ARENDE_NR_2, false, caseApplicant, neighborhoodStakeholder));
		return response.withGetRelateradeArendenByPersOrgNrAndRoleResult(new ArrayOfArende1().withArende(arendeList));
	}

	public static GetArendeResponse generateArendeResponse(String dnr) throws Exception {
		final var response = OBJECT_FACTORY.createGetArendeResponse();
		return response.withGetArendeResult(createArende(dnr, true, CASE_APPLICANT, NEIGHBORHOOD_NOTIFICATION_STAKEHOLDER));
	}

	/**
	 * Creates a response for an errand with the standard events plus a set of decision events (see createDecisionEvents)
	 *
	 * @param  dnr The diary number of the errand
	 * @return     A populated response
	 */
	public static GetArendeResponse generateArendeResponseWithDecisions(String dnr) throws Exception {
		final var arende = createArende(dnr, true, CASE_APPLICANT, NEIGHBORHOOD_NOTIFICATION_STAKEHOLDER);
		arende.getHandelseLista().getHandelse().addAll(createDecisionEvents());

		return OBJECT_FACTORY.createGetArendeResponse().withGetArendeResult(arende);
	}

	/**
	 * Creates decision events covering the filtering cases:
	 * <ul>
	 * <li>Event 10: valid decision (main decision) with one valid decision document (plus a duplicate of it), one
	 * non-decision document, one cancelled decision document, one decision document with non-numeric id and one decision
	 * document without document</li>
	 * <li>Event 11: cancelled decision event</li>
	 * <li>Event 12: secret decision event</li>
	 * <li>Event 13: decision event without any decision documents</li>
	 * <li>Event 14: older valid decision (not main decision, without decision element) with one valid decision
	 * document</li>
	 * </ul>
	 *
	 * @return A list with decision events
	 */
	public static List<Handelse> createDecisionEvents() throws Exception {
		final var validDecision = createDecisionEvent(DECISION_EVENT_ID, DECISION_DATE)
			.withBeslut(new Beslut()
				.withBeslutNr(DECISION_NUMBER)
				.withArHuvudbeslut(true)
				.withInstanstyp(DECISION_INSTANCE_TYPE)
				.withGiltigTillDatum(toXmlGregorianCalendar(DECISION_VALID_UNTIL)))
			.withHandlingLista(OBJECT_FACTORY.createArrayOfHandelseHandling()
				.withHandling(createDecisionDocument(HANDLINGSTYP_BESLUT, DECISION_DOCUMENT_ID, DECISION_DOCUMENT_NAME, false))
				.withHandling(createDecisionDocument(HANDLINGSTYP_BESLUT, DECISION_DOCUMENT_ID, DECISION_DOCUMENT_NAME, false)) // Duplicate to verify that only one remains
				.withHandling(createDecisionDocument(HANDLINGSTYP_ANSOKAN, "468545", "ansokan.pdf", false))
				.withHandling(createDecisionDocument(HANDLINGSTYP_BESLUT, CANCELLED_DECISION_DOCUMENT_ID, "makulerat.pdf", true))
				.withHandling(createDecisionDocument(HANDLINGSTYP_BESLUT, NON_NUMERIC_DECISION_DOCUMENT_ID, "nonnumeric.pdf", false))
				.withHandling(OBJECT_FACTORY.createHandelseHandling().withTyp(HANDLINGSTYP_BESLUT).withMakulerad(false)));

		final var cancelledDecision = createDecisionEvent(11, DECISION_DATE)
			.withMakulerad(true)
			.withHandlingLista(OBJECT_FACTORY.createArrayOfHandelseHandling()
				.withHandling(createDecisionDocument(HANDLINGSTYP_BESLUT, "470585", "makulerad-handelse.pdf", false)));

		final var secretDecision = createDecisionEvent(12, DECISION_DATE)
			.withSekretess(true)
			.withHandlingLista(OBJECT_FACTORY.createArrayOfHandelseHandling()
				.withHandling(createDecisionDocument(HANDLINGSTYP_BESLUT, "470586", "sekretess.pdf", false)));

		final var decisionWithoutDocuments = createDecisionEvent(13, DECISION_DATE)
			.withHandlingLista(OBJECT_FACTORY.createArrayOfHandelseHandling()
				.withHandling(createDecisionDocument(HANDLINGSTYP_ANSOKAN, "468546", "ansokan2.pdf", false)));

		final var olderDecision = createDecisionEvent(OLDER_DECISION_EVENT_ID, OLDER_DECISION_DATE)
			.withHandlingLista(OBJECT_FACTORY.createArrayOfHandelseHandling()
				.withHandling(createDecisionDocument(HANDLINGSTYP_BESLUT, OLDER_DECISION_DOCUMENT_ID, "aldre-beslut.pdf", false)));

		return List.of(validDecision, cancelledDecision, secretDecision, decisionWithoutDocuments, olderDecision);
	}

	private static Handelse createDecisionEvent(int id, LocalDate date) throws Exception {
		return OBJECT_FACTORY.createHandelse()
			.withHandelseId(id)
			.withHandelsetyp(HANDELSETYP_BESLUT)
			.withHandelseslag("BEV")
			.withHandelseutfall(DECISION_OUTCOME)
			.withRubrik(DECISION_HEADING)
			.withStartDatum(toXmlGregorianCalendar(date))
			.withMakulerad(false)
			.withSekretess(false);
	}

	private static HandelseHandling createDecisionDocument(String type, String documentId, String documentName, boolean cancelled) throws Exception {
		return OBJECT_FACTORY.createHandelseHandling()
			.withTyp(type)
			.withMakulerad(cancelled)
			.withHandlingDatum(toXmlGregorianCalendar(DECISION_DOCUMENT_DATE))
			.withDokument(OBJECT_FACTORY.createDokument()
				.withDokId(documentId)
				.withNamn(documentName)
				.withBeskrivning(DECISION_DOCUMENT_DESCRIPTION));
	}

	private static XMLGregorianCalendar toXmlGregorianCalendar(LocalDate date) throws Exception {
		return DatatypeFactory.newInstance().newXMLGregorianCalendar(date.toString());
	}

	public static GetDocumentResponse generateDocumentResponse(String fileId) {
		final var response = OBJECT_FACTORY.createGetDocumentResponse();
		final var dokument = OBJECT_FACTORY.createDokument()
			.withDokId(fileId)
			.withNamn(DOCUMENT_FILE_NAME)
			.withFil(OBJECT_FACTORY.createDokumentFil()
				.withFilBuffer(DOCUMENT_CONTENT));

		return response.withGetDocumentResult(dokument);
	}

	public static GetRelateradeArendenByPersOrgNrAndRoleResponse generateEmptyRelateradeArendenResponse() {
		return OBJECT_FACTORY.createGetRelateradeArendenByPersOrgNrAndRoleResponse();
	}

	public static Arende createArende(String dnr, boolean validEvents) throws Exception {
		return createArende(dnr, validEvents, CASE_APPLICANT, NEIGHBORHOOD_NOTIFICATION_STAKEHOLDER);
	}

	public static Arende createArende(String dnr, boolean validEvents, String errandApplicant, String neighborhoodStakeholder) throws Exception {
		return OBJECT_FACTORY.createArende()
			.withDnr(dnr)
			.withIntressentLista(OBJECT_FACTORY.createArrayOfArendeIntressent2().withIntressent(
				OBJECT_FACTORY.createArendeIntressent()
					.withPersOrgNr(errandApplicant)
					.withRollLista(
						OBJECT_FACTORY.createArrayOfString2().withRoll(APPLICANT_ROLE))))
			.withArendetyp(ARENDE_TYP_BL)
			.withHandelseLista(createArrayOfHandelse(validEvents, neighborhoodStakeholder))
			.withObjektLista(createObjektLista());
	}

	private static ArrayOfHandelse createArrayOfHandelse(boolean validEvents, String neighborhoodStakeholder) throws Exception {
		final var handelse1 = OBJECT_FACTORY.createHandelse()
			.withHandelseId(1)
			.withHandelsetyp(HANDELSETYP_GRANHO)
			// If validEvents is true, we set a valid handelseslag else a invalid one
			.withHandelseslag(validEvents ? HANDELSESLAG_GRAUTS : HANDELSESLAG_GRASVA)
			.withStartDatum(DatatypeFactory.newInstance().newXMLGregorianCalendar(LocalDate.now().toString()))
			.withIntressentLista(OBJECT_FACTORY.createArrayOfHandelseIntressent2().withIntressent(
				OBJECT_FACTORY.createHandelseIntressent()
					.withPersOrgNr(neighborhoodStakeholder)))
			.withHandlingLista(createArrayOfHandling());

		final var handelse2 = OBJECT_FACTORY.createHandelse()
			.withHandelseId(2)
			.withHandelsetyp(HANDELSETYP_GRANHO)
			.withHandelseslag(HANDELSESLAG_GRAUTS)
			.withStartDatum(DatatypeFactory.newInstance().newXMLGregorianCalendar(LocalDate.now().toString()))
			.withIntressentLista(OBJECT_FACTORY.createArrayOfHandelseIntressent2().withIntressent(
				OBJECT_FACTORY.createHandelseIntressent()
					.withPersOrgNr(neighborhoodStakeholder)));

		final var arrayOfHandelse = OBJECT_FACTORY.createArrayOfHandelse();
		return arrayOfHandelse.withHandelse(handelse1, handelse2);
	}

	private static ArrayOfHandelseHandling createArrayOfHandling() {
		final var arrayOfHandelseHandling = OBJECT_FACTORY.createArrayOfHandelseHandling();
		return arrayOfHandelseHandling
			.withHandling(OBJECT_FACTORY.createHandelseHandling()
				.withTyp(WANTED_DOKUMENT_TYPE)
				.withDokument(OBJECT_FACTORY.createDokument()
					.withDokId(WANTED_DOCUMENT_ID)
					.withNamn(WANTED_DOCUMENT_NAME)))
			.withHandling(OBJECT_FACTORY.createHandelseHandling() // Create a handling with a duplicate of document, to verify that only one remains after mapping
				.withTyp(WANTED_DOKUMENT_TYPE)
				.withDokument(OBJECT_FACTORY.createDokument()
					.withDokId(WANTED_DOCUMENT_ID)
					.withNamn(WANTED_DOCUMENT_NAME)))
			.withHandling(OBJECT_FACTORY.createHandelseHandling()
				.withTyp(UNWANTED_DOCUMENT_TYPE_GRA)
				.withDokument(OBJECT_FACTORY.createDokument()
					.withDokId("documentId2")
					.withNamn("documentName2")))
			.withHandling(OBJECT_FACTORY.createHandelseHandling()
				.withTyp(UNWANTED_DOCUMENT_TYPE_UNDUT)
				.withDokument(OBJECT_FACTORY.createDokument()
					.withDokId("documentId3")
					.withNamn("documentName3")));
	}

	private static ArrayOfAbstractArendeObjekt2 createObjektLista() {
		final var objekt2 = OBJECT_FACTORY.createArrayOfAbstractArendeObjekt2();
		final List<AbstractArendeObjekt> abstractArendeObjektList = new ArrayList<>();
		abstractArendeObjektList.add(createAbstractArendeObjekt(FBET_NR_1));
		abstractArendeObjektList.add(createAbstractArendeObjekt(FBET_NR_2));
		return objekt2.withAbstractArendeObjekt(abstractArendeObjektList);
	}

	private static AbstractArendeObjekt createAbstractArendeObjekt(String fbetNr) {
		final var arendeFastighet = OBJECT_FACTORY.createArendeFastighet();
		arendeFastighet.setFastighet(createFastighet(fbetNr));

		return arendeFastighet;
	}

	private static Fastighet createFastighet(String fbetNr) {
		final var fastighet = OBJECT_FACTORY.createFastighet();
		fastighet.setTrakt("ANKEBORG");
		fastighet.setFbetNr(fbetNr);

		return fastighet;
	}

	public static List<ByggrErrandDto> generateByggrErrandDtos() {
		return List.of(
			ByggrErrandDto.builder()
				.withByggrCaseNumber("dnr123")
				.withEvents(List.of(
					Event.builder().withId(123).build(),
					Event.builder().withId(234).build())).build(),
			ByggrErrandDto.builder()
				.withByggrCaseNumber("dnr456")
				.withEvents(List.of(
					Event.builder().withId(345).build(),
					Event.builder().withId(456).build())).build());
	}
}
