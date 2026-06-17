package se.sundsvall.byggrintegrator.api.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KeyValueTest {

	@Test
	void testCreationAndGetters() {
		final var key = "key";
		final var value = "value";

		final var keyValue = new KeyValue(key, value);

		assertThat(keyValue.key()).isEqualTo(key);
		assertThat(keyValue.value()).isEqualTo(value);
	}

	@Test
	void testToString() {
		final var keyValue = new KeyValue("key", "value");
		assertThat(keyValue.toString())
			.contains("key=key")
			.contains("value=value");
	}

	@Test
	void testWithNullValues() {
		final var keyValue = new KeyValue(null, null);
		assertThat(keyValue.key()).isNull();
		assertThat(keyValue.value()).isNull();
		assertThat(keyValue).isEqualTo(new KeyValue(null, null));
	}
}
