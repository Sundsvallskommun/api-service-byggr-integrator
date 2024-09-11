package se.sundsvall.byggrintegrator.service;

import static java.util.Optional.ofNullable;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.util.StreamUtils.copy;
import static org.zalando.problem.Status.INTERNAL_SERVER_ERROR;
import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.byggrintegrator.service.LegalIdUtility.addHyphen;
import static se.sundsvall.byggrintegrator.service.LegalIdUtility.prefixOrgnbr;
import static se.sundsvall.byggrintegrator.service.MimeTypeUtility.detectMimeType;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.zalando.problem.Problem;
import org.zalando.problem.StatusType;
import org.zalando.problem.ThrowableProblem;

import generated.se.sundsvall.arendeexport.Dokument;
import generated.se.sundsvall.arendeexport.GetDocumentResponse;
import jakarta.servlet.http.HttpServletResponse;
import se.sundsvall.byggrintegrator.api.model.KeyValue;
import se.sundsvall.byggrintegrator.api.model.Weight;
import se.sundsvall.byggrintegrator.integration.byggr.ByggrIntegration;
import se.sundsvall.byggrintegrator.integration.byggr.ByggrIntegrationMapper;
import se.sundsvall.byggrintegrator.service.template.TemplateMapper;

@Service
public class ByggrIntegratorService {

	public static final String TEMPLATE_CONTENT_DISPOSITION_HEADER_VALUE = "attachment; filename=\"%s\"";
	public static final String ERROR_ROLES_NOT_FOUND = "No roles found, cannot continue fetching neighborhood notifications";
	public static final String ERROR_ERRAND_NOT_FOUND = "No errand with diary number %s was found";
	public static final String ERROR_FILE_NOT_FOUND = "No file with id %s was found";
	public static final String ERROR_FILE_COULD_NOT_BE_READ = "Could not read file content for document data with id %s";

	private final ByggrIntegrationMapper byggrIntegrationMapper;
	private final ByggrIntegration byggrIntegration;
	private final ApiResponseMapper apiResponseMapper;
	private final TemplateMapper templateMapper;

	public ByggrIntegratorService(ByggrIntegrationMapper byggrIntegrationMapper, ByggrIntegration byggrIntegration, ApiResponseMapper apiResponseMapper, TemplateMapper templateMapper) {
		this.byggrIntegrationMapper = byggrIntegrationMapper;
		this.byggrIntegration = byggrIntegration;
		this.apiResponseMapper = apiResponseMapper;
		this.templateMapper = templateMapper;
	}

	public List<KeyValue> findNeighborhoodNotifications(String identifier) {
		final var roles = byggrIntegration.getRoles();
		if (CollectionUtils.isEmpty(roles)) {
			throw createProblem(NOT_FOUND, ERROR_ROLES_NOT_FOUND);
		}

		// Prefix identifier if it contains organisation legal id and add hyphen to identifier as ByggR integration formats
		// legal id that way
		final var processedIdentifier = addHyphen(prefixOrgnbr(identifier));
		final var matches = byggrIntegration.getErrands(processedIdentifier, roles);

		final var byggrErrandList = byggrIntegrationMapper.mapToNeighborhoodNotifications(matches);

		return apiResponseMapper.mapToKeyValueResponseList(byggrErrandList);
	}

	public List<KeyValue> findApplicantErrands(String identifier) {
		// Prefix identifier if it contains organisation legal id and add hyphen to identifier as ByggR integration formats
		// legal id that way
		final var processedIdentifier = addHyphen(prefixOrgnbr(identifier));

		final var matches = byggrIntegration.getErrands(processedIdentifier, null);

		final var byggrErrandList = byggrIntegrationMapper.mapToApplicantErrands(matches, processedIdentifier);

		return apiResponseMapper.mapToKeyValueResponseList(byggrErrandList);
	}

	public Weight getErrandType(String dnr) {
		final var errand = byggrIntegration.getErrand(dnr);

		return ofNullable(errand)
			.map(apiResponseMapper::mapToWeight)
			.orElseThrow(() -> createProblem(NOT_FOUND, ERROR_ERRAND_NOT_FOUND.formatted(dnr)));
	}

	public String listNeighborhoodNotificationFiles(String municipalityId, String caseNumber) {
		final var response = byggrIntegration.getErrand(caseNumber);
		final var byggrErrandDto = byggrIntegrationMapper.mapToNeighborhoodNotificationFiles(response);
		return templateMapper.generateFileList(municipalityId, byggrErrandDto);
	}

	public void readFile(String fileId, HttpServletResponse response) {
		ofNullable(byggrIntegration.getDocument(fileId))
			.map(GetDocumentResponse::getGetDocumentResult)
			.map(List::getFirst)
			.ifPresentOrElse(byggRFile -> addToResponse(fileId, response, byggRFile), () -> {
				throw createProblem(NOT_FOUND, ERROR_FILE_NOT_FOUND.formatted(fileId));
			});
	}

	private void addToResponse(String documentId, HttpServletResponse response, final Dokument byggRFile) {
		try {
			response.addHeader(CONTENT_TYPE, detectMimeType(byggRFile.getNamn(), byggRFile.getFil().getFilBuffer()));
			response.addHeader(CONTENT_DISPOSITION, TEMPLATE_CONTENT_DISPOSITION_HEADER_VALUE.formatted(byggRFile.getNamn()));
			response.setContentLength(byggRFile.getFil().getFilBuffer().length);

			copy(new ByteArrayInputStream(byggRFile.getFil().getFilBuffer()), response.getOutputStream());
		} catch (final IOException e) {
			throw createProblem(INTERNAL_SERVER_ERROR, ERROR_FILE_COULD_NOT_BE_READ.formatted(documentId));
		}
	}

	private ThrowableProblem createProblem(StatusType status, String detail) {
		return Problem.builder()
			.withStatus(status)
			.withTitle(status.getReasonPhrase())
			.withDetail(detail)
			.build();
	}
}
