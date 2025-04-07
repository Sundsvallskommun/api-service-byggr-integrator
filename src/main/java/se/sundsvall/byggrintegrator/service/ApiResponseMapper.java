package se.sundsvall.byggrintegrator.service;

import generated.se.sundsvall.arendeexport.GetArendeResponse;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Component;
import se.sundsvall.byggrintegrator.api.model.KeyValue;
import se.sundsvall.byggrintegrator.api.model.Weight;
import se.sundsvall.byggrintegrator.model.ByggrErrandDto;

@Component
public class ApiResponseMapper {

	public List<KeyValue> mapToKeyValueResponseList(List<ByggrErrandDto> errands) {
		final var position = new AtomicInteger(1);

		return errands.stream()
			.filter(Objects::nonNull)
			.sorted(Comparator.comparing(ByggrErrandDto::getByggrCaseNumber))
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

	public List<KeyValue> mapToKeyValue(final Map<String, Integer> propertyDesignationAndRemissIdMap) {
		final var position = new AtomicInteger(1);

		return propertyDesignationAndRemissIdMap.entrySet().stream()
			.map(entry -> KEY_TEMPLATE.formatted(entry.getKey(), entry.getValue()))
			.map(value -> mapToKeyValue(position.getAndIncrement(), value))
			.toList();
	}
}
