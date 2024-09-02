package se.sundsvall.byggrintegrator.integration.byggr;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import generated.se.sundsvall.arendeexport.ArrayOfString;
import generated.se.sundsvall.arendeexport.GetRelateradeArendenByPersOrgNrAndRoleResponse;
import generated.se.sundsvall.arendeexport.Roll;

@Component
public class ByggrIntegration {

	private final ByggrClient byggrClient;
	private final ByggrIntegrationMapper byggrIntegrationMapper;

	public ByggrIntegration(ByggrClient byggrClient, final ByggrIntegrationMapper byggrIntegrationMapper) {
		this.byggrClient = byggrClient;
		this.byggrIntegrationMapper = byggrIntegrationMapper;
	}

	public GetRelateradeArendenByPersOrgNrAndRoleResponse getErrandsFromByggr(String identifier, List<String> roles) {
		var request = byggrIntegrationMapper.mapToGetRelateradeArendenRequest(identifier)
			.withArendeIntressentRoller(rolesToArrayOfString(roles));

		return byggrClient.getRelateradeArendenByPersOrgNrAndRole(request);
	}

	private ArrayOfString rolesToArrayOfString(List<String> roles) {
		return new ArrayOfString().withString(roles);
	}

	public List<String> getRoles() {
		var roller = byggrClient.getRoller(byggrIntegrationMapper.createGetRolesRequest());

		return Optional.ofNullable(roller.getGetRollerResult())
			.map(result -> result.getRoll().stream()
				.filter(Roll::isArAktiv)
				.map(Roll::getRollKod)
				.toList())
			.orElse(List.of());
	}
}
