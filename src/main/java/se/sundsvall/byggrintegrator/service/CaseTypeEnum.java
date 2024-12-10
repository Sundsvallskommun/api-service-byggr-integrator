package se.sundsvall.byggrintegrator.service;

import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CaseTypeEnum {
	BYGGLOV("BL", 11),
	RIVNINGSLOV("RL", 12),
	MARKLOV("MARK", 13),
	FORHANDSBESKED("FÖRF", 14),
	ANMALAN_ATTEFALL("ATTANM", 15),
	ANMALAN("ANM", 16),
	STRANDSKYDD("DI", 21);

	private final String byggrType;
	private final int apiNumber;

	public static String translate(String type) {
		return Arrays.stream(CaseTypeEnum.values())
			.filter(item -> type.equalsIgnoreCase(item.getByggrType()))
			.findFirst()
			.map(CaseTypeEnum::getApiNumber)
			.map(String::valueOf)
			.orElse("999"); // 999 is equvivalent to "övriga ärenden"
	}
}
