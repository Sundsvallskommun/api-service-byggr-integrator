package se.sundsvall.byggrintegrator.service;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.byggrintegrator.TestObjectFactory.createRelateradeArendenResponse;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;

import se.sundsvall.byggrintegrator.integration.byggr.ByggrIntegration;
import se.sundsvall.byggrintegrator.integration.byggr.ByggrIntegrationMapper;

import generated.se.sundsvall.arendeexport.GetRelateradeArendenByPersOrgNrAndRoleResponse;
import generated.se.sundsvall.arendeexport.ObjectFactory;

@ExtendWith(MockitoExtension.class)
class ByggrIntegratorServiceTest {

	@Mock
	private ByggrIntegrationMapper mockByggrIntegrationMapper;

	@Mock
	private ByggrIntegration mockByggrIntegration;

	@Mock
	private ApiResponseMapper mockApiResponseMapper;

	@InjectMocks
	private ByggrIntegratorService service;

	private static final ObjectFactory OBJECT_FACTORY = new ObjectFactory();
	private static final List<String> ROLES = List.of("ANM");
	private static final String IDENTIFIER = "1234567890";

	@Test
	void testFindNeighborhoodNotifications() {
		// Arrange
		when(mockByggrIntegration.getRoles()).thenReturn(ROLES);
		when(mockByggrIntegration.getErrandsFromByggr(IDENTIFIER, ROLES)).thenReturn(createRelateradeArendenResponse());
		when(mockByggrIntegrationMapper.mapToNeighborhoodNotificationsDto(any(GetRelateradeArendenByPersOrgNrAndRoleResponse.class))).thenCallRealMethod();
		when(mockApiResponseMapper.mapToKeyValueResponseList(anyList())).thenCallRealMethod();

		// Act
		var neighborNotifications = service.findNeighborhoodNotifications(IDENTIFIER);

		// Assert
		assertThat(neighborNotifications).hasSize(2);
		assertThat(neighborNotifications.getFirst().key()).isEqualTo("BYGG 2024-000123");
		assertThat(neighborNotifications.getFirst().value()).isEqualTo("BYGG 2024-000123, ANKEBORG 1:1234");
		assertThat(neighborNotifications.getLast().key()).isEqualTo("BYGG 2024-000123");
		assertThat(neighborNotifications.getLast().value()).isEqualTo("BYGG 2024-000123, ANKEBORG 2:5678");
		verify(mockByggrIntegration).getRoles();
		verify(mockByggrIntegration).getErrandsFromByggr(IDENTIFIER, ROLES);
		verify(mockByggrIntegrationMapper).mapToNeighborhoodNotificationsDto(any(GetRelateradeArendenByPersOrgNrAndRoleResponse.class));
		verify(mockApiResponseMapper).mapToKeyValueResponseList(anyList());
		verifyNoMoreInteractions(mockByggrIntegration, mockByggrIntegrationMapper, mockApiResponseMapper);
	}

	@Test
	void testFindNeighborhoodNotifications_whenEmpty_shouldReturnEmptyList() {
		// Arrange
		when(mockByggrIntegration.getRoles()).thenReturn(ROLES);
		when(mockByggrIntegration.getErrandsFromByggr(IDENTIFIER, ROLES)).thenReturn(OBJECT_FACTORY.createGetRelateradeArendenByPersOrgNrAndRoleResponse());
		when(mockByggrIntegrationMapper.mapToNeighborhoodNotificationsDto(any(GetRelateradeArendenByPersOrgNrAndRoleResponse.class))).thenCallRealMethod();
		when(mockApiResponseMapper.mapToKeyValueResponseList(anyList())).thenCallRealMethod();

		// Act
		var neighborNotifications = service.findNeighborhoodNotifications(IDENTIFIER);

		// Assert
		assertThat(neighborNotifications).isEmpty();
		verify(mockByggrIntegration).getRoles();
		verify(mockByggrIntegration).getErrandsFromByggr(IDENTIFIER, ROLES);
		verify(mockByggrIntegrationMapper).mapToNeighborhoodNotificationsDto(any(GetRelateradeArendenByPersOrgNrAndRoleResponse.class));
		verify(mockApiResponseMapper).mapToKeyValueResponseList(emptyList());
		verifyNoMoreInteractions(mockByggrIntegration, mockByggrIntegrationMapper, mockApiResponseMapper);
	}

	@Test
	void testFindNeighborhoodNotifications_noRoles_shouldThrow404() {
		// Arrange
		when(mockByggrIntegration.getRoles()).thenReturn(emptyList());

		// Act
		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> service.findNeighborhoodNotifications(IDENTIFIER))
			.satisfies(throwableProblem -> {
				assertThat(throwableProblem.getStatus()).isEqualTo(Status.NOT_FOUND);
				assertThat(throwableProblem.getTitle()).isEqualTo("No roles found");
				assertThat(throwableProblem.getDetail()).isEqualTo("Cannot continue fetching neighborhood notifications");
			});

		verify(mockByggrIntegration).getRoles();
		verifyNoMoreInteractions(mockByggrIntegration);
		verifyNoInteractions(mockByggrIntegrationMapper, mockApiResponseMapper, mockApiResponseMapper);
	}
}
