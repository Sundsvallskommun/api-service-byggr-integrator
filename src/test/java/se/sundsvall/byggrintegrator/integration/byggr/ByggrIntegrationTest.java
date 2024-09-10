package se.sundsvall.byggrintegrator.integration.byggr;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.byggrintegrator.TestObjectFactory.generateRelateradeArendenResponse;

import java.util.List;
import java.util.Locale;
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

import generated.se.sundsvall.arendeexport.GetArende;
import generated.se.sundsvall.arendeexport.GetDocument;
import generated.se.sundsvall.arendeexport.GetRelateradeArendenByPersOrgNrAndRole;
import generated.se.sundsvall.arendeexport.GetRoller;
import generated.se.sundsvall.arendeexport.ObjectFactory;
import jakarta.xml.soap.SOAPConstants;
import jakarta.xml.soap.SOAPFactory;
import jakarta.xml.ws.soap.SOAPFaultException;

@ExtendWith(MockitoExtension.class)
class ByggrIntegrationTest {

	@Mock
	private ByggrClient mockByggrClient;

	@Mock
	private ByggrIntegrationMapper mockByggrIntegrationMapper;

	@Captor
	private ArgumentCaptor<GetRelateradeArendenByPersOrgNrAndRole> getRelateradeArendenByPersOrgNrAndRoleCaptor;

	@Captor
	private ArgumentCaptor<GetArende> getArendeCaptor;

	@Captor
	private ArgumentCaptor<GetDocument> getDocumentCaptor;

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
		final var errands = integration.getErrands(identifier, roles);

		// Assert
		verify(mockByggrIntegrationMapper).mapToGetRelateradeArendenRequest(identifier);
		verify(mockByggrClient).getRelateradeArendenByPersOrgNrAndRole(getRelateradeArendenByPersOrgNrAndRoleCaptor.capture());
		verifyNoMoreInteractions(mockByggrIntegrationMapper, mockByggrClient);

		assertThat(getRelateradeArendenByPersOrgNrAndRoleCaptor.getValue().getPersOrgNr()).isEqualTo(identifier);
		assertThat(getRelateradeArendenByPersOrgNrAndRoleCaptor.getValue().getArendeIntressentRoller()).satisfiesAnyOf(list -> {
			assertThat(role).isNull();
			assertThat(list).isNull();
		}, list -> {
			assertThat(role).isNotNull();
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

	@Test
	void testGetErrand() {
		// Arrange
		final var dnr = "dnr";
		final var response = OBJECT_FACTORY.createGetArendeResponse();

		when(mockByggrIntegrationMapper.mapToGetArendeRequest(dnr)).thenCallRealMethod();
		when(mockByggrClient.getArende(any(GetArende.class))).thenReturn(response);

		// Act
		final var errand = integration.getErrand(dnr);

		// Verify and assert
		verify(mockByggrIntegrationMapper).mapToGetArendeRequest(dnr);
		verify(mockByggrClient).getArende(getArendeCaptor.capture());
		verifyNoMoreInteractions(mockByggrIntegrationMapper, mockByggrClient);

		assertThat(getArendeCaptor.getValue().getDnr()).isEqualTo(dnr);
		assertThat(errand).isEqualTo(response);
	}

	@Test
	void testGetErrand_soapFaultNotFound() throws Exception {
		// Arrange
		final var dnr = "diaryNumber";
		final var soapfault = SOAPFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL).createFault();
		soapfault.addFaultReasonText("Arende not found for dnr diaryNumber", Locale.ENGLISH);

		when(mockByggrIntegrationMapper.mapToGetArendeRequest(dnr)).thenCallRealMethod();
		when(mockByggrClient.getArende(any(GetArende.class))).thenThrow(new SOAPFaultException(soapfault));

		// Act
		assertThat(integration.getErrand(dnr)).isNull();

		// Verify and assert
		verify(mockByggrIntegrationMapper).mapToGetArendeRequest(dnr);
		verify(mockByggrClient).getArende(getArendeCaptor.capture());
		verifyNoMoreInteractions(mockByggrIntegrationMapper, mockByggrClient);

		assertThat(getArendeCaptor.getValue().getDnr()).isEqualTo(dnr);
	}

	@Test
	void testGetErrand_otherSoapFaultThanNotFound() throws Exception {
		// Arrange
		final var dnr = "diaryNumber";
		final var reasonText = "Other reason";
		final var soapfault = SOAPFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL).createFault();
		soapfault.addFaultReasonText(reasonText, Locale.ENGLISH);

		when(mockByggrIntegrationMapper.mapToGetArendeRequest(dnr)).thenCallRealMethod();
		when(mockByggrClient.getArende(any(GetArende.class))).thenThrow(new SOAPFaultException(soapfault));

		// Act
		final var exception = assertThrows(SOAPFaultException.class, () -> integration.getErrand(dnr));

		// Verify and assert
		verify(mockByggrIntegrationMapper).mapToGetArendeRequest(dnr);
		verify(mockByggrClient).getArende(getArendeCaptor.capture());
		verifyNoMoreInteractions(mockByggrIntegrationMapper, mockByggrClient);

		assertThat(getArendeCaptor.getValue().getDnr()).isEqualTo(dnr);
		assertThat(exception).isInstanceOf(SOAPFaultException.class);
		assertThat(exception.getFault().getFaultReasonText(Locale.ENGLISH)).isEqualTo(reasonText);
	}

