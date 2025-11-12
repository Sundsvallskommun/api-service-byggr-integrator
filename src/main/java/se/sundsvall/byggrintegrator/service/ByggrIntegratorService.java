package se.sundsvall.byggrintegrator.service;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.util.StreamUtils.copy;
import static org.zalando.problem.Status.INTERNAL_SERVER_ERROR;
import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.byggrintegrator.service.util.LegalIdUtility.addHyphen;
import static se.sundsvall.byggrintegrator.service.util.LegalIdUtility.prefixOrgnbr;
import static se.sundsvall.byggrintegrator.service.util.MimeTypeUtility.detectMimeType;

import generated.se.sundsvall.arendeexport.v4.Remiss;
import generated.se.sundsvall.arendeexport.v8.Dokument;
import generated.se.sundsvall.arendeexport.v8.GetDocumentResponse;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.xml.datatype.XMLGregorianCalendar;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.zalando.problem.Problem;
import org.zalando.problem.StatusType;
import org.zalando.problem.ThrowableProblem;
import se.sundsvall.byggrintegrator.api.model.KeyValue;
import se.sundsvall.byggrintegrator.api.model.Weight;
import se.sundsvall.byggrintegrator.integration.byggr.ByggrIntegration;
import se.sundsvall.byggrintegrator.integration.byggr.ByggrIntegrationMapper;
import se.sundsvall.byggrintegrator.service.template.TemplateMapper;
import se.sundsvall.byggrintegrator.service.util.ByggrFilterUtility;

@Service
public class ByggrIntegratorService {

	public static final String TEMPLATE_CONTENT_DISPOSITION_HEADER_VALUE = "attachment; filename=\"%s\"";
	public static final String ERROR_ROLES_NOT_FOUND = "No roles found, cannot continue fetching neighborhood notifications";
	public static final String ERROR_ERRAND_NOT_FOUND = "No errand with diary number %s was found";
	public static final String ERROR_FILE_NOT_FOUND = "No file with id %s was found";
	public static final String ERROR_FILE_COULD_NOT_BE_READ = "Could not read file content for document data with id %s";

	private static final List<String> SUPPRESSED_HANDELSE_HANDLING_TYPE = List.of("GRA");

	private final ByggrIntegrationMapper byggrIntegrationMapper;
	private final ByggrIntegration byggrIntegration;
	private final ApiResponseMapper apiResponseMapper;
	private final TemplateMapper templateMapper;
	private final ByggrFilterUtility filterUtility;

	public ByggrIntegratorService(final ByggrIntegrationMapper byggrIntegrationMapper, final ByggrIntegration byggrIntegration, final ApiResponseMapper apiResponseMapper, final TemplateMapper templateMapper, final ByggrFilterUtility filterUtility) {
		this.byggrIntegrationMapper = byggrIntegrationMapper;
		this.byggrIntegration = byggrIntegration;
		this.apiResponseMapper = apiResponseMapper;
		this.templateMapper = templateMapper;
		this.filterUtility = filterUtility;
	}

	@Cacheable("findNeighborhoodNotificationsCache")
	public List<KeyValue> findNeighborhoodNotifications(final String identifier) {
		final var roles = byggrIntegration.getRoles();
		if (CollectionUtils.isEmpty(roles)) {
			throw createProblem(NOT_FOUND, ERROR_ROLES_NOT_FOUND);
		}

		// Prefix identifier if it contains organisation legal id and add hyphen to identifier as ByggR integration formats
		// legal id that way
		final var processedIdentifier = addHyphen(prefixOrgnbr(identifier));
		// Fetch answer from ByggR
		final var result = byggrIntegration.getErrands(processedIdentifier, roles);
		// Filter on neighborhood notifications where identifier matches stakeholder
		final var matches = filterUtility.filterNeighborhoodNotifications(byggrIntegrationMapper.mapToByggrErrandDtos(result), processedIdentifier);

		// Map to API response
		return apiResponseMapper.mapToKeyValueResponseList(matches);
	}

	@Cacheable("findApplicantErrandsCache")
	public List<KeyValue> findApplicantErrands(final String identifier) {
		// Prefix identifier if it contains organisation legal id and add hyphen to identifier as ByggR integration formats
		// legal id that way
		final var processedIdentifier = addHyphen(prefixOrgnbr(identifier));
		// Fetch answer from ByggR
		final var result = byggrIntegration.getErrands(processedIdentifier, null);
		// Filter on errands where applicant matches identifier
		final var matches = filterUtility.filterCasesForApplicant(byggrIntegrationMapper.mapToByggrErrandDtos(result), processedIdentifier);

		// Map to API response
		return apiResponseMapper.mapToKeyValueResponseList(matches);
	}

	@Cacheable("getPropertyDesignationCache")
	public String getPropertyDesignation(final String caseNumber) {
		final var errand = byggrIntegration.getErrand(caseNumber);

		final var byggrErrand = byggrIntegrationMapper.mapToByggrErrandDto(errand);

		return templateMapper.getDescriptionAndPropertyDesignation(byggrErrand);
	}

	@Cacheable("getErrandTypeCache")
	public Weight getErrandType(final String caseNumber) {
		final var errand = byggrIntegration.getErrand(caseNumber);

		return ofNullable(errand)
			.map(apiResponseMapper::mapToWeight)
			.orElseThrow(() -> createProblem(NOT_FOUND, ERROR_ERRAND_NOT_FOUND.formatted(caseNumber)));
	}

