package se.sundsvall.byggrintegrator.configuration;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import se.sundsvall.byggrintegrator.Application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class FileAccessTokenPropertiesTest {

	@Autowired
	private FileAccessTokenProperties properties;

	@Test
	void testProperties() {
		assertThat(properties.expiration()).isEqualTo(Duration.ofHours(24));
		assertThat(properties.cron()).isEqualTo("-");
	}
}
