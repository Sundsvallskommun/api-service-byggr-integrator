package se.sundsvall.byggrintegrator.service.template;

import generated.se.sundsvall.arendeexport.v4.Dokument;
import generated.se.sundsvall.arendeexport.v4.HandelseHandling;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.byggrintegrator.model.FileTemplateDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TemplateMapperTest {

	private static final String VALID_MUNICIPALITY_ID = "2281";

	@Mock
	private FileUrlService mockFileUrlService;

	@InjectMocks
	private TemplateMapper templateMapper;

	@Test
	void testMapByggrFilesToList_duplicateRemovedAndSorts() {
		when(mockFileUrlService.parseFileUrl(VALID_MUNICIPALITY_ID, 1)).thenReturn("Url1");
		when(mockFileUrlService.parseFileUrl(VALID_MUNICIPALITY_ID, 2)).thenReturn("Url2");

		final var handlingTyper = Map.of("PLAN", "Planer", "RIT", "Ritningar");
		final var handling = List.of(
			new HandelseHandling().withTyp("PLAN").withDokument(
				new Dokument().withNamn("FileName1").withDokId("1")),
			new HandelseHandling().withTyp("PLAN").withDokument(
				new Dokument().withNamn("FileName1").withDokId("1")),
			new HandelseHandling().withTyp("RIT").withDokument(
				new Dokument().withNamn("FileName2").withDokId("2")));

		final var result = templateMapper.mapByggrFilesToList(VALID_MUNICIPALITY_ID, handlingTyper, handling);

		assertThat(result).hasSize(2);
		assertThat(result).extracting(FileTemplateDto::getFileName).containsExactly("Ritningar (FileName2)", "Planer (FileName1)");

		verify(mockFileUrlService, times(2)).parseFileUrl(VALID_MUNICIPALITY_ID, 1);
		verify(mockFileUrlService).parseFileUrl(VALID_MUNICIPALITY_ID, 2);
		verifyNoMoreInteractions(mockFileUrlService);
	}

	@Test
	void testMapByggrFilesToList_withNullId_parseDokIdFallback() {
		when(mockFileUrlService.parseFileUrl(VALID_MUNICIPALITY_ID, -1)).thenReturn("Url");

		final var handlingTyper = Map.of("PLAN", "Planer", "RIT", "Ritningar");
		final var handling = List.of(
			new HandelseHandling().withTyp("PLAN").withDokument(
				new Dokument().withNamn("FileName").withDokId(null)));

		final var result = templateMapper.mapByggrFilesToList(VALID_MUNICIPALITY_ID, handlingTyper, handling);

		assertThat(result).hasSize(1);
		assertThat(result.getFirst().getFileName()).isEqualTo("Planer (FileName)");

		verify(mockFileUrlService).parseFileUrl(VALID_MUNICIPALITY_ID, -1);
		verifyNoMoreInteractions(mockFileUrlService);
	}

	@Test
	void testMapByggrFilesToList_withStringId_parseDokIdFallback() {
		when(mockFileUrlService.parseFileUrl(VALID_MUNICIPALITY_ID, -1)).thenReturn("Url");

		final var handlingTyper = Map.of("PLAN", "Planer", "RIT", "Ritningar");
		final var handling = List.of(
			new HandelseHandling().withTyp("PLAN").withDokument(
				new Dokument().withNamn("FileName").withDokId("a")));

		final var result = templateMapper.mapByggrFilesToList(VALID_MUNICIPALITY_ID, handlingTyper, handling);

		assertThat(result).hasSize(1);
		assertThat(result.getFirst().getFileName()).isEqualTo("Planer (FileName)");

		verify(mockFileUrlService).parseFileUrl(VALID_MUNICIPALITY_ID, -1);
		verifyNoMoreInteractions(mockFileUrlService);
	}
}
