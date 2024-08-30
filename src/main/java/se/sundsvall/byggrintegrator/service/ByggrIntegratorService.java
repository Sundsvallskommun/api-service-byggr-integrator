package se.sundsvall.byggrintegrator.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

import se.sundsvall.byggrintegrator.api.model.KeyValue;
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
		var roles = byggrIntegration.getRoles();
		if (CollectionUtils.isEmpty(roles)) {
			throw Problem.builder()
				.withStatus(Status.NOT_FOUND)
				.withTitle("No roles found")
				.withDetail("Cannot continue fetching neighborhood notifications")
				.build();
		}

		var neighborNotifications = byggrIntegration.getErrandsFromByggr(identifier, roles);

		var byggrErrandList = byggrIntegrationMapper.mapToNeighborhoodNotificationsDto(neighborNotifications);

		return apiResponseMapper.mapToKeyValueResponseList(byggrErrandList);
	}
}
