package se.sundsvall.byggrintegrator.service;

import generated.se.sundsvall.arendeexport.v8.GetArendeResponse;
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

	private static final String KEY_TEMPLATE = "%s%s [%d]";

	public List<KeyValue> mapToKeyValueResponseList(final List<ByggrErrandDto> errands) {
		final var position = new AtomicInteger(1);

		return errands.stream()
			.filter(Objects::nonNull)
			.sorted(Comparator.comparing(ByggrErrandDto::getByggrCaseNumber))
			.map(errand -> mapToKeyValue(position.getAndIncrement(), errand.getByggrCaseNumber()))
			.toList();
	}

	private KeyValue mapToKeyValue(final int position, final String value) {
		return new KeyValue(String.valueOf(position), value);
	}

	public Weight mapToWeight(final GetArendeResponse errand) {
		return Weight.builder()
			.withValue(CaseTypeEnum.translate(errand.getGetArendeResult().getArendetyp()))
			.build();
	}

	public List<KeyValue> mapToKeyValue(final Map<String, Map<Integer, String>> propertyDesignationAndRemissIdMap) {
		final var position = new AtomicInteger(1);

		return propertyDesignationAndRemissIdMap.entrySet().stream()
			.flatMap(entry -> formatRemissInfo(entry.getKey(), entry.getValue()).stream())
			.map(value -> mapToKeyValue(position.getAndIncrement(), value))
			.toList();
	}

	private List<String> formatRemissInfo(final String propertyDesignation, final Map<Integer, String> remissIdToSvarDatumMap) {
		return remissIdToSvarDatumMap.entrySet().stream()
			.map(entry -> {
				final var remissId = entry.getKey();
				final var svarDatum = entry.getValue();

				final var statusText = (svarDatum == null || svarDatum.isBlank() || "nil".equalsIgnoreCase(svarDatum))
					? " - ej besvarad"
					: " - besvarad " + svarDatum.split("T")[0];

				return KEY_TEMPLATE.formatted(propertyDesignation, statusText, remissId);
			})
			.toList();
	}
}
