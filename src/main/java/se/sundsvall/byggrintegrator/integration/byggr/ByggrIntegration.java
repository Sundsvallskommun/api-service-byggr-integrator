package se.sundsvall.byggrintegrator.integration.byggr;

import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.startsWithIgnoreCase;

import generated.se.sundsvall.arendeexport.v4.GetRemisserByPersOrgNr;
import generated.se.sundsvall.arendeexport.v4.GetRemisserByPersOrgNrResponse;
import generated.se.sundsvall.arendeexport.v4.RemissStatusFilter;
import generated.se.sundsvall.arendeexport.v8.ArrayOfString;
import generated.se.sundsvall.arendeexport.v8.GetArendeResponse;
import generated.se.sundsvall.arendeexport.v8.GetDocumentResponse;
import generated.se.sundsvall.arendeexport.v8.GetRelateradeArendenByPersOrgNrAndRoleResponse;
import generated.se.sundsvall.arendeexport.v8.HandlingTyp;
import generated.se.sundsvall.arendeexport.v8.Roll;
import jakarta.xml.soap.SOAPFault;
import jakarta.xml.ws.soap.SOAPFaultException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
public class ByggrIntegration {

	private static final Logger LOG = LoggerFactory.getLogger(ByggrIntegration.class);

	private static final String SOAP_FAULT_PREFIX_ERRAND_NOT_FOUND = "Arende not found for dnr";
	private static final String SOAP_FAULT_PREFIX_ERROR_GETTING_DOCUMENT = "Error getting GemDmsdoclink";
	private static final String SOAP_FAULT_PREFIX_DOCUMENT_ID_NOT_VALID = "is not a numeric value";
	private static final String EMPTY_STRING = "";

	private final ByggrClient byggrClient;
	private final ByggrIntegrationMapper byggrIntegrationMapper;

	public ByggrIntegration(final ByggrClient byggrClient,
		final ByggrIntegrationMapper byggrIntegrationMapper) {
		this.byggrClient = byggrClient;
		this.byggrIntegrationMapper = byggrIntegrationMapper;
	}

	public List<GetRelateradeArendenByPersOrgNrAndRoleResponse> getErrands(final String identifier, final List<String> roles) {
		var identifiers = new LinkedHashSet<String>();
		identifiers.add(identifier);
		Stream.of(identifier).filter(id -> id.startsWith("16")).map(id -> id.substring(2)).findAny().ifPresent(identifiers::add);

		return identifiers.stream()
			.map(id -> getErrandsInternal(id, roles))
			.toList();
	}

	private GetRelateradeArendenByPersOrgNrAndRoleResponse getErrandsInternal(final String identifier, final List<String> roles) {
		var request = byggrIntegrationMapper.mapToGetRelateradeArendenRequest(identifier)
			.withHandelseIntressentRoller(rolesToArrayOfString(roles));

		return byggrClient.getRelateradeArendenByPersOrgNrAndRole(request);
	}

	private ArrayOfString rolesToArrayOfString(final List<String> roles) {
		return Objects.isNull(roles) ? null : new ArrayOfString().withString(roles);
	}

	@Cacheable("getRolesCache")
	public List<String> getRoles() {
		var roller = byggrClient.getRoller(byggrIntegrationMapper.createGetRolesRequest());

		return Optional.ofNullable(roller.getGetRollerResult())
			.map(result -> result.getRoll().stream()
				.filter(Roll::isArAktiv)
				.map(Roll::getRollKod)
				.toList())
			.orElse(List.of());
	}

	public GetArendeResponse getErrand(final String dnr) {
		try {
			return byggrClient.getArende(byggrIntegrationMapper.mapToGetArendeRequest(dnr));
		} catch (final SOAPFaultException e) {
			var faultString = extractFaultString(e);
			if (startsWithIgnoreCase(faultString, SOAP_FAULT_PREFIX_ERRAND_NOT_FOUND)) {
				LOG.warn(faultString);

				return null;
			}

			throw e;
		}
	}

	@Cacheable("getDocumentCache")
	public GetDocumentResponse getDocument(final String documentId) {
		try {
			return byggrClient.getDocument(byggrIntegrationMapper.mapToGetDocumentRequest(documentId));
		} catch (SOAPFaultException e) {
			var faultString = extractFaultString(e);
			if (startsWithIgnoreCase(faultString, SOAP_FAULT_PREFIX_ERROR_GETTING_DOCUMENT) ||
				containsIgnoreCase(faultString, SOAP_FAULT_PREFIX_DOCUMENT_ID_NOT_VALID)) {
				LOG.warn(faultString);

				return null;
			}

			throw e;
		}
	}

	@Cacheable("getHandlingTyperCache")
	public Map<String, String> getHandlingTyper() {
		var response = byggrClient.getHandlingTyper(byggrIntegrationMapper.createGetHandlingTyperRequest());

		return response.getGetHandlingTyperResult().getHandlingTyp().stream()
			.collect(Collectors.toMap(HandlingTyp::getTyp, HandlingTyp::getBeskrivning));
	}

	public GetRemisserByPersOrgNrResponse getRemisserByPersOrgNr(final String identifier) {
		return byggrClient.getRemisserByPersOrgNr(new GetRemisserByPersOrgNr()
			.withPersOrgNr(identifier)
			.withStatusFilter(RemissStatusFilter.NONE));
	}

	private String extractFaultString(final SOAPFaultException e) {
		return Optional.ofNullable(e.getFault())
			.map(SOAPFault::getFaultString)
			.orElse(EMPTY_STRING);
	}
}
