package se.sundsvall.byggrintegrator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import se.sundsvall.byggrintegrator.model.ByggrErrandDto;
import se.sundsvall.byggrintegrator.model.ByggrErrandDto.Event;

import generated.se.sundsvall.arendeexport.AbstractArendeObjekt;
import generated.se.sundsvall.arendeexport.Arende;
import generated.se.sundsvall.arendeexport.ArrayOfAbstractArendeObjekt2;
import generated.se.sundsvall.arendeexport.ArrayOfArende1;
import generated.se.sundsvall.arendeexport.ArrayOfHandelse;
import generated.se.sundsvall.arendeexport.ArrayOfHandelseHandling;
import generated.se.sundsvall.arendeexport.Fastighet;
import generated.se.sundsvall.arendeexport.GetArendeResponse;
import generated.se.sundsvall.arendeexport.GetDocumentResponse;
import generated.se.sundsvall.arendeexport.GetRelateradeArendenByPersOrgNrAndRoleResponse;
import generated.se.sundsvall.arendeexport.ObjectFactory;

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
	public static final String UNWANTED_DOKUMENT_TYPE_ANM = "ANM";
	public static final String UNWANTED_DOKUMENT_TYPE_ANS = "ANS";
	public static final String WANTED_DOKUMENT_TYPE = "WANTED";
	public static final String WANTED_DOCUMENT_NAME = "wantedDocumentName";
	public static final String WANTED_DOCUMENT_ID = "wantedDocumentId";

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
	 * @return A populated response
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
			.withHandling(OBJECT_FACTORY.createHandelseHandling()
				.withTyp(UNWANTED_DOKUMENT_TYPE_ANS)
				.withDokument(OBJECT_FACTORY.createDokument()
					.withDokId("documentId2")
					.withNamn("documentName2")))
			.withHandling(OBJECT_FACTORY.createHandelseHandling()
				.withTyp(UNWANTED_DOKUMENT_TYPE_ANM)
				.withDokument(OBJECT_FACTORY.createDokument()
					.withDokId("documentId3")
					.withNamn("documentName4")));
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
					Event.builder().withId(234).build()))
				.build(),
			ByggrErrandDto.builder()
				.withByggrCaseNumber("dnr456")
				.withEvents(List.of(
					Event.builder().withId(345).build(),
					Event.builder().withId(456).build()))
				.build());
	}
}
