package se.sundsvall.byggrintegrator.service;

import generated.se.sundsvall.arendeexport.v4.ArrayOfHandelseHandling;
import generated.se.sundsvall.arendeexport.v4.ArrayOfRemiss;
import generated.se.sundsvall.arendeexport.v4.ArrayOfString2;
import generated.se.sundsvall.arendeexport.v4.GetRemisserByPersOrgNrResponse;
import generated.se.sundsvall.arendeexport.v4.HandelseHandling;
import generated.se.sundsvall.arendeexport.v4.HandelseIntressent;
import generated.se.sundsvall.arendeexport.v4.Remiss;
import generated.se.sundsvall.arendeexport.v8.GetArendeResponse;
import generated.se.sundsvall.arendeexport.v8.ObjectFactory;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.byggrintegrator.api.model.KeyValue;
import se.sundsvall.byggrintegrator.integration.byggr.ByggrIntegration;
import se.sundsvall.byggrintegrator.integration.byggr.ByggrIntegrationMapper;
import se.sundsvall.byggrintegrator.model.ByggrErrandDto;
import se.sundsvall.byggrintegrator.service.template.TemplateService;
import se.sundsvall.byggrintegrator.service.util.ByggrFilterUtility;
import se.sundsvall.dept44.problem.ThrowableProblem;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static se.sundsvall.byggrintegrator.TestObjectFactory.APPLICANT_ROLE;
import static se.sundsvall.byggrintegrator.TestObjectFactory.CASE_APPLICANT;
import static se.sundsvall.byggrintegrator.TestObjectFactory.DOCUMENT_CONTENT;
import static se.sundsvall.byggrintegrator.TestObjectFactory.NEIGHBORHOOD_NOTIFICATION_STAKEHOLDER;
import static se.sundsvall.byggrintegrator.TestObjectFactory.generateArendeResponse;
import static se.sundsvall.byggrintegrator.TestObjectFactory.generateDocumentResponse;
import static se.sundsvall.byggrintegrator.TestObjectFactory.generateRelateradeArendenResponse;

@ExtendWith(MockitoExtension.class)
class ByggrIntegratorServiceTest {

	private static final ObjectFactory OBJECT_FACTORY = new ObjectFactory();
	private static final List<String> ROLES = List.of("ANM");
	private static final String ORG_IDENTIFIER = "1234567890";
	private static final String PRIVATE_IDENTIFIER = "123456789012";
	private static final String BYGGR_ERRAND_NUMBER = "BYGG 2024-000123";
	private static final String PROCESSED_ORG_IDENTIFIER = "16123456-7890";
	private static final String PROCESSED_PRIVATE_IDENTIFIER = "12345678-9012";
	private static final String MUNICIPALITY_ID = "2281";
	private static final String REFERRAL_REFERENCE = "1234567890";
	private static final String CASE_NUMBER = "caseNumber";

	@Mock
	private ByggrIntegrationMapper mockByggrIntegrationMapper;

	@Mock
	private ByggrIntegration mockByggrIntegration;

	@Mock
	private ApiResponseMapper mockApiResponseMapper;

	@Mock
	private TemplateService mockTemplateService;

	@Mock
	private HttpServletResponse mockHttpServletResponse;

	@Mock
	private ByggrFilterUtility mockByggrFilterUtility;

	@Mock
	private FileAccessTokenService mockFileAccessTokenService;

	@Mock
	private ServletOutputStream mockServletOutputStream;

	@InjectMocks
	private ByggrIntegratorService service;

	private static Stream<Arguments> identifierProvider() {
		return Stream.of(
			Arguments.of(ORG_IDENTIFIER, PROCESSED_ORG_IDENTIFIER),
			Arguments.of(PRIVATE_IDENTIFIER, PROCESSED_PRIVATE_IDENTIFIER));
	}

	@ParameterizedTest
	@MethodSource("identifierProvider")
	void testFindNeighborhoodNotifications(final String identifier, final String processedIdentifier) throws Exception {
		// Arrange
		final var response = List.of(generateRelateradeArendenResponse(CASE_APPLICANT, processedIdentifier));
		setField(mockByggrIntegrationMapper, "filterUtility", mockByggrFilterUtility);
		setField(mockByggrFilterUtility, "unwantedDocumentTypes", List.of("GRA", "UNDUT"));

		when(mockByggrIntegration.getRoles()).thenReturn(ROLES);
		when(mockByggrIntegration.getErrands(processedIdentifier, ROLES)).thenReturn(response);
		when(mockByggrIntegrationMapper.mapToByggrErrandDtos(response)).thenCallRealMethod();
		when(mockByggrFilterUtility.hasValidDocumentType(any())).thenCallRealMethod();
		when(mockByggrFilterUtility.filterNeighborhoodNotifications(anyList(), eq(processedIdentifier))).thenCallRealMethod();
		when(mockByggrFilterUtility.filterEvents(eq(processedIdentifier), any())).thenCallRealMethod();
		when(mockApiResponseMapper.mapToKeyValueResponseList(any())).thenCallRealMethod();

		// Act
		final var neighborNotifications = service.findNeighborhoodNotifications(identifier);

		// Assert
		assertThat(neighborNotifications).hasSize(2).satisfiesExactlyInAnyOrder(notification -> {
			assertThat(notification.key()).isEqualTo("1");
			assertThat(notification.value()).isEqualTo("BYGG 2024-000123");
		}, notification -> {
			assertThat(notification.key()).isEqualTo("2");
			assertThat(notification.value()).isEqualTo("BYGG 2024-000234");
		});
		verify(mockByggrIntegration).getRoles();
		verify(mockByggrIntegration).getErrands(processedIdentifier, ROLES);
		verify(mockByggrIntegrationMapper).mapToByggrErrandDtos(response);
		verify(mockByggrFilterUtility).filterNeighborhoodNotifications(anyList(), eq(processedIdentifier));
		verify(mockByggrFilterUtility, times(8)).hasValidDocumentType(any());
		verify(mockApiResponseMapper).mapToKeyValueResponseList(anyList());
		verifyNoMoreInteractionsWithMocks();
	}

