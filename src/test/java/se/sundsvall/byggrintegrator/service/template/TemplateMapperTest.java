package se.sundsvall.byggrintegrator.service.template;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import se.sundsvall.byggrintegrator.Application;
import se.sundsvall.byggrintegrator.model.ByggrErrandDto;
import se.sundsvall.byggrintegrator.model.ByggrErrandDto.Event;

@ActiveProfiles("junit")
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TemplateMapperTest {

	@MockitoBean
	private TemplateProperties mockProperties;

	@Autowired
	private TemplateMapper templateMapper;

	@Test
	void testGenerateHtmlFromTemplate() {
		when(mockProperties.domain()).thenReturn("https://somewhere.com/");
		when(mockProperties.subDirectory()).thenReturn("/files/");

		var html = templateMapper.generateFileList(
			"1234",
			ByggrErrandDto.builder()
				.withByggrCaseNumber("BYGG 2001-1234")
				.withDescription("Bygglov för tillbyggnad av fritidshus")
				.withPropertyDesignation("RUNSVIK 1:22")
				.withEvents(List.of(Event.builder()
					.withHeading("Heading")
					.withId(1)
					.withEventType("GRANHO")
					.withEventSubtype("GRAUTS")
					.withFiles(Map.of(
						"file1", new Event.DocumentNameAndType("file1.txt", "SKR"),
						"file2", new Event.DocumentNameAndType("file2.txt", "PLAN")))
					.build()))
				.build(),
			Map.of("SKR", "Skrivelse", "PLAN", "Planer"),
			1);

		assertThat(html).isEqualTo(
			"<p>Bygglov för tillbyggnad av fritidshus (RUNSVIK 1:22)</p><p>Heading</p><ul><li><a href=\"https://somewhere.com/1234/files/file2\">Planer (file2.txt)</a></li><li><a href=\"https://somewhere.com/1234/files/file1\">Skrivelse (file1.txt)</a></li></ul>");
		verify(mockProperties, times(2)).domain();
		verify(mockProperties, times(2)).subDirectory();
		verifyNoMoreInteractions(mockProperties);
	}

	@Test
	void getDescriptionAndPropertyDesignationInHtml() {
		var html = templateMapper.getDescriptionAndPropertyDesignation(
			ByggrErrandDto.builder()
				.withByggrCaseNumber("BYGG 2001-1234")
				.withDescription("Bygglov för tillbyggnad av fritidshus")
				.withPropertyDesignation("RUNSVIK 1:22")
				.withEvents(List.of(Event.builder()
					.withHeading("Heading")
					.withId(1)
					.withEventType("GRANHO")
					.withEventSubtype("GRAUTS")
					.withFiles(Map.of(
						"file1", new Event.DocumentNameAndType("file1.txt", "SKR"),
						"file2", new Event.DocumentNameAndType("file2.txt", "PLAN")))
					.build()))
				.build());

		assertThat(html).isEqualTo("<p>Bygglov för tillbyggnad av fritidshus (RUNSVIK 1:22)</p>");
		verifyNoInteractions(mockProperties);
	}
}
