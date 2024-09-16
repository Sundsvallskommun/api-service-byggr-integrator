package se.sundsvall.byggrintegrator.service;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Component;

import generated.se.sundsvall.arendeexport.GetArendeResponse;
import se.sundsvall.byggrintegrator.api.model.KeyValue;
import se.sundsvall.byggrintegrator.api.model.Weight;
import se.sundsvall.byggrintegrator.model.ByggrErrandDto;

@Component
public class ApiResponseMapper {

	public List<KeyValue> mapToKeyValueResponseList(List<ByggrErrandDto> errands) {
		final var position = new AtomicInteger(1);

		return errands.stream()
			.filter(Objects::nonNull)
			.sorted((o1, o2) -> o1.getByggrCaseNumber().compareTo(o2.getByggrCaseNumber()))
			.map(errand -> mapToKeyValue(position.getAndIncrement(), errand.getByggrCaseNumber()))
			.toList();
	}

	private KeyValue mapToKeyValue(int position, String dnr) {
		return new KeyValue(String.valueOf(position), dnr);
	}

	public Weight mapToWeight(GetArendeResponse errand) {
		return Weight.builder()
			.withValue(errand.getGetArendeResult().getArendetyp())
			.build();
	}
}