	@ParameterizedTest
	@MethodSource("identifierProvider")
	void testFindNeighborhoodNotifications_whenEmpty_shouldReturnEmptyList(final String identifier, final String processedIdentifier) {
		// Arrange
		final var response = List.of(OBJECT_FACTORY.createGetRelateradeArendenByPersOrgNrAndRoleResponse());

		when(mockByggrIntegration.getRoles()).thenReturn(ROLES);
		when(mockByggrIntegration.getErrands(processedIdentifier, ROLES)).thenReturn(response);
		when(mockByggrIntegrationMapper.mapToByggrErrandDtos(response)).thenCallRealMethod();
		when(mockByggrFilterUtility.filterNeighborhoodNotifications(anyList(), eq(processedIdentifier))).thenCallRealMethod();
		when(mockApiResponseMapper.mapToKeyValueResponseList(anyList())).thenCallRealMethod();

		// Act
		final var neighborNotifications = service.findNeighborhoodNotifications(identifier);

		// Assert
		assertThat(neighborNotifications).isEmpty();
		verify(mockByggrIntegration).getRoles();
		verify(mockByggrIntegration).getErrands(processedIdentifier, ROLES);
		verify(mockByggrIntegrationMapper).mapToByggrErrandDtos(response);
		verify(mockByggrFilterUtility).filterNeighborhoodNotifications(anyList(), eq(processedIdentifier));
		verify(mockApiResponseMapper).mapToKeyValueResponseList(emptyList());
		verifyNoMoreInteractionsWithMocks();
	}

	@ParameterizedTest
	@ValueSource(strings = {
		ORG_IDENTIFIER, PRIVATE_IDENTIFIER
	})
	void testFindNeighborhoodNotifications_noRoles_shouldThrow404(final String identifier) {
		// Arrange
		when(mockByggrIntegration.getRoles()).thenReturn(emptyList());

		// Act
		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> service.findNeighborhoodNotifications(identifier))
			.satisfies(throwableProblem -> {
				assertThat(throwableProblem.getStatus()).isEqualTo(NOT_FOUND);
				assertThat(throwableProblem.getTitle()).isEqualTo(NOT_FOUND.getReasonPhrase());
				assertThat(throwableProblem.getDetail()).isEqualTo("No roles found, cannot continue fetching neighborhood notifications");
			});

