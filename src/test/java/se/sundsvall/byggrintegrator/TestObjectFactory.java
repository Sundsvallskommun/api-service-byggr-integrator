package se.sundsvall.byggrintegrator;

import java.util.ArrayList;
import java.util.List;

import se.sundsvall.byggrintegrator.model.NeighborhoodNotificationsDto;

import generated.se.sundsvall.arendeexport.AbstractArendeObjekt;
import generated.se.sundsvall.arendeexport.Arende;
import generated.se.sundsvall.arendeexport.ArrayOfAbstractArendeObjekt2;
import generated.se.sundsvall.arendeexport.ArrayOfArende1;
import generated.se.sundsvall.arendeexport.ArrayOfHandelse;
import generated.se.sundsvall.arendeexport.Fastighet;
import generated.se.sundsvall.arendeexport.GetRelateradeArendenByPersOrgNrAndRoleResponse;
import generated.se.sundsvall.arendeexport.ObjectFactory;

public final class TestObjectFactory {

	private static final ObjectFactory OBJECT_FACTORY = new ObjectFactory();

	public static final String FBET_NR_1 = "1:1234";
	public static final String FBET_NR_2 = "2:5678";
	public static final String HANDELSETYP_GRANHO = "GRANHO";
	public static final String HANDELSESLAG_GRASVA = "GRASVA";
	public static final String HANDELSESLAG_GRAUTS = "GRAUTS";
	public static final String BYGGR_ARENDE_NR_1 = "BYGG 2024-000123";
	public static final String BYGGR_ARENDE_NR_2 = "BYGG 2024-000234";

	/**
	 * Creates a response with one valid and one invalid event
	 *
	 * @return A populated response
	 */
	public static GetRelateradeArendenByPersOrgNrAndRoleResponse createRelateradeArendenResponse() {
		var response = OBJECT_FACTORY.createGetRelateradeArendenByPersOrgNrAndRoleResponse();
		var arendeList = List.of(createArende(BYGGR_ARENDE_NR_1, true), createArende(BYGGR_ARENDE_NR_2, false));
		response.withGetRelateradeArendenByPersOrgNrAndRoleResult(new ArrayOfArende1().withArende(arendeList));
		return response;
	}

	private static Arende createArende(String dnr, boolean validEvents) {
		var arende = OBJECT_FACTORY.createArende();
		arende.setDnr(dnr);
		arende.setHandelseLista(createArrayOfHandelse(validEvents));
		arende.setObjektLista(createObjektLista());
		return arende;
	}

	private static ArrayOfHandelse createArrayOfHandelse(boolean validEvents) {
		var handelse1 = OBJECT_FACTORY.createHandelse();
		handelse1.setHandelseId(1);
		handelse1.setHandelsetyp(HANDELSETYP_GRANHO);

		// If validEvents is true, we set a valid handelseslag
		if(validEvents) {
			handelse1.setHandelseslag(HANDELSESLAG_GRAUTS);
		} else {
			handelse1.setHandelseslag(HANDELSESLAG_GRASVA);
		}

		var handelse2 = OBJECT_FACTORY.createHandelse();
		handelse2.setHandelseId(2);
		handelse2.setHandelsetyp(HANDELSETYP_GRANHO);
		handelse2.setHandelseslag(HANDELSESLAG_GRAUTS);

		var arrayOfHandelse = OBJECT_FACTORY.createArrayOfHandelse();
		arrayOfHandelse.withHandelse(handelse1, handelse2);

		return arrayOfHandelse;
	}

	private static ArrayOfAbstractArendeObjekt2 createObjektLista() {
		var objekt2 = OBJECT_FACTORY.createArrayOfAbstractArendeObjekt2();
		List<AbstractArendeObjekt> abstractArendeObjektList = new ArrayList<>();
		abstractArendeObjektList.add(createAbstractArendeObjekt(FBET_NR_1));
		abstractArendeObjektList.add(createAbstractArendeObjekt(FBET_NR_2));
		objekt2.withAbstractArendeObjekt(abstractArendeObjektList);

		return objekt2;
	}

	private static AbstractArendeObjekt createAbstractArendeObjekt(String fbetNr) {
		var arendeFastighet = OBJECT_FACTORY.createArendeFastighet();
		arendeFastighet.setFastighet(createFastighet(fbetNr));

		return arendeFastighet;
	}

	private static Fastighet createFastighet(String fbetNr) {
		var fastighet = OBJECT_FACTORY.createFastighet();
		fastighet.setTrakt("ANKEBORG");
		fastighet.setFbetNr(fbetNr);

		return fastighet;
	}

	public static List<NeighborhoodNotificationsDto> generateNeighborNotificationsDtos() {
		return List.of(
			NeighborhoodNotificationsDto.builder()
				.withByggrErrandNumber("dnr123")
				.withPropertyDesignation(List.of(
					NeighborhoodNotificationsDto.PropertyDesignation.builder().withProperty("des-1").withDesignation("type1").build(),
					NeighborhoodNotificationsDto.PropertyDesignation.builder().withProperty("des-2").withDesignation("type2").build()
				))
				.build(),
			NeighborhoodNotificationsDto.builder()
				.withByggrErrandNumber("dnr456")
				.withPropertyDesignation(List.of(
					NeighborhoodNotificationsDto.PropertyDesignation.builder().withProperty("des-3").withDesignation("type3").build(),
					NeighborhoodNotificationsDto.PropertyDesignation.builder().withProperty("des-4").withDesignation("type4").build()
				))
				.build()
		);
	}
}
