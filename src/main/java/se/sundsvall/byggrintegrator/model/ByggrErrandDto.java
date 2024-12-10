package se.sundsvall.byggrintegrator.model;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Middle-class for transporting only necessary data between integration and API.
 */
@Getter
@Setter
@Builder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ByggrErrandDto {
	private String byggrCaseNumber;
	private String description;
	private String propertyDesignation;
	private List<Event> events;
	private List<Stakeholder> stakeholders;

	@Getter
	@Setter
	@Builder(setterPrefix = "with")
	@NoArgsConstructor
	@AllArgsConstructor
	@ToString
	@EqualsAndHashCode
	public static class Event {
		private int id; // Maps to handelseId
		private String eventType; // Maps to handelsetyp
		private String eventSubtype; // Maps to handelseslag
		private LocalDate eventDate; // Maps to startDatum
		private Map<String, String> files; // Map<dokumentId, documentName>
		private List<Stakeholder> stakeholders;
		private String heading;     // Used as heading in the template
	}

	@Getter
	@Setter
	@Builder(setterPrefix = "with")
	@NoArgsConstructor
	@AllArgsConstructor
	@ToString
	@EqualsAndHashCode
	public static class Stakeholder {
		private String legalId; // Maps to personNummer
		private List<String> roles; // Maps to roll
	}
}
