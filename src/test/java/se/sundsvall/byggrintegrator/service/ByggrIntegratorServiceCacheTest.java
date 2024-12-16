package se.sundsvall.byggrintegrator.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import generated.se.sundsvall.arendeexport.GetArendeResponse;
import generated.se.sundsvall.arendeexport.GetRelateradeArendenByPersOrgNrAndRoleResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import se.sundsvall.byggrintegrator.Application;
import se.sundsvall.byggrintegrator.api.model.KeyValue;
import se.sundsvall.byggrintegrator.api.model.Weight;
import se.sundsvall.byggrintegrator.integration.byggr.ByggrIntegration;
import se.sundsvall.byggrintegrator.integration.byggr.ByggrIntegrationMapper;
import se.sundsvall.byggrintegrator.model.ByggrErrandDto;
import se.sundsvall.byggrintegrator.service.template.TemplateMapper;
import se.sundsvall.byggrintegrator.service.util.ByggrFilterUtility;

@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
	"spring.cache.type=SIMPLE",
	"spring.cache.cache-names=findNeighborhoodNotificationsCache, findApplicantErrandsCache, getPropertyDesignationCache, getErrandTypeCache, listNeighborhoodNotificationFilesCache, getHandlingTyperCache"
})
@ActiveProfiles("junit")
class ByggrIntegratorServiceCacheTest {

	@Autowired
	private ByggrIntegratorService byggrIntegratorService;

	@MockitoBean
	private ByggrIntegration mockByggrIntegration;

	@MockitoBean
	private ByggrIntegrationMapper mockByggrIntegrationMapper;

	@MockitoBean
	private ApiResponseMapper mockApiResponseMapper;

	@MockitoBean
	private TemplateMapper mockTemplateMapper;

	@MockitoBean
	private ByggrFilterUtility mockFilterUtility;

	@Captor
	private ArgumentCaptor<List<ByggrErrandDto>> byggrErrandDtosCaptor;

	@Test
	void testFindNeighborhoodNotificationsCaching() {
		final var identifier = "16123456-7890";
		final var roles = List.of("ANM");
		final var errands = List.of(new GetRelateradeArendenByPersOrgNrAndRoleResponse());
		final var response = List.of(new KeyValue("key", "value"));

		when(mockByggrIntegration.getRoles()).thenReturn(roles);
		when(mockByggrIntegration.getErrands(identifier, roles)).thenReturn(errands);
		when(mockByggrIntegrationMapper.mapToByggrErrandDtos(errands)).thenCallRealMethod();
		when(mockFilterUtility.filterNeighborhoodNotifications(any(), any())).thenAnswer(invocation -> invocation.getArgument(0));
		when(mockApiResponseMapper.mapToNeighborhoodKeyValueResponseList(any())).thenReturn(response);

		// First call - should hit the service
		var result = byggrIntegratorService.findNeighborhoodNotifications(identifier);

		// Mocks should only be called first time
		verify(mockByggrIntegration).getRoles();
		verify(mockByggrIntegration).getErrands(identifier, roles);
		verify(mockByggrIntegrationMapper).mapToByggrErrandDtos(errands);
		verify(mockFilterUtility).filterNeighborhoodNotifications(byggrErrandDtosCaptor.capture(), eq(identifier));
		verify(mockApiResponseMapper).mapToNeighborhoodKeyValueResponseList(byggrErrandDtosCaptor.getValue());

		assertThat(result).isEqualTo(response);

		// Second call - should hit the cache
		result = byggrIntegratorService.findNeighborhoodNotifications(identifier);

		assertThat(result).isEqualTo(response);

		verifyNoMoreInteractions(mockByggrIntegration, mockByggrIntegrationMapper, mockApiResponseMapper, mockTemplateMapper, mockFilterUtility);
	}

	@Test
	void testFindNeighborhoodNotificationsHasCorrectCacheAnnotation() throws NoSuchMethodException {
		assertThat(ByggrIntegratorService.class.getMethod("findNeighborhoodNotifications", String.class).getAnnotation(Cacheable.class).value()).containsExactly("findNeighborhoodNotificationsCache");
	}

	@Test
	void testFindApplicantErrandsCaching() {
		final var identifier = "16123456-7890";
		final var errands = List.of(new GetRelateradeArendenByPersOrgNrAndRoleResponse());
		final var response = List.of(new KeyValue("key", "value"));

		when(mockByggrIntegration.getErrands(identifier, null)).thenReturn(errands);
		when(mockByggrIntegrationMapper.mapToByggrErrandDtos(errands)).thenCallRealMethod();
		when(mockFilterUtility.filterCasesForApplicant(any(), any())).thenAnswer(invocation -> invocation.getArgument(0));
		when(mockApiResponseMapper.mapToKeyValueResponseList(any())).thenReturn(response);

		// First call - should hit the service
		var result = byggrIntegratorService.findApplicantErrands(identifier);

		// Mocks should only be called first time
		verify(mockByggrIntegration).getErrands(identifier, null);
		verify(mockByggrIntegrationMapper).mapToByggrErrandDtos(errands);
		verify(mockFilterUtility).filterCasesForApplicant(byggrErrandDtosCaptor.capture(), eq(identifier));
		verify(mockApiResponseMapper).mapToKeyValueResponseList(byggrErrandDtosCaptor.getValue());

		assertThat(result).isEqualTo(response);

		// Second call - should hit the cache
		result = byggrIntegratorService.findApplicantErrands(identifier);

		assertThat(result).isEqualTo(response);

		verifyNoMoreInteractions(mockByggrIntegration, mockByggrIntegrationMapper, mockApiResponseMapper, mockTemplateMapper, mockFilterUtility);
	}

