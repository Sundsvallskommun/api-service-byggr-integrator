package se.sundsvall.byggrintegrator.service.template;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import generated.se.sundsvall.arendeexport.v4.Dokument;
import generated.se.sundsvall.arendeexport.v4.HandelseHandling;
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
import se.sundsvall.byggrintegrator.model.ByggrErrandDto.Stakeholder;

@ActiveProfiles("junit")
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TemplateMapperTest {

	@MockitoBean
	private TemplateProperties mockProperties;

	@Autowired
	private TemplateMapper templateMapper;

	private static ByggrErrandDto createByggrErrandDto(final String identifier) {
		return ByggrErrandDto.builder()
			.withByggrCaseNumber("BYGG 2001-1234")
			.withDescription("Bygglov för tillbyggnad av fritidshus")
			.withPropertyDesignation("RUNSVIK 1:22")
			.withEvents(List.of(
				Event.builder() // Event with matching stakeholder, having 2 files
					.withId(1)
					.withEventType("GRANHO")
					.withEventSubtype("GRAUTS")
					.withStakeholders(List.of(
						Stakeholder.builder()
							.withLegalId(identifier)
							.build()))
					.withFiles(Map.of(
						"file1", new Event.DocumentNameAndType("file1.txt", "SKR"),
						"file2", new Event.DocumentNameAndType("file2.txt", "PLAN")))
					.build(),
				Event.builder() // Event with matching stakeholder, having 2 files of which one is a duplicate (file1)
					.withId(2)
					.withEventType("GRANHO")
					.withEventSubtype("GRAUTS")
					.withStakeholders(List.of(
						Stakeholder.builder()
							.withLegalId(identifier)
							.build()))
					.withFiles(Map.of(
						"file1", new Event.DocumentNameAndType("file1.txt", "SKR"),
						"file3", new Event.DocumentNameAndType("file3.pdf", "RIT")))
					.build(),
				Event.builder()// Event with non-matching stakeholder, having 1 file that should not be in the result
					.withId(3)
					.withEventType("GRANHO")
					.withEventSubtype("GRAUTS")
					.withStakeholders(List.of(
						Stakeholder.builder()
							.withLegalId("no-match")
							.build()))
					.withFiles(Map.of(
						"file4", new Event.DocumentNameAndType("file4.txt", "SKR")))
					.build()))
			.build();
	}

	@Test
	void testGenerateHtmlFromTemplate() {
		final var identifier = "190102034567";

		when(mockProperties.domain()).thenReturn("https://somewhere.com/");
		when(mockProperties.version()).thenReturn("1.0");
		when(mockProperties.subDirectory()).thenReturn("/files/");

		final var html = templateMapper.generateFileList(
			"1234",
			createByggrErrandDto(identifier),
			Map.of("SKR", "Skrivelse", "PLAN", "Planer", "RIT", "Ritningar"),
			List.of(new HandelseHandling().withHandlingId(1).withTyp("SKR").withDokument(new Dokument().withNamn("file1.txt")),
				new HandelseHandling().withHandlingId(2).withTyp("PLAN").withDokument(new Dokument().withNamn("file2.txt")),
				new HandelseHandling().withHandlingId(4).withTyp("PLAN").withDokument(new Dokument().withNamn("file2.txt")),
				new HandelseHandling().withHandlingId(3).withTyp("RIT").withDokument(new Dokument().withNamn("file3.pdf"))));

		assertThat(html).containsIgnoringWhitespaces("""
			<p>Bygglov för tillbyggnad av fritidshus (RUNSVIK 1:22)</p>
			<ul>
				<li><a href="https://somewhere.com/1.0/1234/files/3">Ritningar (file3.pdf)</a></li>
				<li><a href="https://somewhere.com/1.0/1234/files/2">Planer (file2.txt)</a></li>
				<li><a href="https://somewhere.com/1.0/1234/files/1">Skrivelse (file1.txt)</a></li>
			</ul>""");
		verify(mockProperties, times(4)).domain();
		verify(mockProperties, times(4)).version();
		verify(mockProperties, times(4)).subDirectory();
		verifyNoMoreInteractions(mockProperties);
	}

	@Test
	void getDescriptionAndPropertyDesignationInHtml() {
		final var html = templateMapper.getDescriptionAndPropertyDesignation(createByggrErrandDto("190102034567"));

		assertThat(html).isEqualTo("<p>Bygglov för tillbyggnad av fritidshus (RUNSVIK 1:22)</p>");
		verifyNoInteractions(mockProperties);
	}
}
