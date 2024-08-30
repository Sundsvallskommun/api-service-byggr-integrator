package se.sundsvall.byggrintegrator.api.model;

import io.swagger.v3.oas.annotations.media.Schema;

public record KeyValue(
	@Schema(description = "The key of the key-value pair")
	String key,

	@Schema(description = "The value of the key-value pair")
	String value) {
}
