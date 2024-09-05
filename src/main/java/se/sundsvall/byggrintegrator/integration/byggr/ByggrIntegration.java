package se.sundsvall.byggrintegrator.integration.byggr;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Component;

import generated.se.sundsvall.arendeexport.ArrayOfString;
import generated.se.sundsvall.arendeexport.GetArendeResponse;
import generated.se.sundsvall.arendeexport.GetRelateradeArendenByPersOrgNrAndRoleResponse;
import generated.se.sundsvall.arendeexport.Roll;
import jakarta.xml.soap.SOAPFault;
import jakarta.xml.ws.soap.SOAPFaultException;

@Component
public class ByggrIntegration {

	private static final String SOAP_FAULT_PREFIX_ERRAND_NOT_FOUND = "Arende not found for dnr";
	private static final String EMPTY_STRING = "";

	private final ByggrClient byggrClient;
	private final ByggrIntegrationMapper byggrIntegrationMapper;

	public ByggrIntegration(ByggrClient byggrClient, final ByggrIntegrationMapper byggrIntegrationMapper) {
		this.byggrClient = byggrClient;
		this.byggrIntegrationMapper = byggrIntegrationMapper;
	}

	public GetRelateradeArendenByPersOrgNrAndRoleResponse getErrands(String identifier, List<String> roles) {
		final var request = byggrIntegrationMapper.mapToGetRelateradeArendenRequest(identifier)
			.withArendeIntressentRoller(rolesToArrayOfString(roles));

		return byggrClient.getRelateradeArendenByPersOrgNrAndRole(request);
	}

	private ArrayOfString rolesToArrayOfString(List<String> roles) {
		return Objects.isNull(roles) ? null : new ArrayOfString().withString(roles);
	}

	public List<String> getRoles() {
		final var roller = byggrClient.getRoller(byggrIntegrationMapper.createGetRolesRequest());

		return Optional.ofNullable(roller.getGetRollerResult())
			.map(result -> result.getRoll().stream()
				.filter(Roll::isArAktiv)
				.map(Roll::getRollKod)
				.toList())
			.orElse(List.of());
	}

	public GetArendeResponse getErrand(String dnr) {
		try {
			return byggrClient.getArende(byggrIntegrationMapper.mapToGetArendeRequest(dnr));
		} catch (final SOAPFaultException e) {
			if (extractFaultString(e).startsWith(SOAP_FAULT_PREFIX_ERRAND_NOT_FOUND)) {
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