	@Test
	void testGetDocument() {
		// Arrange
		final var fileId = "fileId";
		final var response = OBJECT_FACTORY.createGetDocumentResponse();

		when(mockByggrIntegrationMapper.mapToGetDocumentRequest(fileId)).thenCallRealMethod();
		when(mockByggrClient.getDocument(any(GetDocument.class))).thenReturn(response);

		// Act
		final var document = integration.getDocument(fileId);

		// Verify and assert
		verify(mockByggrIntegrationMapper).mapToGetDocumentRequest(fileId);
		verify(mockByggrClient).getDocument(getDocumentCaptor.capture());
		verifyNoMoreInteractions(mockByggrIntegrationMapper, mockByggrClient);

		assertThat(getDocumentCaptor.getValue().getDocumentId()).isEqualTo(fileId);
		assertThat(getDocumentCaptor.getValue().isInkluderaFil()).isTrue();
		assertThat(document).isEqualTo(response);
	}

	@ParameterizedTest
	@ValueSource(strings = { "Error getting GemDmsdoclink something something", "Something is not a numeric value" })
	void testGetDocument_soapFaultNotFound(String faultText) throws Exception {
		// Arrange
		final var fileId = "fileId";
		final var soapfault = SOAPFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL).createFault();
		soapfault.addFaultReasonText(faultText, Locale.ENGLISH);

		when(mockByggrIntegrationMapper.mapToGetDocumentRequest(fileId)).thenCallRealMethod();
		when(mockByggrClient.getDocument(any(GetDocument.class))).thenThrow(new SOAPFaultException(soapfault));

		// Act
		assertThat(integration.getDocument(fileId)).isNull();

		// Verify and assert
		verify(mockByggrIntegrationMapper).mapToGetDocumentRequest(fileId);
		verify(mockByggrClient).getDocument(getDocumentCaptor.capture());
		verifyNoMoreInteractions(mockByggrIntegrationMapper, mockByggrClient);

		assertThat(getDocumentCaptor.getValue().getDocumentId()).isEqualTo(fileId);
		assertThat(getDocumentCaptor.getValue().isInkluderaFil()).isTrue();
	}

	@Test
	void testGetDocument_otherSoapFaultThanNotFound() throws Exception {
		// Arrange
		final var fileId = "fileId";
		final var reasonText = "Other reason";
		final var soapfault = SOAPFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL).createFault();
		soapfault.addFaultReasonText(reasonText, Locale.ENGLISH);

		when(mockByggrIntegrationMapper.mapToGetDocumentRequest(fileId)).thenCallRealMethod();
		when(mockByggrClient.getDocument(any(GetDocument.class))).thenThrow(new SOAPFaultException(soapfault));

		// Act
		final var exception = assertThrows(SOAPFaultException.class, () -> integration.getDocument(fileId));

		// Verify and assert
		verify(mockByggrIntegrationMapper).mapToGetDocumentRequest(fileId);
		verify(mockByggrClient).getDocument(getDocumentCaptor.capture());
		verifyNoMoreInteractions(mockByggrIntegrationMapper, mockByggrClient);

		assertThat(getDocumentCaptor.getValue().getDocumentId()).isEqualTo(fileId);
		assertThat(getDocumentCaptor.getValue().isInkluderaFil()).isTrue();
		assertThat(exception).isInstanceOf(SOAPFaultException.class);
		assertThat(exception.getFault().getFaultReasonText(Locale.ENGLISH)).isEqualTo(reasonText);
	}
}
