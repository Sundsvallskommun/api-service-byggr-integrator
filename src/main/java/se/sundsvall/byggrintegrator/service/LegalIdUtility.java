package se.sundsvall.byggrintegrator.service;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.containsNone;

public class LegalIdUtility {
	private static final int LEGAL_ID_MINIMUM_LENGTH = 5;
	private static final int LEGAL_ID_HYPHEN_POSITION_FROM_RIGHT = 4;

	private LegalIdUtility() {}

	/**
	 * Method to add a hyphen after position 4 when string passes the following tests:
	 * - string is not null
	 * - string has a minimum length of 4
	 * - string contains no hyphen
	 * If sent in string doesn't pass the test above, the string is returned untouched.
	 *
	 * @param legalId string
	 * @return string with hyphen added or untouched string if it doesn't pass the tests above
	 */
	public static String addHyphen(String legalId) {
		return ofNullable(legalId)
			.filter(string -> string.length() >= LEGAL_ID_MINIMUM_LENGTH)
			.filter(string -> containsNone(string, "-"))
			.map(string -> new StringBuilder(string).insert(string.length() - LEGAL_ID_HYPHEN_POSITION_FROM_RIGHT, "-").toString())
			.orElse(legalId);
	}

}
