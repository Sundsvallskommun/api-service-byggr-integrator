package se.sundsvall.byggrintegrator.service.template;

import generated.se.sundsvall.arendeexport.v4.HandelseHandling;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import org.springframework.stereotype.Component;
import se.sundsvall.byggrintegrator.model.FileTemplateDto;

@Component
public class TemplateMapper {

	private final FileUrlService fileUrlService;

	public TemplateMapper(FileUrlService fileUrlService) {
		this.fileUrlService = fileUrlService;
	}

	private static <T> Predicate<T> distinctByKey(final Function<? super T, ?> keyExtractor) {
		final Set<Object> seen = ConcurrentHashMap.newKeySet();
		return t -> seen.add(keyExtractor.apply(t));
	}

	// Create a list that we iterate over with Thymeleaf
	public List<FileTemplateDto> mapByggrFilesToList(final String municipalityId, final Map<String, String> handlingtyper, final List<HandelseHandling> handling) {
		return handling.stream()
			.map(entry -> mapToFileTemplateDto(municipalityId, parseDokId(entry), entry, handlingtyper))
			.filter(distinctByKey(FileTemplateDto::getFileName)) // Remove duplicate file names if such exists
			.sorted((o1, o2) -> o2.getFileUrl().compareTo(o1.getFileUrl()))
			.toList();
	}

	private FileTemplateDto mapToFileTemplateDto(final String municipalityId, final int fileId, final HandelseHandling documentNameAndType, final Map<String, String> handlingtyper) {
		return FileTemplateDto.builder()
			.withFileUrl(fileUrlService.parseFileUrl(municipalityId, fileId))
			.withFileName(parseFileName(documentNameAndType, handlingtyper))
			.build();
	}

	private String parseFileName(final HandelseHandling documentNameAndType, final Map<String, String> handlingtyper) {
		// String content is divided into the following: [documentType] ([documentName])
		return "%s (%s)".formatted(
			handlingtyper.get(documentNameAndType.getTyp()),
			documentNameAndType.getDokument().getNamn());
	}

	private int parseDokId(final HandelseHandling handelseHandling) {
		return Optional.ofNullable(handelseHandling.getDokument().getDokId())
			.filter(Objects::nonNull)
			.filter(id -> id.matches("\\d+"))
			.map(Integer::parseInt)
			.orElse(-1);
	}
}
