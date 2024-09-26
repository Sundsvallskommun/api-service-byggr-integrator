
package se.sundsvall.byggrintegrator.service.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import se.sundsvall.byggrintegrator.Application;

@SpringBootTest(classes = Application.class)

@ActiveProfiles("junit")
class ByggrFilterPropertiesTest {

	@Autowired
	private ByggrFilterProperties properties;

	@Test
	void testProperties() {
		assertThat(properties.applicant().roles()).containsExactly("SOK", "KPER");
		assertThat(properties.notifications().unwantedEventTypes()).containsExactly("UNWANTED1", "UNWANTED2");
	}
}