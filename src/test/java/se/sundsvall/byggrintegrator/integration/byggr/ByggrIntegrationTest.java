package se.sundsvall.byggrintegrator.integration.byggr;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.byggrintegrator.TestObjectFactory.generateRelateradeArendenResponse;

import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import generated.se.sundsvall.arendeexport.GetRelateradeArendenByPersOrgNrAndRole;
import generated.se.sundsvall.arendeexport.GetRoller;
import generated.se.sundsvall.arendeexport.ObjectFactory;

@ExtendWith(MockitoExtension.class)
class ByggrIntegrationTest {

	@Mock
	private ByggrClient mockByggrClient;

	@Mock
	private ByggrIntegrationMapper mockByggrIntegrationMapper;

	@Captor
	private ArgumentCaptor<GetRelateradeArendenByPersOrgNrAndRole> getRelateradeArendenByPersOrgNrAndRoleCaptor;

	@InjectMocks
	private ByggrIntegration integration;

	private static final ObjectFactory OBJECT_FACTORY = new ObjectFactory();

	@ParameterizedTest
	@ValueSource(strings = { "ANM" })
	@NullSource
	void testGetErrandsFromByggr(String role) {
		// Arrange
		final var identifier = "1234567890";
		final var roles = Objects.isNull(role) ? null : List.of(role);
		when(mockByggrIntegrationMapper.mapToGetRelateradeArendenRequest(identifier)).thenCallRealMethod();
		when(mockByggrClient.getRelateradeArendenByPersOrgNrAndRole(any())).thenReturn(generateRelateradeArendenResponse());

		// Act
		final var errands = integration.getErrandsFromByggr(identifier, roles);

		// Assert
		verify(mockByggrIntegrationMapper).mapToGetRelateradeArendenRequest(identifier);
		verify(mockByggrClient).getRelateradeArendenByPersOrgNrAndRole(getRelateradeArendenByPersOrgNrAndRoleCaptor.capture());
		verifyNoMoreInteractions(mockByggrIntegrationMapper, mockByggrClient);

		assertThat(getRelateradeArendenByPersOrgNrAndRoleCaptor.getValue().getPersOrgNr()).isEqualTo(identifier);
		assertThat(getRelateradeArendenByPersOrgNrAndRoleCaptor.getValue().getArendeIntressentRoller()).satisfiesAnyOf(list -> {
			assertThat(list).isNull();
		}, list -> {
			assertThat(list.getString()).hasSize(1);
			assertThat(list.getString()).isEqualTo(roles);
		});
		assertThat(errands).isNotNull();
		assertThat(errands.getGetRelateradeArendenByPersOrgNrAndRoleResult()).isNotNull();
		assertThat(errands.getGetRelateradeArendenByPersOrgNrAndRoleResult().getArende()).isNotEmpty();
	}

	@Test
	void testGetRoles() {
		// Arrange
		final var code1 = "ANM";
		final var code2 = "ANM2";
		when(mockByggrIntegrationMapper.createGetRolesRequest()).thenReturn(OBJECT_FACTORY.createGetRoller());
		when(mockByggrClient.getRoller(any(GetRoller.class))).thenReturn(OBJECT_FACTORY.createGetRollerResponse()
			.withGetRollerResult(OBJECT_FACTORY.createArrayOfRoll()
				.withRoll(OBJECT_FACTORY.createRoll().withArAktiv(true).withRollKod(code1),
					OBJECT_FACTORY.createRoll().withArAktiv(true).withRollKod(code2))));

		// Act
		final var roles = integration.getRoles();

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
		final var roles = integration.getRoles();

		assertThat(roles).isNotNull().isEmpty();

		verify(mockByggrIntegrationMapper).createGetRolesRequest();
		verify(mockByggrClient).getRoller(any(GetRoller.class));
		verifyNoMoreInteractions(mockByggrIntegrationMapper, mockByggrClient);
	}
}
