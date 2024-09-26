package se.sundsvall.byggrintegrator.service;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

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

	private static final String KEY_TEMPLATE = "%s [%s]";

	public List<KeyValue> mapToNeighborhoodKeyValueResponseList(List<ByggrErrandDto> errands) {
		final var position = new AtomicInteger(1);

		return errands.stream()
			.filter(Objects::nonNull)
			.map(this::decorateWithEventIds)
			.flatMap(List::stream)
			.sorted()
			.map(value -> mapToKeyValue(position.getAndIncrement(), value))
			.toList();
	}

	private List<String> decorateWithEventIds(ByggrErrandDto dto) {
		return ofNullable(dto.getEvents()).orElse(emptyList())
			.stream()
			.map(event -> KEY_TEMPLATE.formatted(dto.getByggrCaseNumber(), event.getId()))
			.toList();
	}

	public List<KeyValue> mapToKeyValueResponseList(List<ByggrErrandDto> errands) {
		final var position = new AtomicInteger(1);

		return errands.stream()
			.filter(Objects::nonNull)
			.sorted((o1, o2) -> o1.getByggrCaseNumber().compareTo(o2.getByggrCaseNumber()))
			.map(errand -> mapToKeyValue(position.getAndIncrement(), errand.getByggrCaseNumber()))
			.toList();
	}

	private KeyValue mapToKeyValue(int position, String value) {
		return new KeyValue(String.valueOf(position), value);
	}

	public Weight mapToWeight(GetArendeResponse errand) {
		return Weight.builder()
			.withValue(CaseTypeEnum.translate(errand.getGetArendeResult().getArendetyp()))
			.build();
	}
}
