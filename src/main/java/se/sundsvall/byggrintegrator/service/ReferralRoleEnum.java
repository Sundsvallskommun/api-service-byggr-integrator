package se.sundsvall.byggrintegrator.service;

import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ReferralRoleEnum {

	NEIGHBOUR("GRAN", 1),
	PROPERTY_OWNER("FAG", 2);

	public static final String UNKNOWN_WEIGHT = "0";

	private final String byggrRole;
	private final int apiNumber;

	public static String translate(String role) {
		return Arrays.stream(ReferralRoleEnum.values())
			.filter(item -> item.getByggrRole().equalsIgnoreCase(role))
			.findFirst()
			.map(ReferralRoleEnum::getApiNumber)
			.map(String::valueOf)
			.orElse(UNKNOWN_WEIGHT);
	}
}
