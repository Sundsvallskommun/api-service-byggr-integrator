package se.sundsvall.byggrintegrator.service;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static se.sundsvall.byggrintegrator.TestObjectFactory.generateArendeResponse;
import static se.sundsvall.byggrintegrator.TestObjectFactory.generateByggrErrandDtos;
import static se.sundsvall.byggrintegrator.TestObjectFactory.generateDocumentResponse;
import static se.sundsvall.byggrintegrator.TestObjectFactory.generateRelateradeArendenResponse;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;

import generated.se.sundsvall.arendeexport.GetArendeResponse;
import generated.se.sundsvall.arendeexport.GetRelateradeArendenByPersOrgNrAndRoleResponse;
import generated.se.sundsvall.arendeexport.ObjectFactory;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import se.sundsvall.byggrintegrator.TestObjectFactory;
import se.sundsvall.byggrintegrator.integration.byggr.ByggrIntegration;
import se.sundsvall.byggrintegrator.integration.byggr.ByggrIntegrationMapper;
import se.sundsvall.byggrintegrator.model.ByggrErrandDto;
import se.sundsvall.byggrintegrator.service.template.TemplateMapper;

@ExtendWith(MockitoExtension.class)
class ByggrIntegratorServiceTest {

	@Mock
	private ByggrIntegrationMapper mockByggrIntegrationMapper;

	@Mock
	private ByggrIntegration mockByggrIntegration;

	@Mock
	private ApiResponseMapper mockApiResponseMapper;

	@Mock
	private TemplateMapper mockTemplateMapper;

	@Mock
	private HttpServletResponse mockHttpServletResponse;

	@Mock
	private ServletOutputStream mockServletOutputStream;

	@InjectMocks
	private ByggrIntegratorService service;

	private static final ObjectFactory OBJECT_FACTORY = new ObjectFactory();
	private static final List<String> ROLES = List.of("ANM");
	private static final String IDENTIFIER = "1234567890";
	private static final String BYGGR_ERRAND_NUMBER = "BYGG 2024-000123";
	private static final String IDENTIFIER_WITH_DASH = "123456-7890";
	private static final String MUNICIPALITY_ID = "2281";

	@Test
	void testFindNeighborhoodNotifications() {
		// Arrange
		when(mockByggrIntegration.getRoles()).thenReturn(ROLES);
		when(mockByggrIntegration.getErrands(IDENTIFIER_WITH_DASH, ROLES)).thenReturn(generateRelateradeArendenResponse());
		when(mockByggrIntegrationMapper.mapToNeighborhoodNotifications(any(GetRelateradeArendenByPersOrgNrAndRoleResponse.class))).thenCallRealMethod();
		when(mockApiResponseMapper.mapToKeyValueResponseList(anyList())).thenCallRealMethod();

		// Act
		final var neighborNotifications = service.findNeighborhoodNotifications(IDENTIFIER);

		// Assert
		assertThat(neighborNotifications).hasSize(2).satisfiesExactlyInAnyOrder(notification -> {
			assertThat(notification.key()).isEqualTo(BYGGR_ERRAND_NUMBER);
			assertThat(notification.value()).isEqualTo("BYGG 2024-000123, ANKEBORG 1:1234");
		}, notification -> {
			assertThat(notification.key()).isEqualTo(BYGGR_ERRAND_NUMBER);
			assertThat(notification.value()).isEqualTo("BYGG 2024-000123, ANKEBORG 2:5678");
		});
		verify(mockByggrIntegration).getRoles();
		verify(mockByggrIntegration).getErrands(IDENTIFIER_WITH_DASH, ROLES);
		verify(mockByggrIntegrationMapper).mapToNeighborhoodNotifications(any(GetRelateradeArendenByPersOrgNrAndRoleResponse.class));
		verify(mockApiResponseMapper).mapToKeyValueResponseList(anyList());
		verifyNoMoreInterations();
	}