	@Test
	void testFindApplicantErrandsHasCorrectCacheAnnotation() throws NoSuchMethodException {
		assertThat(ByggrIntegratorService.class.getMethod("findApplicantErrands", String.class).getAnnotation(Cacheable.class).value()).containsExactly("findApplicantErrandsCache");
	}

	@Test
	void testGetPropertyDesignationCaching() {
		final var caseNumber = "caseNumber";
		final var errand = new GetArendeResponse();
		final var response = "response";

		when(mockByggrIntegration.getErrand(caseNumber)).thenReturn(errand);
		when(mockByggrIntegrationMapper.mapToByggrErrandDto(errand)).thenCallRealMethod();
		when(mockTemplateMapper.getDescriptionAndPropertyDesignation(any())).thenReturn(response);

		// First call - should hit the service
		var result = byggrIntegratorService.getPropertyDesignation(caseNumber);

		// Mocks should only be called first time
		verify(mockByggrIntegration).getErrand(caseNumber);
		verify(mockByggrIntegrationMapper).mapToByggrErrandDto(errand);
		verify(mockTemplateMapper).getDescriptionAndPropertyDesignation(any());

		assertThat(result).isEqualTo(response);

		// Second call - should hit the cache
		result = byggrIntegratorService.getPropertyDesignation(caseNumber);

		assertThat(result).isEqualTo(response);

		verifyNoMoreInteractions(mockByggrIntegration, mockByggrIntegrationMapper, mockApiResponseMapper, mockTemplateMapper, mockFilterUtility);
	}

	@Test
	void testGetPropertyDesignationHasCorrectCacheAnnotation() throws NoSuchMethodException {
		assertThat(ByggrIntegratorService.class.getMethod("getPropertyDesignation", String.class).getAnnotation(Cacheable.class).value()).containsExactly("getPropertyDesignationCache");
	}

	@Test
	void testGetErrandTypeCaching() {
		final var caseNumber = "caseNumber";
		final var errand = new GetArendeResponse();
		final var weight = Weight.builder().withValue("weight").build();

		when(mockByggrIntegration.getErrand(caseNumber)).thenReturn(errand);
		when(mockApiResponseMapper.mapToWeight(errand)).thenReturn(weight);

		// First call - should hit the service
		var result = byggrIntegratorService.getErrandType(caseNumber);

		// Mocks should only be called first time
		verify(mockByggrIntegration).getErrand(caseNumber);
		verify(mockApiResponseMapper).mapToWeight(errand);

		assertThat(result).isEqualTo(weight);

		// Second call - should hit the cache
		result = byggrIntegratorService.getErrandType(caseNumber);

		assertThat(result).isEqualTo(weight);

		verifyNoMoreInteractions(mockByggrIntegration, mockByggrIntegrationMapper, mockApiResponseMapper, mockTemplateMapper, mockFilterUtility);
	}

	@Test
	void testGetErrandTypeHasCorrectCacheAnnotation() throws NoSuchMethodException {
		assertThat(ByggrIntegratorService.class.getMethod("getErrandType", String.class).getAnnotation(Cacheable.class).value()).containsExactly("getErrandTypeCache");
	}

	@Test
	void testListNeighborhoodNotificationFilesCaching() {
		final var municipalityId = "municipalityId";
		final var caseNumber = "caseNumber";
		final var eventId = 123;
		final var errand = new GetArendeResponse();
		final var response = "response";

		when(mockByggrIntegration.getErrand(caseNumber)).thenReturn(errand);
		when(mockByggrIntegrationMapper.mapToByggrErrandDto(errand)).thenCallRealMethod();
		when(mockFilterUtility.filterEvent(any(), eq(eventId))).thenAnswer(invocation -> invocation.getArgument(0));
		when(mockTemplateMapper.generateFileList(eq(municipalityId), any(), any(), eq(eventId))).thenReturn(response);

		// First call - should hit the service
		var result = byggrIntegratorService.listNeighborhoodNotificationFiles(municipalityId, caseNumber, eventId);

		// Mocks should only be called first time
		verify(mockByggrIntegration).getErrand(caseNumber);
		verify(mockByggrIntegrationMapper).mapToByggrErrandDto(errand);
		verify(mockByggrIntegration).getHandlingTyper();
		verify(mockFilterUtility).filterEvent(any(), eq(eventId));
		verify(mockTemplateMapper).generateFileList(eq(municipalityId), any(), any(), eq(eventId));

		assertThat(result).isEqualTo(response);

		// Second call - should hit the cache
		result = byggrIntegratorService.listNeighborhoodNotificationFiles(municipalityId, caseNumber, eventId);

		assertThat(result).isEqualTo(response);

		verifyNoMoreInteractions(mockByggrIntegration, mockByggrIntegrationMapper, mockApiResponseMapper, mockTemplateMapper, mockFilterUtility);
	}

	@Test
	void testListNeighborhoodNotificationFilesHasCorrectCacheAnnotation() throws NoSuchMethodException {
		assertThat(ByggrIntegratorService.class.getMethod("listNeighborhoodNotificationFiles", String.class, String.class, int.class).getAnnotation(Cacheable.class).value()).containsExactly("listNeighborhoodNotificationFilesCache");
	}

}
