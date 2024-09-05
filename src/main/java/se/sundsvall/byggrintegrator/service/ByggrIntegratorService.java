package se.sundsvall.byggrintegrator.service;

import static java.util.Optional.ofNullable;
import static se.sundsvall.byggrintegrator.service.LegalIdUtility.addHyphen;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

import se.sundsvall.byggrintegrator.api.model.KeyValue;
import se.sundsvall.byggrintegrator.api.model.Weight;
import se.sundsvall.byggrintegrator.integration.byggr.ByggrIntegration;
import se.sundsvall.byggrintegrator.integration.byggr.ByggrIntegrationMapper;

@Service
public class ByggrIntegratorService {

	private final ByggrIntegrationMapper byggrIntegrationMapper;
	private final ByggrIntegration byggrIntegration;
	private final ApiResponseMapper apiResponseMapper;

	public ByggrIntegratorService(ByggrIntegrationMapper byggrIntegrationMapper, ByggrIntegration byggrIntegration, ApiResponseMapper apiResponseMapper) {
		this.byggrIntegrationMapper = byggrIntegrationMapper;
		this.byggrIntegration = byggrIntegration;
		this.apiResponseMapper = apiResponseMapper;
	}

	public List<KeyValue> findNeighborhoodNotifications(String identifier) {
		final var roles = byggrIntegration.getRoles();
		if (CollectionUtils.isEmpty(roles)) {
			throw Problem.builder()
				.withStatus(Status.NOT_FOUND)
				.withTitle("No roles found")
				.withDetail("Cannot continue fetching neighborhood notifications")
				.build();
		}

		final var matches = byggrIntegration.getErrands(addHyphen(identifier), roles); // Add hyphen to identifier as ByggR integration formats legal id that way

		final var byggrErrandList = byggrIntegrationMapper.mapToNeighborhoodNotifications(matches);

		return apiResponseMapper.mapToKeyValueResponseList(byggrErrandList);
	}

	public List<KeyValue> findApplicantErrands(String identifier) {
		final var identifierWithHyphen = addHyphen(identifier); // Add hyphen to identifier as ByggR integration formats legal id that way

		final var matches = byggrIntegration.getErrands(identifierWithHyphen, null);

		final var byggrErrandList = byggrIntegrationMapper.mapToApplicantErrands(matches, identifierWithHyphen);

		return apiResponseMapper.mapToKeyValueResponseList(byggrErrandList);
	}

	public Weight getErrandType(String dnr) {
		final var errand = byggrIntegration.getErrand(dnr);

		return ofNullable(errand)
			.map(apiResponseMapper::mapToWeight)
			.orElseThrow(() -> Problem.builder()
				.withStatus(Status.NOT_FOUND)
				.withTitle(Status.NOT_FOUND.getReasonPhrase())
				.withDetail("No errand with diary number %s was found".formatted(dnr))
				.build());
	}
}