	@Cacheable("listNeighborhoodNotificationFilesCache")
	public String listNeighborhoodNotificationFiles(final String municipalityId, final String identifier, final String caseNumber, final String referralReference) {
		// Fetch errand from ByggR
		final var result = byggrIntegration.getErrand(caseNumber);

		// Extract referralReferenceId
		final var pattern = Pattern.compile("\\[(\\d+)]");
		final int referralReferenceId = Optional.ofNullable(referralReference)
			.map(pattern::matcher)
			.filter(Matcher::find)
			.map(m -> Integer.parseInt(m.group(1)))
			.orElse(0);

		// Prefix identifier if it contains organizations legal id and add hyphen to identifier as ByggR integration formats
		// legal id that way
		final var processedIdentifier = addHyphen(prefixOrgnbr(identifier));

		final var remissResult = byggrIntegration.getRemisserByPersOrgNr(processedIdentifier).getGetRemisserByPersOrgNrResult()
			.getRemiss()
			.stream()
			.filter(remiss -> referralReferenceId == remiss.getRemissId()).findFirst()
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, "Remiss not found"));

		// Filter out unwanted types (e.g. GRA).
		final var filteredHandelseHandlingList = Optional.ofNullable(remissResult.getUtskicksHandlingar().getHandling()).orElse(emptyList())
			.stream()
			.filter(Objects::nonNull)
			.filter(handelseHandling -> !SUPPRESSED_HANDELSE_HANDLING_TYPE.contains(defaultIfBlank(handelseHandling.getTyp(), "")))
			.toList();

		// Filter on event that matches incoming id
		final var match = filterUtility.filterEvents(processedIdentifier, byggrIntegrationMapper.mapToByggrErrandDto(result));

		// Fetch "handlingtyper" from ByggR
		final var handlingtyper = byggrIntegration.getHandlingTyper();

		// Map to API response
		return templateMapper.generateFileList(municipalityId, match, handlingtyper, filteredHandelseHandlingList);
	}

	public void readFile(final String fileId, final HttpServletResponse response) {
		ofNullable(byggrIntegration.getDocument(fileId))
			.map(GetDocumentResponse::getGetDocumentResult)
			.map(List::getFirst)
			.ifPresentOrElse(byggRFile -> addToResponse(fileId, response, byggRFile), () -> {
				throw createProblem(NOT_FOUND, ERROR_FILE_NOT_FOUND.formatted(fileId));
			});
	}

	public List<KeyValue> getNeighborhoodNotificationFacilities(final String identifier, final String caseNumber) {
		final var processedIdentifier = addHyphen(prefixOrgnbr(identifier));

		final var remisser = byggrIntegration.getRemisserByPersOrgNr(processedIdentifier);

		final var propertyDesignationAndRemissIdMap = remisser.getGetRemisserByPersOrgNrResult().getRemiss().stream()
			// Filter to only include remisser with the given case number
			.filter(remiss -> caseNumber.equals(remiss.getDnr()) && remiss.getFastighetsbeteckning() != null)
			// Creates a map where the propertyDesignation is the key and the value is a map containing remissId and svarDatum
			.collect(Collectors.toMap(Remiss::getFastighetsbeteckning,
				remiss -> Map.of(remiss.getRemissId(),
					Optional.ofNullable(remiss.getSvarDatum()).map(XMLGregorianCalendar::toString).orElse("")),
				(map1, map2) -> {
					var merged = new LinkedHashMap<>(map1);
					merged.putAll(map2);
					return merged;
				}));

		return apiResponseMapper.mapToKeyValue(propertyDesignationAndRemissIdMap);
	}

	private void addToResponse(final String documentId, final HttpServletResponse response, final Dokument byggRFile) {
		try {
			byggRFile.setNamn(decorateNameWithExtension(byggRFile.getNamn(), byggRFile.getFil().getFilAndelse()));

			response.addHeader(CONTENT_TYPE, detectMimeType(byggRFile.getNamn(), byggRFile.getFil().getFilBuffer()));
			response.addHeader(CONTENT_DISPOSITION, TEMPLATE_CONTENT_DISPOSITION_HEADER_VALUE.formatted(byggRFile.getNamn()));
			response.setContentLength(byggRFile.getFil().getFilBuffer().length);

			copy(new ByteArrayInputStream(byggRFile.getFil().getFilBuffer()), response.getOutputStream());
		} catch (final IOException e) {
			throw createProblem(INTERNAL_SERVER_ERROR, ERROR_FILE_COULD_NOT_BE_READ.formatted(documentId));
		}
	}

	private String decorateNameWithExtension(final String name, final String extension) {
		if (extension == null) {
			return name;
		}

		final var dotIndex = name.lastIndexOf(".");
		if (dotIndex != -1) {
			final var fileExtension = name.substring(dotIndex + 1);
			if (fileExtension.equalsIgnoreCase(extension)) {
				return name;
			}
		}
		return name + "." + extension.toLowerCase();
	}

	private ThrowableProblem createProblem(final StatusType status, final String detail) {
		return Problem.builder()
			.withStatus(status)
			.withTitle(status.getReasonPhrase())
			.withDetail(detail)
			.build();
	}
}
