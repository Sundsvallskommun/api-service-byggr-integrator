package se.sundsvall.byggrintegrator.integration.byggr;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import generated.se.sundsvall.arendeexport.ArrayOfRoll;
import generated.se.sundsvall.arendeexport.GetDocument;
import generated.se.sundsvall.arendeexport.GetDocumentResponse;
import generated.se.sundsvall.arendeexport.GetRoller;
import generated.se.sundsvall.arendeexport.GetRollerResponse;
import generated.se.sundsvall.arendeexport.Roll;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import se.sundsvall.byggrintegrator.Application;

@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
	"spring.cache.type=SIMPLE",
	"spring.cache.cache-names=getRolesCache, getDocumentCache"
})
@ActiveProfiles("junit")
class ByggrIntegrationCacheTest {

	@Autowired
	private ByggrIntegration byggrIntegration;

	@MockitoBean
	private ByggrClient mockByggrClient;

	@MockitoBean
	private ByggrIntegrationMapper mockByggrIntegrationMapper;

	@Test
	void testGetRolesCaching() {
		final var response = List.of("ANM");
		final var getRollerResponse = new GetRollerResponse().withGetRollerResult(new ArrayOfRoll().withRoll(new Roll().withRollKod("ANM").withArAktiv(true)));

		when(mockByggrClient.getRoller(any())).thenReturn(getRollerResponse);
		when(mockByggrIntegrationMapper.createGetRolesRequest()).thenReturn(new GetRoller());

		// First call - should hit the service
		var result = byggrIntegration.getRoles();
		assertThat(result).isEqualTo(response); // Cannot use isSameAs since the list is copied

		// Second call - should hit the cache
		result = byggrIntegration.getRoles();
		assertThat(result).isEqualTo(response); // Cannot use isSameAs since the list is copied

		// Mocks should only have been called once
		verify(mockByggrIntegrationMapper).createGetRolesRequest();
		verify(mockByggrClient).getRoller(any());
		verifyNoMoreInteractions(mockByggrIntegrationMapper, mockByggrClient);
	}

	@Test
	void testGetDocumentCaching() {
		final var documentId = "1234567890";
		final var response = new GetDocumentResponse();

		when(mockByggrIntegrationMapper.mapToGetDocumentRequest(any())).thenReturn(new GetDocument());
		when(mockByggrClient.getDocument(any())).thenReturn(response);

		// First call - should hit the service
		var result = byggrIntegration.getDocument(documentId);
		assertThat(result).isSameAs(response);

		// Second call - should hit the cache
		result = byggrIntegration.getDocument(documentId);
		assertThat(result).isSameAs(response);

		// Mocks should only have been called once
		verify(mockByggrIntegrationMapper).mapToGetDocumentRequest(documentId);
		verify(mockByggrClient).getDocument(any());
		verifyNoMoreInteractions(mockByggrIntegrationMapper, mockByggrClient);
	}

	@Test
	void testGetRolesHasCorrectCacheAnnotation() throws NoSuchMethodException {
		assertThat(ByggrIntegration.class.getMethod("getRoles").getAnnotation(Cacheable.class).value()).containsExactly("getRolesCache");
	}

	@Test
	void testGetDocumentHasCorrectCacheAnnotation() throws NoSuchMethodException {
		assertThat(ByggrIntegration.class.getMethod("getDocument", String.class).getAnnotation(Cacheable.class).value()).containsExactly("getDocumentCache");
	}
}
