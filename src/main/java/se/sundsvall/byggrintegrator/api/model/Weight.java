package se.sundsvall.byggrintegrator.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import tools.jackson.dataformat.xml.annotation.JacksonXmlText;

@Getter
@Setter
@Builder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Weight {

	@JacksonXmlText
	private String value;
}
