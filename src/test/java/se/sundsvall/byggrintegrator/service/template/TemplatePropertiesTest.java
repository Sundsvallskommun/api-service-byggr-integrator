package se.sundsvall.byggrintegrator.service.template;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import se.sundsvall.byggrintegrator.Application;

@SpringBootTest(classes = Application.class)
@ActiveProfiles("junit")
class TemplatePropertiesTest {

	@Autowired
	private TemplateProperties properties;

	@Test
	void testProperties() {
		assertThat(properties.domain()).isEqualTo("http://somewhere.com/");
		assertThat(properties.subDirectory()).isEqualTo("/files/");
	}
}
