package se.sundsvall.byggrintegrator.service.template;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.LinkedHashMap;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import se.sundsvall.byggrintegrator.Application;
import se.sundsvall.byggrintegrator.model.ByggrErrandDto;

@ActiveProfiles("junit")
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
class TemplateMapperTest {

	@MockBean
	private TemplateProperties mockProperties;

	@Autowired
	private TemplateMapper templateMapper;

	@Test
	void test() {
		when(mockProperties.domain()).thenReturn("http://somewhere.com/");
		when(mockProperties.subDirectory()).thenReturn("/files/");

		final var html = templateMapper.generateFileList(
			"1234",
			ByggrErrandDto.builder()
				.withByggrCaseNumber("BYGG 2001-1234")
				.withFiles(new LinkedHashMap<>() {
					private static final long serialVersionUID = 1L;
					{
						put("file1", "file1.txt");
						put("file2", "file2.txt");
					}
				})
				.build());

		assertThat(html).isEqualTo("<ul><li><a href=\"http://somewhere.com/1234/files/file1\">file1.txt</a></li><li><a href=\"http://somewhere.com/1234/files/file2\">file2.txt</a></li></ul>");
		verify(mockProperties, times(2)).domain();
		verify(mockProperties, times(2)).subDirectory();
		verifyNoMoreInteractions(mockProperties);
	}
}
