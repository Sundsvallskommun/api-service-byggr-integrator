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

	private static final String KEY_TEMPLATE = "%s – Lämna svar som %s [%d]";
	static final String ROLE_NEIGHBOUR = "GRAN";
	static final String ROLE_PROPERTY_OWNER = "FAG";
	private static final String ROLE_TEXT_NEIGHBOUR = "granne";
	private static final String ROLE_TEXT_PROPERTY_OWNER = "fastighetsägare";

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

	private List<String> formatRemissInfo(final String propertyDesignation, final Map<Integer, String> remissIdToRoleMap) {
		return remissIdToRoleMap.entrySet().stream()
			.map(entry -> {
				final var remissId = entry.getKey();
				final var roleText = toRoleText(entry.getValue());
				if (roleText == null) {
					return null;
				}
				return KEY_TEMPLATE.formatted(propertyDesignation, roleText, remissId);
			})
			.filter(Objects::nonNull)
			.toList();
	}

	private String toRoleText(final String role) {
		if (ROLE_NEIGHBOUR.equalsIgnoreCase(role)) {
			return ROLE_TEXT_NEIGHBOUR;
		}
		if (ROLE_PROPERTY_OWNER.equalsIgnoreCase(role)) {
			return ROLE_TEXT_PROPERTY_OWNER;
		}
		return null;
	}
}
