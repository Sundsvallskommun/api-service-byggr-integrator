package se.sundsvall.byggrintegrator.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import se.sundsvall.byggrintegrator.api.model.KeyValue;
import se.sundsvall.byggrintegrator.model.NeighborhoodNotificationsDto;

@Component
public class ApiResponseMapper {

	public List<KeyValue> mapToKeyValueResponseList(List<NeighborhoodNotificationsDto> errands) {
		List<KeyValue> response = new ArrayList<>();

		errands.forEach(
			errand -> errand.getPropertyDesignation().forEach(
				designation -> response.add(mapToKeyValue(errand.getByggrErrandNumber(), designation))));

		return response;
	}

	private KeyValue mapToKeyValue(String dnr, NeighborhoodNotificationsDto.PropertyDesignation designation) {
		return new KeyValue(dnr, dnr + ", " + designation.getProperty() + " " + designation.getDesignation());
	}
}
