package se.sundsvall.byggrintegrator.api;

import static java.util.Optional.ofNullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

public class ParameterUtility {
	private static final Pattern CASE_NUMBER_AND_EVENT_ID_PATTERN = Pattern.compile("^(.+)\\s+\\[\\s*(\\d+)\\s*\\].*$");
	private static final String ERROR_NON_INTERPRETABLE_VALUE = "Parameter caseNumberAndEventId with value '%s' is not interpretable. Valid format is '<diarynumber> [<numeric eventId>]', for example 'BYGG 2024-000001 [123456]'";

	private ParameterUtility() {}

	public static String parseDiaryNumber(String origin) {
		try {
			return ofNullable(origin)
				.map(CASE_NUMBER_AND_EVENT_ID_PATTERN::matcher)
				.map(matcher -> fetchGroup(matcher, 1))
				.filter(StringUtils::isNotBlank)
				.map(String::trim)
				.orElseThrow(() -> Problem.valueOf(Status.BAD_REQUEST));
		} catch (final Exception e) {
			throw Problem.valueOf(Status.BAD_REQUEST, ERROR_NON_INTERPRETABLE_VALUE.formatted(origin));
		}
	}

	public static int parseEventId(String origin) {
		try {
			return ofNullable(origin)
				.map(CASE_NUMBER_AND_EVENT_ID_PATTERN::matcher)
				.map(matcher -> fetchGroup(matcher, 2))
				.filter(StringUtils::isNotBlank)
				.map(Integer::parseInt)
				.orElseThrow(() -> Problem.valueOf(Status.BAD_REQUEST));
		} catch (final Exception e) {
			throw Problem.valueOf(Status.BAD_REQUEST, ERROR_NON_INTERPRETABLE_VALUE.formatted(origin));
		}
	}

	private static String fetchGroup(Matcher matcher, int groupNbr) {
		matcher.find();
		return matcher.group(groupNbr);
	}
}
