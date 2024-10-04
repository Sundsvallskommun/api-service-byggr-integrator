package se.sundsvall.byggrintegrator.integration.byggr;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import generated.se.sundsvall.arendeexport.ArrayOfString;
import generated.se.sundsvall.arendeexport.GetArendeResponse;
import generated.se.sundsvall.arendeexport.GetDocumentResponse;
import generated.se.sundsvall.arendeexport.GetRelateradeArendenByPersOrgNrAndRoleResponse;
import generated.se.sundsvall.arendeexport.Roll;
import jakarta.xml.soap.SOAPFault;
import jakarta.xml.ws.soap.SOAPFaultException;

@Component
public class ByggrIntegration {

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

	@Cacheable(value = "getErrandsCache")
	public List<GetRelateradeArendenByPersOrgNrAndRoleResponse> getErrands(String identifier, List<String> roles) {

		final var identifiers = new LinkedHashSet<String>();
		identifiers.add(identifier);
		Stream.of(identifier).filter(id -> id.startsWith("16")).map(id -> id.substring(2)).findAny().ifPresent(identifiers::add);

		return identifiers.stream()
			.map(id -> getErrandsInternal(id, roles))
			.toList();
	}

	private GetRelateradeArendenByPersOrgNrAndRoleResponse getErrandsInternal(String identifier, List<String> roles) {
		final var request = byggrIntegrationMapper.mapToGetRelateradeArendenRequest(identifier)
			.withArendeIntressentRoller(rolesToArrayOfString(roles));

		return byggrClient.getRelateradeArendenByPersOrgNrAndRole(request);
	}

	private ArrayOfString rolesToArrayOfString(List<String> roles) {
		return Objects.isNull(roles) ? null : new ArrayOfString().withString(roles);
	}

	@Cacheable(value = "getRolesCache")
	public List<String> getRoles() {
		final var roller = byggrClient.getRoller(byggrIntegrationMapper.createGetRolesRequest());

		return Optional.ofNullable(roller.getGetRollerResult())
			.map(result -> result.getRoll().stream()
				.filter(Roll::isArAktiv)
				.map(Roll::getRollKod)
				.toList())
			.orElse(List.of());
	}

	@Cacheable(value = "getErrandCache")
	public GetArendeResponse getErrand(String dnr) {
		try {
			return byggrClient.getArende(byggrIntegrationMapper.mapToGetArendeRequest(dnr));
		} catch (final SOAPFaultException e) {
			if (StringUtils.startsWithIgnoreCase(extractFaultString(e), SOAP_FAULT_PREFIX_ERRAND_NOT_FOUND)) {
				return null;
			}

			throw e;
		}
	}

	@Cacheable(value = "getDocumentCache")
	public GetDocumentResponse getDocument(String documentId) {
		try {
			return byggrClient.getDocument(byggrIntegrationMapper.mapToGetDocumentRequest(documentId));
		} catch (final SOAPFaultException e) {
			final var faultString = extractFaultString(e);
			if (StringUtils.startsWithIgnoreCase(faultString, SOAP_FAULT_PREFIX_ERROR_GETTING_DOCUMENT) ||
				StringUtils.containsIgnoreCase(faultString, SOAP_FAULT_PREFIX_DOCUMENT_ID_NOT_VALID)) {
				return null;
			}

			throw e;
		}
	}

	private String extractFaultString(final SOAPFaultException e) {
		return Optional.ofNullable(e.getFault())
			.map(SOAPFault::getFaultString)
			.orElse(EMPTY_STRING);
	}
}
