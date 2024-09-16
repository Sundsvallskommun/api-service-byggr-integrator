package se.sundsvall.byggrintegrator.integration.byggr;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import se.sundsvall.byggrintegrator.Application;

@SpringBootTest(classes = Application.class)
@ActiveProfiles("junit")
class ByggrPropertiesTest {

	@Autowired
	private ByggrProperties properties;

	@Test
	void testProperties() {
		assertThat(properties.connectTimeoutInSeconds()).isEqualTo(1);
		assertThat(properties.readTimeoutInSeconds()).isEqualTo(2);
		assertThat(properties.mapper().applicant().roles()).containsExactly("SOK", "KPER");
		assertThat(properties.mapper().notifications().unwantedEventTypes()).containsExactly("UNWANTED1", "UNWANTED2");
	}
}
