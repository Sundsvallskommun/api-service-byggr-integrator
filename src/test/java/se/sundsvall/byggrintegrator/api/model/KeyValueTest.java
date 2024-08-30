package se.sundsvall.byggrintegrator.api.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class KeyValueTest {

	@Test
	void testCreationAndGetters() {
		var key = "key";
		var value = "value";

		var keyValue = new KeyValue(key, value);

		assertEquals(key, keyValue.key());
		assertEquals(value, keyValue.value());
	}
}
