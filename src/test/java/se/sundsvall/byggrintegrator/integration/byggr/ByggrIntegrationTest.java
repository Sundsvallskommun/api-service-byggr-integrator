package se.sundsvall.byggrintegrator.integration.byggr;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.byggrintegrator.TestObjectFactory.createRelateradeArendenResponse;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import generated.se.sundsvall.arendeexport.GetRoller;
import generated.se.sundsvall.arendeexport.ObjectFactory;

@ExtendWith(MockitoExtension.class)
class ByggrIntegrationTest {

	@Mock
	private ByggrClient mockByggrClient;

	@Mock
	private ByggrIntegrationMapper mockByggrIntegrationMapper;

	@InjectMocks
	private ByggrIntegration integration;

	private static final ObjectFactory OBJECT_FACTORY = new ObjectFactory();

	@Test
	void testGetErrandsFromByggr() {
		// Arrange
		var identifier = "1234567890";
		var roles = List.of("ANM");
		when(mockByggrIntegrationMapper.mapToGetRelateradeArendenRequest(identifier)).thenReturn(OBJECT_FACTORY.createGetRelateradeArendenByPersOrgNrAndRole());
		when(mockByggrClient.getRelateradeArendenByPersOrgNrAndRole(any())).thenReturn(createRelateradeArendenResponse());

		// Act
		var errands = integration.getErrandsFromByggr(identifier, roles);

		// Assert
		assertThat(errands).isNotNull();
		assertThat(errands.getGetRelateradeArendenByPersOrgNrAndRoleResult()).isNotNull();
		assertThat(errands.getGetRelateradeArendenByPersOrgNrAndRoleResult().getArende()).isNotEmpty();
		verify(mockByggrIntegrationMapper).mapToGetRelateradeArendenRequest(identifier);
		verify(mockByggrClient).getRelateradeArendenByPersOrgNrAndRole(any());
		verifyNoMoreInteractions(mockByggrIntegrationMapper, mockByggrClient);
	}

	@Test
	void testGetRoles() {
		// Arrange
		var code1 = "ANM";
		var code2 = "ANM2";
		when(mockByggrIntegrationMapper.createGetRolesRequest()).thenReturn(OBJECT_FACTORY.createGetRoller());
		when(mockByggrClient.getRoller(any(GetRoller.class))).thenReturn(OBJECT_FACTORY.createGetRollerResponse()
			.withGetRollerResult(OBJECT_FACTORY.createArrayOfRoll()
				.withRoll(OBJECT_FACTORY.createRoll().withArAktiv(true).withRollKod(code1),
					OBJECT_FACTORY.createRoll().withArAktiv(true).withRollKod(code2))));

		// Act
		var roles = integration.getRoles();

		// Assert
		assertThat(roles).containsExactly(code1, code2);
		verify(mockByggrClient).getRoller(any(GetRoller.class));
		verifyNoMoreInteractions(mockByggrIntegrationMapper, mockByggrClient);
	}

	@Test
	void testGetRoles_emptyResponse_shouldReturnEmptyList() {
		// Arrange
		when(mockByggrIntegrationMapper.createGetRolesRequest()).thenReturn(OBJECT_FACTORY.createGetRoller());
		when(mockByggrClient.getRoller(any())).thenReturn(OBJECT_FACTORY.createGetRollerResponse());

		// Act & Assert
		var roles = integration.getRoles();

		assertThat(roles).isNotNull().isEmpty();

		verify(mockByggrIntegrationMapper).createGetRolesRequest();
		verify(mockByggrClient).getRoller(any(GetRoller.class));
		verifyNoMoreInteractions(mockByggrIntegrationMapper, mockByggrClient);
	}
}
