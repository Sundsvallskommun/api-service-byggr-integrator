package se.sundsvall.byggrintegrator.model;

import java.util.List;

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
	private String byggrErrandNumber;
	private List<PropertyDesignation> propertyDesignation;

	@Getter
	@Setter
	@Builder(setterPrefix = "with")
	@NoArgsConstructor
	@AllArgsConstructor
	@ToString
	@EqualsAndHashCode
	public static class PropertyDesignation {
		private String property;    //Name of the property
		private String designation; // Fastighetsbeteckning
	}
}
