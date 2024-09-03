package se.sundsvall.byggrintegrator.service;

import java.util.List;

import org.springframework.stereotype.Component;

import se.sundsvall.byggrintegrator.api.model.KeyValue;
import se.sundsvall.byggrintegrator.model.ByggrErrandDto;

@Component
public class ApiResponseMapper {

	public List<KeyValue> mapToKeyValueResponseList(List<ByggrErrandDto> errands) {
		return errands.stream()
			.flatMap(errand -> errand.getPropertyDesignation().stream()
				.map(designation -> mapToKeyValue(errand.getByggrErrandNumber(), designation)))
			.toList();
	}

	private KeyValue mapToKeyValue(String dnr, ByggrErrandDto.PropertyDesignation designation) {
		return new KeyValue(dnr, dnr + ", " + designation.getProperty() + " " + designation.getDesignation());
	}
}