	@Test
	void testFindNeighborhoodNotifications_whenEmpty_shouldReturnEmptyList() {
		// Arrange
		when(mockByggrIntegration.getRoles()).thenReturn(ROLES);
		when(mockByggrIntegration.getErrands(IDENTIFIER_WITH_DASH, ROLES)).thenReturn(OBJECT_FACTORY.createGetRelateradeArendenByPersOrgNrAndRoleResponse());
		when(mockByggrIntegrationMapper.mapToNeighborhoodNotifications(any(GetRelateradeArendenByPersOrgNrAndRoleResponse.class))).thenCallRealMethod();
		when(mockApiResponseMapper.mapToKeyValueResponseList(anyList())).thenCallRealMethod();

		// Act
		final var neighborNotifications = service.findNeighborhoodNotifications(IDENTIFIER);

		// Assert
		assertThat(neighborNotifications).isEmpty();
		verify(mockByggrIntegration).getRoles();
		verify(mockByggrIntegration).getErrands(IDENTIFIER_WITH_DASH, ROLES);
		verify(mockByggrIntegrationMapper).mapToNeighborhoodNotifications(any(GetRelateradeArendenByPersOrgNrAndRoleResponse.class));
		verify(mockApiResponseMapper).mapToKeyValueResponseList(emptyList());
		verifyNoMoreInterations();
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
				assertThat(throwableProblem.getTitle()).isEqualTo(Status.NOT_FOUND.getReasonPhrase());
				assertThat(throwableProblem.getDetail()).isEqualTo("No roles found, cannot continue fetching neighborhood notifications");
			});

		verify(mockByggrIntegration).getRoles();
		verifyNoMoreInteractions(mockByggrIntegration);
		verifyNoInteractions(mockByggrIntegrationMapper, mockApiResponseMapper, mockApiResponseMapper);
	}

	@Test
	void testListNeighborhoodNotificationFiles() {
		when(mockByggrIntegration.getErrand(BYGGR_ERRAND_NUMBER)).thenReturn(OBJECT_FACTORY.createGetArendeResponse());
		when(mockByggrIntegrationMapper.mapToNeighborhoodNotificationFiles(any())).thenReturn(ByggrErrandDto.builder().build());
		when(mockTemplateMapper.generateFileList(any(String.class), any(ByggrErrandDto.class))).thenReturn("html");

		final var html = service.listNeighborhoodNotificationFiles(MUNICIPALITY_ID, BYGGR_ERRAND_NUMBER);

		assertThat(html).isEqualTo("html");
		verify(mockByggrIntegration).getErrand(BYGGR_ERRAND_NUMBER);
		verify(mockByggrIntegrationMapper).mapToNeighborhoodNotificationFiles(any(GetArendeResponse.class));
		verify(mockTemplateMapper).generateFileList(eq(MUNICIPALITY_ID), any(ByggrErrandDto.class));
		verifyNoInteractions(mockApiResponseMapper);
		verifyNoMoreInteractions(mockByggrIntegration, mockByggrIntegrationMapper, mockTemplateMapper);
	}

	@Test
	void testFindApplicantErrands() {
		// Arrange
		when(mockByggrIntegration.getErrands(IDENTIFIER_WITH_DASH, null)).thenReturn(generateRelateradeArendenResponse());
		when(mockByggrIntegrationMapper.mapToApplicantErrands(any(GetRelateradeArendenByPersOrgNrAndRoleResponse.class), eq(IDENTIFIER_WITH_DASH))).thenReturn(generateByggrErrandDtos());
		when(mockApiResponseMapper.mapToKeyValueResponseList(anyList())).thenCallRealMethod();

		// Act
		final var applicantErrands = service.findApplicantErrands(IDENTIFIER);

		// Assert
		assertThat(applicantErrands).hasSize(4).satisfiesExactlyInAnyOrder(notification -> {
			assertThat(notification.key()).isEqualTo("dnr123");
			assertThat(notification.value()).isEqualTo("dnr123, des-1 type1");
		}, notification -> {
			assertThat(notification.key()).isEqualTo("dnr123");
			assertThat(notification.value()).isEqualTo("dnr123, des-2 type2");
		}, notification -> {
			assertThat(notification.key()).isEqualTo("dnr456");
			assertThat(notification.value()).isEqualTo("dnr456, des-3 type3");
		}, notification -> {
			assertThat(notification.key()).isEqualTo("dnr456");
			assertThat(notification.value()).isEqualTo("dnr456, des-4 type4");
		});
		verify(mockByggrIntegration).getErrands(IDENTIFIER_WITH_DASH, null);
		verify(mockByggrIntegrationMapper).mapToApplicantErrands(any(GetRelateradeArendenByPersOrgNrAndRoleResponse.class), eq(IDENTIFIER_WITH_DASH));
		verify(mockApiResponseMapper).mapToKeyValueResponseList(anyList());
		verifyNoMoreInterations();
	}

	@Test
	void testGetErrandType() {
		// Arrange
		final var dnr = "dnr";
		when(mockByggrIntegration.getErrand(dnr)).thenReturn(generateArendeResponse(dnr));
		when(mockApiResponseMapper.mapToWeight(any(GetArendeResponse.class))).thenCallRealMethod();

		// Act
		final var errandType = service.getErrandType(dnr);

		// Assert
		assertThat(errandType.getValue()).isEqualTo(TestObjectFactory.ARENDE_TYP_LH);

		verify(mockByggrIntegration).getErrand(dnr);
		verify(mockApiResponseMapper).mapToWeight(any(GetArendeResponse.class));
		verifyNoMoreInterations();
	}

	@Test
	void testGetErrandType_nonMatchingErrand() {
		// Arrange
		final var dnr = "dnr";

		// Act and assert
		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> service.getErrandType(dnr))
			.satisfies(throwableProblem -> {
				assertThat(throwableProblem.getStatus()).isEqualTo(Status.NOT_FOUND);
				assertThat(throwableProblem.getTitle()).isEqualTo(Status.NOT_FOUND.getReasonPhrase());
				assertThat(throwableProblem.getDetail()).isEqualTo("No errand with diary number dnr was found");
			});

		verify(mockByggrIntegration).getErrand(dnr);
		verifyNoMoreInterations();
	}

	@Test
	void testReadFile() throws Exception {
		// Arrange
		final var fileId = "fileId";
		when(mockByggrIntegration.getDocument(fileId)).thenReturn(generateDocumentResponse(fileId));
		when(mockHttpServletResponse.getOutputStream()).thenReturn(mockServletOutputStream);

		// Act
		service.readFile(fileId, mockHttpServletResponse);

		// Assert
		verify(mockByggrIntegration).getDocument(fileId);
		verify(mockHttpServletResponse).addHeader(CONTENT_TYPE, "text/plain");
		verify(mockHttpServletResponse).addHeader(CONTENT_DISPOSITION, "attachment; filename=\"random.txt\"");
		verify(mockHttpServletResponse).setContentLength(TestObjectFactory.DOCUMENT_CONTENT.length);
		verify(mockHttpServletResponse).getOutputStream();
		verifyNoMoreInterations();
	}

	@Test
	void testReadFile_fileNotFound() {
		// Arrange
		final var fileId = "fileId";

		// Act and assert
		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> service.readFile(fileId, mockHttpServletResponse))
			.satisfies(throwableProblem -> {
				assertThat(throwableProblem.getStatus()).isEqualTo(Status.NOT_FOUND);
				assertThat(throwableProblem.getTitle()).isEqualTo(Status.NOT_FOUND.getReasonPhrase());
				assertThat(throwableProblem.getDetail()).isEqualTo("No file with id fileId was found");
			});

		verify(mockByggrIntegration).getDocument(fileId);
		verifyNoMoreInterations();
	}

	@Test
	void testReadFile_fileProcessingException() throws Exception {
		// Arrange
		final var fileId = "fileId";
		when(mockByggrIntegration.getDocument(fileId)).thenReturn(generateDocumentResponse(fileId));
		when(mockHttpServletResponse.getOutputStream()).thenThrow(new IOException("An error occured during byte array copy"));

		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> service.readFile(fileId, mockHttpServletResponse))
			.satisfies(throwableProblem -> {
				assertThat(throwableProblem.getStatus()).isEqualTo(Status.INTERNAL_SERVER_ERROR);
				assertThat(throwableProblem.getTitle()).isEqualTo(Status.INTERNAL_SERVER_ERROR.getReasonPhrase());
				assertThat(throwableProblem.getDetail()).isEqualTo("Could not read file content for document data with id fileId");
			});

		verify(mockByggrIntegration).getDocument(fileId);
		verify(mockHttpServletResponse).addHeader(CONTENT_TYPE, "text/plain");
		verify(mockHttpServletResponse).addHeader(CONTENT_DISPOSITION, "attachment; filename=\"random.txt\"");
		verify(mockHttpServletResponse).setContentLength(TestObjectFactory.DOCUMENT_CONTENT.length);
		verify(mockHttpServletResponse).getOutputStream();
		verifyNoMoreInterations();
	}

	private void verifyNoMoreInterations() {
		verifyNoMoreInteractions(mockByggrIntegration, mockByggrIntegrationMapper, mockApiResponseMapper, mockHttpServletResponse);
	}
}