		verify(mockByggrIntegration).getRoles();
		verifyNoMoreInteractions(mockByggrIntegration);
		verifyNoInteractions(mockByggrIntegrationMapper, mockByggrFilterUtility, mockApiResponseMapper, mockApiResponseMapper);
	}

	@ParameterizedTest
	@MethodSource("identifierProvider")
	void testListNeighborhoodNotificationFiles(final String identifier, final String processedIdentifier) {
		when(mockByggrIntegration.getErrand(BYGGR_ERRAND_NUMBER)).thenReturn(OBJECT_FACTORY.createGetArendeResponse());
		when(mockByggrIntegrationMapper.mapToByggrErrandDto(any())).thenReturn(ByggrErrandDto.builder().build());
		when(mockByggrFilterUtility.filterEvents(eq(processedIdentifier), any(ByggrErrandDto.class))).thenReturn(ByggrErrandDto.builder().build());
		when(mockTemplateService.generateFileList(anyString(), any(ByggrErrandDto.class), any(), any())).thenReturn("html");
		when(mockByggrIntegration.getRemisserByPersOrgNr(any())).thenReturn(
			new GetRemisserByPersOrgNrResponse().withGetRemisserByPersOrgNrResult(new ArrayOfRemiss().withRemiss(new Remiss().withUtskicksHandlingar(new ArrayOfHandelseHandling().withHandling(new HandelseHandling())))));
		final var html = service.listNeighborhoodNotificationFiles(MUNICIPALITY_ID, identifier, BYGGR_ERRAND_NUMBER, REFERRAL_REFERENCE);

		assertThat(html).isEqualTo("html");
		verify(mockByggrIntegration).getErrand(BYGGR_ERRAND_NUMBER);
		verify(mockByggrIntegrationMapper).mapToByggrErrandDto(any(GetArendeResponse.class));
		verify(mockByggrFilterUtility).filterEvents(eq(processedIdentifier), any(ByggrErrandDto.class));
		verify(mockTemplateService).generateFileList(eq(MUNICIPALITY_ID), any(ByggrErrandDto.class), any(), any());
		verifyNoMoreInteractions(mockByggrIntegrationMapper, mockByggrFilterUtility, mockApiResponseMapper, mockApiResponseMapper);
		verifyNoInteractions(mockApiResponseMapper);
	}

	@Test
	void testListNeighborhoodNotificationFilesFiltersUnwantedHandlingTypes() {
		final var identifier = PRIVATE_IDENTIFIER;
		final var processedIdentifier = PROCESSED_PRIVATE_IDENTIFIER;
		final var keepHandling = new HandelseHandling().withTyp("BIL");
		final var graHandling = new HandelseHandling().withTyp("GRA");
		final var remissHandling = new HandelseHandling().withTyp("REMISS");
		final var undutHandling = new HandelseHandling().withTyp("UNDUT");

		when(mockByggrIntegration.getErrand(BYGGR_ERRAND_NUMBER)).thenReturn(OBJECT_FACTORY.createGetArendeResponse());
		when(mockByggrIntegrationMapper.mapToByggrErrandDto(any())).thenReturn(ByggrErrandDto.builder().build());
		when(mockByggrFilterUtility.filterEvents(eq(processedIdentifier), any(ByggrErrandDto.class))).thenReturn(ByggrErrandDto.builder().build());
		when(mockTemplateService.generateFileList(anyString(), any(ByggrErrandDto.class), any(), any())).thenReturn("html");
		when(mockByggrIntegration.getRemisserByPersOrgNr(any())).thenReturn(
			new GetRemisserByPersOrgNrResponse().withGetRemisserByPersOrgNrResult(new ArrayOfRemiss().withRemiss(new Remiss()
				.withUtskicksHandlingar(new ArrayOfHandelseHandling().withHandling(keepHandling, graHandling, remissHandling, undutHandling)))));

		service.listNeighborhoodNotificationFiles(MUNICIPALITY_ID, identifier, BYGGR_ERRAND_NUMBER, REFERRAL_REFERENCE);

		@SuppressWarnings("unchecked")
		final var captor = org.mockito.ArgumentCaptor.forClass(List.class);
		verify(mockTemplateService).generateFileList(eq(MUNICIPALITY_ID), any(ByggrErrandDto.class), any(), captor.capture());
		assertThat(captor.getValue())
			.extracting(handling -> ((HandelseHandling) handling).getTyp())
			.containsExactly("BIL");
	}

	@ParameterizedTest
	@MethodSource("identifierProvider")
	void testFindApplicantErrands(final String identifier, final String processedIdentifier) throws Exception {
		// Prepare list of unwanted handelseslag
		setField(mockByggrFilterUtility, "applicantRoles", List.of(APPLICANT_ROLE));
		setField(mockByggrIntegrationMapper, "filterUtility", mockByggrFilterUtility);
		setField(mockByggrFilterUtility, "unwantedDocumentTypes", List.of("GRA", "UNDUT"));

		// Arrange
		final var response = List.of(generateRelateradeArendenResponse(processedIdentifier, NEIGHBORHOOD_NOTIFICATION_STAKEHOLDER));

		when(mockByggrIntegration.getErrands(processedIdentifier, null)).thenReturn(response);
		when(mockByggrIntegrationMapper.mapToByggrErrandDtos(response)).thenCallRealMethod();
		when(mockByggrFilterUtility.filterCasesForApplicant(anyList(), eq(processedIdentifier))).thenCallRealMethod();
		when(mockApiResponseMapper.mapToKeyValueResponseList(anyList())).thenCallRealMethod();

		// Act
		final var applicantErrands = service.findApplicantErrands(identifier);

		// Assert
		assertThat(applicantErrands).hasSize(2).satisfiesExactlyInAnyOrder(notification -> {
			assertThat(notification.key()).isEqualTo("1");
			assertThat(notification.value()).isEqualTo("BYGG 2024-000123");
		}, notification -> {
			assertThat(notification.key()).isEqualTo("2");
			assertThat(notification.value()).isEqualTo("BYGG 2024-000234");
		});
		verify(mockByggrIntegration).getErrands(processedIdentifier, null);
		verify(mockByggrIntegrationMapper).mapToByggrErrandDtos(response);
		verify(mockByggrFilterUtility).filterCasesForApplicant(anyList(), eq(processedIdentifier));
		verify(mockApiResponseMapper).mapToKeyValueResponseList(anyList());
		verify(mockByggrFilterUtility, times(8)).hasValidDocumentType(any());
		verifyNoMoreInteractionsWithMocks();
	}

	@Test
	void getPropertyDesignation() throws Exception {
		setField(mockByggrIntegrationMapper, "filterUtility", mockByggrFilterUtility);
		final var dnr = "BYGG 2024-000123";
		final var byggrErrandDto = ByggrErrandDto.builder()
			.build();

		when(mockByggrIntegration.getErrand(dnr)).thenReturn(generateArendeResponse(dnr));
		when(mockByggrIntegrationMapper.mapToByggrErrandDto(any())).thenReturn(byggrErrandDto);
		when(mockTemplateService.getDescriptionAndPropertyDesignation(any(ByggrErrandDto.class))).thenReturn("RUNSVIK 1:22");

		final var propertyDesignation = service.getPropertyDesignation(dnr);

		assertThat(propertyDesignation).isNotNull().isEqualTo("RUNSVIK 1:22");
		verify(mockByggrIntegration).getErrand(dnr);
		verify(mockByggrIntegrationMapper).mapToByggrErrandDto(any(GetArendeResponse.class));
		verify(mockTemplateService).getDescriptionAndPropertyDesignation(any(ByggrErrandDto.class));

		verifyNoMoreInteractionsWithMocks();
	}

	@Test
	void testGetErrandType() throws Exception {
		// Arrange
		final var dnr = "dnr";
		when(mockByggrIntegration.getErrand(dnr)).thenReturn(generateArendeResponse(dnr));
		when(mockApiResponseMapper.mapToWeight(any(GetArendeResponse.class))).thenCallRealMethod();

		// Act
		final var errandType = service.getErrandType(dnr);

		// Assert
		assertThat(errandType.getValue()).isEqualTo("11"); // BL translated to integer value according to the CaseTypeEnum

		verify(mockByggrIntegration).getErrand(dnr);
		verify(mockApiResponseMapper).mapToWeight(any(GetArendeResponse.class));
		verifyNoMoreInteractionsWithMocks();
	}

	@Test
	void testGetErrandType_nonMatchingErrand() {
		// Arrange
		final var dnr = "dnr";

		// Act and assert
		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> service.getErrandType(dnr))
			.satisfies(throwableProblem -> {
				assertThat(throwableProblem.getStatus()).isEqualTo(NOT_FOUND);
				assertThat(throwableProblem.getTitle()).isEqualTo(NOT_FOUND.getReasonPhrase());
				assertThat(throwableProblem.getDetail()).isEqualTo("No errand with diary number dnr was found");
			});

		verify(mockByggrIntegration).getErrand(dnr);
		verifyNoMoreInteractionsWithMocks();
	}

	private static Stream<Arguments> referralTypeProvider() {
		return Stream.of(
			Arguments.of(ORG_IDENTIFIER, PROCESSED_ORG_IDENTIFIER, "FAG", "2"),
			Arguments.of(PRIVATE_IDENTIFIER, PROCESSED_PRIVATE_IDENTIFIER, "GRAN", "1"));
	}

	@ParameterizedTest
	@MethodSource("referralTypeProvider")
	void testGetReferralType(final String identifier, final String processedIdentifier, final String role, final String expectedWeight) {
		final var referralReference = "SILJE 2:65 [188115]";
		final var remiss = new Remiss()
			.withRemissId(188115)
			.withMottagare(new generated.se.sundsvall.arendeexport.v4.HandelseIntressent()
				.withRollLista(new generated.se.sundsvall.arendeexport.v4.ArrayOfString2().withRoll(role)));

		when(mockByggrIntegration.getRemisserByPersOrgNr(processedIdentifier)).thenReturn(new GetRemisserByPersOrgNrResponse()
			.withGetRemisserByPersOrgNrResult(new ArrayOfRemiss().withRemiss(remiss)));
		when(mockApiResponseMapper.mapToWeight(any(Remiss.class))).thenCallRealMethod();

		final var weight = service.getReferralType(identifier, referralReference);

		assertThat(weight.getValue()).isEqualTo(expectedWeight);
		verify(mockByggrIntegration).getRemisserByPersOrgNr(processedIdentifier);
		verify(mockApiResponseMapper).mapToWeight(any(Remiss.class));
		verifyNoMoreInteractionsWithMocks();
	}

	@Test
	void testGetReferralType_remissNotFound_throws404() {
		final var referralReference = "SILJE 2:65 [999999]";

		when(mockByggrIntegration.getRemisserByPersOrgNr(PROCESSED_PRIVATE_IDENTIFIER)).thenReturn(new GetRemisserByPersOrgNrResponse()
			.withGetRemisserByPersOrgNrResult(new ArrayOfRemiss()));

		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> service.getReferralType(PRIVATE_IDENTIFIER, referralReference))
			.satisfies(throwableProblem -> {
				assertThat(throwableProblem.getStatus()).isEqualTo(NOT_FOUND);
				assertThat(throwableProblem.getTitle()).isEqualTo(NOT_FOUND.getReasonPhrase());
				assertThat(throwableProblem.getDetail()).isEqualTo("No referral with reference SILJE 2:65 [999999] was found");
			});

		verify(mockByggrIntegration).getRemisserByPersOrgNr(PROCESSED_PRIVATE_IDENTIFIER);
		verifyNoMoreInteractionsWithMocks();
	}

	@Test
	void testGetReferralType_referralReferenceWithoutBracketSuffix_throws404() {
		final var referralReference = "SILJE 2:65 – Lämna svar som fastighetsägare";
		final var remiss = new Remiss().withRemissId(188115);

		when(mockByggrIntegration.getRemisserByPersOrgNr(PROCESSED_PRIVATE_IDENTIFIER)).thenReturn(new GetRemisserByPersOrgNrResponse()
			.withGetRemisserByPersOrgNrResult(new ArrayOfRemiss().withRemiss(remiss)));

		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> service.getReferralType(PRIVATE_IDENTIFIER, referralReference))
			.satisfies(throwableProblem -> {
				assertThat(throwableProblem.getStatus()).isEqualTo(NOT_FOUND);
				assertThat(throwableProblem.getTitle()).isEqualTo(NOT_FOUND.getReasonPhrase());
				assertThat(throwableProblem.getDetail()).isEqualTo("No referral with reference SILJE 2:65 – Lämna svar som fastighetsägare was found");
			});

		verify(mockByggrIntegration).getRemisserByPersOrgNr(PROCESSED_PRIVATE_IDENTIFIER);
		verifyNoMoreInteractionsWithMocks();
	}

	@Test
	void testReadFile() throws Exception {
		// Arrange
		final var fileId = "fileId";
		final var token = "valid-token";
		when(mockByggrIntegration.getDocument(fileId)).thenReturn(generateDocumentResponse(fileId));
		when(mockHttpServletResponse.getOutputStream()).thenReturn(mockServletOutputStream);

		// Act
		service.readFile(MUNICIPALITY_ID, fileId, token, mockHttpServletResponse);

		// Assert
		verify(mockFileAccessTokenService).validateToken(MUNICIPALITY_ID, fileId, token);
		verify(mockByggrIntegration).getDocument(fileId);
		verify(mockHttpServletResponse).addHeader(CONTENT_TYPE, "text/plain");
		verify(mockHttpServletResponse).addHeader(CONTENT_DISPOSITION, "attachment; filename=\"random.txt\"");
		verify(mockHttpServletResponse).setContentLength(DOCUMENT_CONTENT.length);
		verify(mockHttpServletResponse).getOutputStream();
		verifyNoMoreInteractionsWithMocks();
	}

	/**
	 * Test scenario where the file name have no extension. The extension in "filAndelse" should be concatenated as the file
	 * extension
	 */
	@Test
	void testReadFile_2() throws IOException {
		final var fileId = "fileId";
		final var token = "valid-token";

		final var dokument = OBJECT_FACTORY.createDokument()
			.withDokId(fileId)
			.withNamn("random-name")
			.withFil(OBJECT_FACTORY.createDokumentFil()
				.withFilAndelse("pdf")
				.withFilBuffer(DOCUMENT_CONTENT));

		when(mockByggrIntegration.getDocument(fileId)).thenReturn(OBJECT_FACTORY.createGetDocumentResponse().withGetDocumentResult(dokument));
		when(mockHttpServletResponse.getOutputStream()).thenReturn(mockServletOutputStream);

		service.readFile(MUNICIPALITY_ID, fileId, token, mockHttpServletResponse);

		verify(mockFileAccessTokenService).validateToken(MUNICIPALITY_ID, fileId, token);
		verify(mockByggrIntegration).getDocument(fileId);
		verify(mockHttpServletResponse).addHeader(CONTENT_TYPE, "application/pdf");
		verify(mockHttpServletResponse).addHeader(CONTENT_DISPOSITION, "attachment; filename=\"random-name.pdf\"");
		verify(mockHttpServletResponse).setContentLength(DOCUMENT_CONTENT.length);
		verify(mockHttpServletResponse).getOutputStream();
		verifyNoMoreInteractionsWithMocks();
	}

	@Test
	void testReadFile_fileNotFound() {
		// Arrange
		final var fileId = "fileId";
		final var token = "valid-token";

		// Act and assert
		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> service.readFile(MUNICIPALITY_ID, fileId, token, mockHttpServletResponse))
			.satisfies(throwableProblem -> {
				assertThat(throwableProblem.getStatus()).isEqualTo(NOT_FOUND);
				assertThat(throwableProblem.getTitle()).isEqualTo(NOT_FOUND.getReasonPhrase());
				assertThat(throwableProblem.getDetail()).isEqualTo("No file with id fileId was found");
			});

		verify(mockFileAccessTokenService).validateToken(MUNICIPALITY_ID, fileId, token);
		verify(mockByggrIntegration).getDocument(fileId);
		verifyNoMoreInteractionsWithMocks();
	}

	@Test
	void testReadFile_fileProcessingException() throws Exception {
		// Arrange
		final var fileId = "fileId";
		final var token = "valid-token";
		when(mockByggrIntegration.getDocument(fileId)).thenReturn(generateDocumentResponse(fileId));
		when(mockHttpServletResponse.getOutputStream()).thenThrow(new IOException("An error occured during byte array copy"));

		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> service.readFile(MUNICIPALITY_ID, fileId, token, mockHttpServletResponse))
			.satisfies(throwableProblem -> {
				assertThat(throwableProblem.getStatus()).isEqualTo(INTERNAL_SERVER_ERROR);
				assertThat(throwableProblem.getTitle()).isEqualTo(INTERNAL_SERVER_ERROR.getReasonPhrase());
				assertThat(throwableProblem.getDetail()).isEqualTo("Could not read file content for document data with id fileId");
			});

		verify(mockFileAccessTokenService).validateToken(MUNICIPALITY_ID, fileId, token);
		verify(mockByggrIntegration).getDocument(fileId);
		verify(mockHttpServletResponse).addHeader(CONTENT_TYPE, "text/plain");
		verify(mockHttpServletResponse).addHeader(CONTENT_DISPOSITION, "attachment; filename=\"random.txt\"");
		verify(mockHttpServletResponse).setContentLength(DOCUMENT_CONTENT.length);
		verify(mockHttpServletResponse).getOutputStream();
		verifyNoMoreInteractionsWithMocks();
	}

	@Test
	void testReadFile_fileNameAlreadyHasExtension() throws IOException {
		final var fileId = "FileId";
		final var token = "valid-token";

		final var dokument = OBJECT_FACTORY.createDokument()
			.withDokId(fileId)
			.withNamn("fil.txt").withFil(OBJECT_FACTORY.createDokumentFil()
				.withFilAndelse("TXT")
				.withFilBuffer(DOCUMENT_CONTENT));

		when(mockByggrIntegration.getDocument(fileId))
			.thenReturn(OBJECT_FACTORY.createGetDocumentResponse().withGetDocumentResult(dokument));

		when(mockHttpServletResponse.getOutputStream()).thenReturn(mockServletOutputStream);

		service.readFile(MUNICIPALITY_ID, fileId, token, mockHttpServletResponse);

		verify(mockFileAccessTokenService).validateToken(MUNICIPALITY_ID, fileId, token);
		verify(mockByggrIntegration).getDocument(fileId);
		verify(mockHttpServletResponse).addHeader(CONTENT_DISPOSITION, "attachment; filename=\"fil.txt\"");
		verify(mockHttpServletResponse).addHeader(CONTENT_TYPE, "text/plain");
		verify(mockHttpServletResponse).setContentLength(DOCUMENT_CONTENT.length);
		verify(mockHttpServletResponse).getOutputStream();
		verifyNoMoreInteractionsWithMocks();
	}

	@ParameterizedTest
	@MethodSource("identifierProvider")
	void getNeighborhoodNotificationFacilities(final String identifier, final String processedIdentifier) {
		final var caseNumber = "caseNumber";
		final var propertyDesignation1 = "propertyDesignation1";
		final var propertyDesignation2 = "propertyDesignation2";

		when(mockByggrIntegration.getRemisserByPersOrgNr(processedIdentifier)).thenReturn(new GetRemisserByPersOrgNrResponse()
			.withGetRemisserByPersOrgNrResult(new ArrayOfRemiss()
				.withRemiss(new Remiss()
					.withDnr(caseNumber)
					.withFastighetsbeteckning(propertyDesignation1)
					.withMottagare(new generated.se.sundsvall.arendeexport.v4.HandelseIntressent()
						.withRollLista(new generated.se.sundsvall.arendeexport.v4.ArrayOfString2().withRoll("GRAN")))
					.withRemissId(123),
					new Remiss()
						.withDnr(caseNumber)
						.withFastighetsbeteckning(propertyDesignation1)
						.withMottagare(new generated.se.sundsvall.arendeexport.v4.HandelseIntressent()
							.withRollLista(new generated.se.sundsvall.arendeexport.v4.ArrayOfString2().withRoll("FAG")))
						.withRemissId(456),
					new Remiss()
						.withDnr(caseNumber)
						.withFastighetsbeteckning(propertyDesignation2)
						.withMottagare(new generated.se.sundsvall.arendeexport.v4.HandelseIntressent()
							.withRollLista(new generated.se.sundsvall.arendeexport.v4.ArrayOfString2().withRoll("GRAN")))
						.withRemissId(789))));
		when(mockApiResponseMapper.mapToKeyValue(any())).thenCallRealMethod();

		final var result = service.getNeighborhoodNotificationFacilities(identifier, caseNumber);

		verify(mockByggrIntegration).getRemisserByPersOrgNr(processedIdentifier);
		verify(mockApiResponseMapper).mapToKeyValue(any());

		assertThat(result).hasSize(3).extracting(KeyValue::key, KeyValue::value).contains(
			tuple("1", "propertyDesignation1 – Lämna svar som granne [123]"),
			tuple("2", "propertyDesignation1 – Lämna svar som fastighetsägare [456]"),
			tuple("3", "propertyDesignation2 – Lämna svar som granne [789]"));
	}

	@Test
	void testListNeighborhoodNotificationFiles_remissNotFound() {
		when(mockByggrIntegration.getErrand(BYGGR_ERRAND_NUMBER))
			.thenReturn(OBJECT_FACTORY.createGetArendeResponse());
		when(mockByggrIntegration.getRemisserByPersOrgNr(PROCESSED_PRIVATE_IDENTIFIER))
			.thenReturn(new GetRemisserByPersOrgNrResponse()
				.withGetRemisserByPersOrgNrResult(new ArrayOfRemiss().withRemiss(new Remiss().withRemissId(123))));

		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> service.listNeighborhoodNotificationFiles(MUNICIPALITY_ID, PRIVATE_IDENTIFIER, BYGGR_ERRAND_NUMBER, REFERRAL_REFERENCE))
			.satisfies(problem -> {
				assertThat(problem.getStatus()).isEqualTo(NOT_FOUND);
				assertThat(problem.getDetail()).isEqualTo("Remiss not found");
			});
	}

	@Test
	void testListNeighborhoodNotificationFacilities_withNullFastighetsBeteckningAndDifferentCaseId() {
		final var propertyDesignation1 = "propertyDesignation1";
		final var propertyDesignation2 = "propertyDesignation2";

		when(mockByggrIntegration.getRemisserByPersOrgNr(PROCESSED_PRIVATE_IDENTIFIER))
			.thenReturn(new GetRemisserByPersOrgNrResponse()
				.withGetRemisserByPersOrgNrResult(new ArrayOfRemiss()
					.withRemiss(new Remiss()
						.withDnr(CASE_NUMBER)
						.withFastighetsbeteckning(propertyDesignation1)
						.withMottagare(new HandelseIntressent()
							.withRollLista(new ArrayOfString2().withRoll(ApiResponseMapper.ROLE_PROPERTY_OWNER)))
						.withRemissId(1),
						new Remiss()
							.withDnr("OTHER_CASE")
							.withFastighetsbeteckning(propertyDesignation2)
							.withRemissId(2),
						new Remiss()
							.withDnr(CASE_NUMBER)
							.withFastighetsbeteckning(null)
							.withRemissId(3))));

		when(mockApiResponseMapper.mapToKeyValue(any())).thenCallRealMethod();

		final var result = service.getNeighborhoodNotificationFacilities(PRIVATE_IDENTIFIER, CASE_NUMBER);

		assertThat(result).hasSize(1)
			.extracting(KeyValue::value)
			.containsExactly("propertyDesignation1 – Lämna svar som fastighetsägare [1]");

		verifyNoMoreInteractionsWithMocks();
	}

	@Test
	void testExtractRecipientRole_emptyRollerLista() {
		when(mockByggrIntegration.getRemisserByPersOrgNr(PROCESSED_PRIVATE_IDENTIFIER))
			.thenReturn(new GetRemisserByPersOrgNrResponse()
				.withGetRemisserByPersOrgNrResult(new ArrayOfRemiss()
					.withRemiss(new Remiss()
						.withDnr(CASE_NUMBER)
						.withFastighetsbeteckning("SILJE 2:65 [999999]")
						.withMottagare(new HandelseIntressent()
							.withRollLista(new ArrayOfString2()))
						.withRemissId(123))));
		when(mockApiResponseMapper.mapToKeyValue(any())).thenCallRealMethod();

		service.getNeighborhoodNotificationFacilities(PRIVATE_IDENTIFIER, CASE_NUMBER);

		final ArgumentCaptor<Map<String, Map<Integer, String>>> captor = ArgumentCaptor.forClass(Map.class);

		verify(mockApiResponseMapper).mapToKeyValue(captor.capture());
		assertThat(captor.getValue())
			.containsEntry("SILJE 2:65 [999999]", Map.of(123, ""));
		verifyNoMoreInteractionsWithMocks();
	}

	@Test
	void testExtractRecipientRole_rollerListaContainsPropertyOwner() {
		when(mockByggrIntegration.getRemisserByPersOrgNr(PROCESSED_PRIVATE_IDENTIFIER))
			.thenReturn(new GetRemisserByPersOrgNrResponse()
				.withGetRemisserByPersOrgNrResult(new ArrayOfRemiss()
					.withRemiss(new Remiss()
						.withDnr(CASE_NUMBER)
						.withFastighetsbeteckning("SILJE 2:65 [999999]")
						.withMottagare(new HandelseIntressent()
							.withRollLista(new ArrayOfString2().withRoll(ApiResponseMapper.ROLE_PROPERTY_OWNER)))
						.withRemissId(123))));

		when(mockApiResponseMapper.mapToKeyValue(any())).thenCallRealMethod();

		service.getNeighborhoodNotificationFacilities(PRIVATE_IDENTIFIER, CASE_NUMBER);

		final ArgumentCaptor<Map<String, Map<Integer, String>>> captor = ArgumentCaptor.forClass(Map.class);

		verify(mockApiResponseMapper).mapToKeyValue(captor.capture());
		assertThat(captor.getValue())
			.containsEntry("SILJE 2:65 [999999]", Map.of(123, ApiResponseMapper.ROLE_PROPERTY_OWNER));
		verifyNoMoreInteractionsWithMocks();
	}

	@Test
	void testExtractRecipientRole_rollerListaContainsNeighbour() {
		when(mockByggrIntegration.getRemisserByPersOrgNr(PROCESSED_PRIVATE_IDENTIFIER))
			.thenReturn(new GetRemisserByPersOrgNrResponse()
				.withGetRemisserByPersOrgNrResult(new ArrayOfRemiss()
					.withRemiss(new Remiss()
						.withDnr(CASE_NUMBER)
						.withFastighetsbeteckning("SILJE 2:65 [999999]")
						.withMottagare(new HandelseIntressent()
							.withRollLista(new ArrayOfString2().withRoll(ApiResponseMapper.ROLE_NEIGHBOUR)))
						.withRemissId(123))));

		when(mockApiResponseMapper.mapToKeyValue(any())).thenCallRealMethod();

		service.getNeighborhoodNotificationFacilities(PRIVATE_IDENTIFIER, CASE_NUMBER);

		final ArgumentCaptor<Map<String, Map<Integer, String>>> captor = ArgumentCaptor.forClass(Map.class);
		verify(mockApiResponseMapper).mapToKeyValue(captor.capture());

		assertThat(captor.getValue())
			.containsEntry("SILJE 2:65 [999999]", Map.of(123, ApiResponseMapper.ROLE_NEIGHBOUR));
		verifyNoMoreInteractionsWithMocks();
	}

	@Test
	void testExtractRecipientRole_unknownRole_returnFirst() {
		when(mockByggrIntegration.getRemisserByPersOrgNr(PROCESSED_PRIVATE_IDENTIFIER))
			.thenReturn(new GetRemisserByPersOrgNrResponse()
				.withGetRemisserByPersOrgNrResult(new ArrayOfRemiss()
					.withRemiss(new Remiss()
						.withDnr(CASE_NUMBER)
						.withFastighetsbeteckning("SILJE 2:65 [999999]")
						.withMottagare(new HandelseIntressent()
							.withRollLista(new ArrayOfString2().withRoll("UNKNOWN ROLE")))
						.withRemissId(123))));

		when(mockApiResponseMapper.mapToKeyValue(any())).thenCallRealMethod();

		service.getNeighborhoodNotificationFacilities(PRIVATE_IDENTIFIER, CASE_NUMBER);

		final ArgumentCaptor<Map<String, Map<Integer, String>>> captor = ArgumentCaptor.forClass(Map.class);
		verify(mockApiResponseMapper).mapToKeyValue(captor.capture());

		assertThat(captor.getValue())
			.containsEntry("SILJE 2:65 [999999]", Map.of(123, "UNKNOWN ROLE"));
		verifyNoMoreInteractionsWithMocks();
	}

	@Test
	void testReadFile_differentFilAndelse() throws IOException {

		final var token = "valid-token";
		final var fileId = "fileId";

		final var dokument = OBJECT_FACTORY.createDokument()
			.withDokId(fileId)
			.withNamn("random.txt")
			.withFil(OBJECT_FACTORY
				.createDokumentFil()
				.withFilAndelse("pdf")
				.withFilBuffer(DOCUMENT_CONTENT));

		when(mockByggrIntegration.getDocument(fileId))
			.thenReturn(OBJECT_FACTORY.createGetDocumentResponse()
				.withGetDocumentResult(dokument));
		when(mockHttpServletResponse.getOutputStream())
			.thenReturn(mockServletOutputStream);

		service.readFile(MUNICIPALITY_ID, fileId, token, mockHttpServletResponse);

		verify(mockFileAccessTokenService).validateToken(MUNICIPALITY_ID, fileId, token);
		verify(mockByggrIntegration).getDocument(fileId);
		verify(mockHttpServletResponse).addHeader(CONTENT_DISPOSITION, "attachment; filename=\"random.txt.pdf\"");
		verify(mockHttpServletResponse).addHeader(CONTENT_TYPE, "application/pdf");
		verify(mockHttpServletResponse).setContentLength(DOCUMENT_CONTENT.length);
		verify(mockHttpServletResponse).getOutputStream();
	}

	private void verifyNoMoreInteractionsWithMocks() {
		verifyNoMoreInteractions(mockByggrIntegration, mockByggrIntegrationMapper, mockByggrFilterUtility, mockApiResponseMapper, mockHttpServletResponse, mockFileAccessTokenService);
	}
}
