package se.sundsvall.byggrintegrator.service;

import org.springframework.stereotype.Service;

import se.sundsvall.byggrintegrator.integration.byggr.ByggRIntegration;

@Service
public class ByggrIntegratorService {

	private final ByggRIntegration byggrIntegration;

	public ByggrIntegratorService(ByggRIntegration byggrIntegration) {
		this.byggrIntegration = byggrIntegration;
	}

	public void findNeighborNotifications(String identifier) {
		// TODO implement
	}
}
