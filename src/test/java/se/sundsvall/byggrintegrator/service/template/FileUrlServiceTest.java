package se.sundsvall.byggrintegrator.service.template;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.byggrintegrator.service.FileAccessTokenService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileUrlServiceTest {

	private static final String VALID_MUNICIPALITY_ID = "2281";
	private static final String VALID_TOKEN = "valid-token";
	private static final String DOMAIN = "http://somewhere.com/";
	private static final String VERSION = "2.4";
	private static final String DIRECTORY = "/files/";
	private static final String FORMATTED_URL = "http://somewhere.com/2.4/2281/files/123?token=valid-token";
	private static final int FILE_ID = 123;

	@Mock
	private TemplateProperties mockProperties;

	@Mock
	private FileAccessTokenService mockTokenService;

	@InjectMocks
	private FileUrlService fileUrlService;

	@Test
	void testParseFileURL() {

		when(mockTokenService.createToken(VALID_MUNICIPALITY_ID, String.valueOf(FILE_ID))).thenReturn(VALID_TOKEN);
		when(mockProperties.domain()).thenReturn(DOMAIN);
		when(mockProperties.version()).thenReturn(VERSION);
		when(mockProperties.subDirectory()).thenReturn(DIRECTORY);

		var result = fileUrlService.parseFileUrl(VALID_MUNICIPALITY_ID, FILE_ID);

		assertThat(result).isEqualTo(FORMATTED_URL);
		verify(mockTokenService).createToken(VALID_MUNICIPALITY_ID, String.valueOf(FILE_ID));
		verify(mockProperties).domain();
		verify(mockProperties).version();
		verify(mockProperties).subDirectory();
		verifyNoMoreInteractions(mockTokenService, mockProperties);

	}

}
