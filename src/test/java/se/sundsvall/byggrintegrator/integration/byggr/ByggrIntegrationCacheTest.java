package se.sundsvall.byggrintegrator.integration.byggr;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import se.sundsvall.byggrintegrator.Application;

import generated.se.sundsvall.arendeexport.ArrayOfRoll;
import generated.se.sundsvall.arendeexport.GetArende;
import generated.se.sundsvall.arendeexport.GetArendeResponse;
import generated.se.sundsvall.arendeexport.GetDocument;
import generated.se.sundsvall.arendeexport.GetDocumentResponse;
import generated.se.sundsvall.arendeexport.GetRelateradeArendenByPersOrgNrAndRole;
import generated.se.sundsvall.arendeexport.GetRelateradeArendenByPersOrgNrAndRoleResponse;
import generated.se.sundsvall.arendeexport.GetRoller;
import generated.se.sundsvall.arendeexport.GetRollerResponse;
import generated.se.sundsvall.arendeexport.Roll;

@ExtendWith(MockitoExtension.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ByggrIntegrationCacheTest {

	@Autowired
	private ByggrIntegration byggrIntegration;

	@MockBean
	private ByggrClient mockByggrClient;

	@MockBean
	private ByggrIntegrationMapper mockByggrIntegrationMapper;

	@Test
	void testGetErrandsCaching() {
		var identifier = "1234567890";
		var roles = List.of("ANM");
		var response = new GetRelateradeArendenByPersOrgNrAndRoleResponse();

		when(mockByggrIntegrationMapper.mapToGetRelateradeArendenRequest(any())).thenReturn(new GetRelateradeArendenByPersOrgNrAndRole());
		when(mockByggrClient.getRelateradeArendenByPersOrgNrAndRole(any())).thenReturn(response);

		// First call - should hit the service
		var result = byggrIntegration.getErrands(identifier, roles);
		assertThat(result).isSameAs(response);

		// Second call - should hit the cache
		result = byggrIntegration.getErrands(identifier, roles);
		assertThat(result).isSameAs(response);
		verify(mockByggrClient, times(1)).getRelateradeArendenByPersOrgNrAndRole(any());
	}

	@Test
	void testGetRolesCaching() {
		var response = List.of("ANM");
		var getRollerResponse = new GetRollerResponse().withGetRollerResult(new ArrayOfRoll().withRoll(new Roll().withRollKod("ANM").withArAktiv(true)));

		when(mockByggrClient.getRoller(any())).thenReturn(getRollerResponse);
		when(mockByggrIntegrationMapper.createGetRolesRequest()).thenReturn(new GetRoller());

		// First call - should hit the service
		var result = byggrIntegration.getRoles();
		assertThat(result).isEqualTo(response); // Cannot use isSameAs since the list is copied

		// Second call - should hit the cache
		result = byggrIntegration.getRoles();
		assertThat(result).isEqualTo(response); // Cannot use isSameAs since the list is copied
		verify(mockByggrClient, times(1)).getRoller(any());
	}

	@Test
	void testGetErrandCaching() {
		var dnr = "1234567890";
		var response = new GetArendeResponse();

		when(mockByggrIntegrationMapper.mapToGetArendeRequest(any())).thenReturn(new GetArende());
		when(mockByggrClient.getArende(any())).thenReturn(response);

		// First call - should hit the service
		var result = byggrIntegration.getErrand(dnr);
		assertThat(result).isSameAs(response);

		// Second call - should hit the cache
		result = byggrIntegration.getErrand(dnr);
		assertThat(result).isSameAs(response);
		verify(mockByggrClient, times(1)).getArende(any());
	}

	@Test
	void testGetDocumentCaching() {
		var documentId = "1234567890";
		var response = new GetDocumentResponse();

		when(mockByggrIntegrationMapper.mapToGetDocumentRequest(any())).thenReturn(new GetDocument());
		when(mockByggrClient.getDocument(any())).thenReturn(response);

		// First call - should hit the service
		var result = byggrIntegration.getDocument(documentId);
		assertThat(result).isSameAs(response);

		// Second call - should hit the cache
		result = byggrIntegration.getDocument(documentId);
		assertThat(result).isSameAs(response);
		verify(mockByggrClient, times(1)).getDocument(any());
	}
}
