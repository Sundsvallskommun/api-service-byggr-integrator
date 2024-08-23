package se.sundsvall.byggrintegrator.integration.byggr;

import org.springframework.stereotype.Component;

@Component
public class ByggRIntegration {

	private final ByggrClient byggrClient;

	public ByggRIntegration(ByggrClient byggrClient) {
		this.byggrClient = byggrClient;
	}

	public void findNeighborNotifications(String identifier) {
		// TODO implement
	}
}
