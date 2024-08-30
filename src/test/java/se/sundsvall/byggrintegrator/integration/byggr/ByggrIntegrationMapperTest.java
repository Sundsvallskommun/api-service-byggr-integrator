package se.sundsvall.byggrintegrator.integration.byggr;


import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.byggrintegrator.TestObjectFactory.BYGGR_ARENDE_NR_1;
import static se.sundsvall.byggrintegrator.TestObjectFactory.HANDELSESLAG_GRASVA;
import static se.sundsvall.byggrintegrator.TestObjectFactory.createRelateradeArendenResponse;

import org.junit.jupiter.api.Test;

import generated.se.sundsvall.arendeexport.StatusFilter;

class ByggrIntegrationMapperTest {

	private final ByggrIntegrationMapper mapper = new ByggrIntegrationMapper();

	@Test
	void testMapToGetRelateradeArendenRequest() {
		// Arrange
		var id = "1234567890";

		// Act
		var request = mapper.mapToGetRelateradeArendenRequest(id);

		// Assert
		assertThat(request.getStatusfilter()).isEqualByComparingTo(StatusFilter.AKTIV);
		assertThat(request.getPersOrgNr()).isEqualTo(id);
	}

	@Test
	void testMapToNeighborhoodNotificationsDto() {
		// Arrange
		var response = createRelateradeArendenResponse();

		// Act
		var neighborNotificationsDtos = mapper.mapToNeighborhoodNotificationsDto(response);

		// Assert
		assertThat(neighborNotificationsDtos).hasSize(1);
		assertThat(neighborNotificationsDtos.getFirst().getByggrErrandNumber()).isEqualTo(BYGGR_ARENDE_NR_1);
		assertThat(neighborNotificationsDtos.getFirst().getPropertyDesignation()).hasSize(2);
		assertThat(neighborNotificationsDtos.getFirst().getPropertyDesignation().getFirst()).satisfies(propertyDesignation -> {
			assertThat(propertyDesignation.getProperty()).isEqualTo("ANKEBORG");
			assertThat(propertyDesignation.getDesignation()).isEqualTo("1:1234");
		});
		assertThat(neighborNotificationsDtos.getFirst().getPropertyDesignation().getLast()).satisfies(propertyDesignation -> {
			assertThat(propertyDesignation.getProperty()).isEqualTo("ANKEBORG");
			assertThat(propertyDesignation.getDesignation()).isEqualTo("2:5678");
		});
	}

	@Test
	void testMapToNeighborhoodNotificationsDto_noValidEvents() {
		// Arrange
		var response = createRelateradeArendenResponse();
		//Set all events to invalid
		response.getGetRelateradeArendenByPersOrgNrAndRoleResult().getArende().getFirst().getHandelseLista().getHandelse().getFirst().setHandelseslag(HANDELSESLAG_GRASVA);

		// Act
		var neighborNotificationsDtos = mapper.mapToNeighborhoodNotificationsDto(response);

		// Assert
		assertThat(neighborNotificationsDtos).isEmpty();
	}
}
