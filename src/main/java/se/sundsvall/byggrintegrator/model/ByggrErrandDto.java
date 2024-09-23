package se.sundsvall.byggrintegrator.model;

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
	private List<PropertyDesignation> propertyDesignation;
	private Map<String, String> files; // Map<dokumentId, documentName>
	private List<Integer> neighborhoodEventIds; // Used when mapping neighborhood errands

	@Getter
	@Setter
	@Builder(setterPrefix = "with")
	@NoArgsConstructor
	@AllArgsConstructor
	@ToString
	@EqualsAndHashCode
	public static class PropertyDesignation {
		private String property; // Name of the property
		private String designation; // Fastighetsbeteckning
	}
}
