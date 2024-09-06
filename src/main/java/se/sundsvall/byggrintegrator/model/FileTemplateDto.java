package se.sundsvall.byggrintegrator.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder(setterPrefix = "with")
public class FileTemplateDto {
	private String fileUrl;
	private String fileName;
}
