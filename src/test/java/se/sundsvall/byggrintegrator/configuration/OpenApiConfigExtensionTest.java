package se.sundsvall.byggrintegrator.configuration;

import static org.mockito.Mockito.verify;

import io.swagger.v3.oas.models.Operation;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = OpenApiConfigExtension.class)
@ActiveProfiles("junit")
class OpenApiConfigExtensionTest {

	@Mock
	private Operation operationMock;

	@Autowired
	private OpenApiConfigExtension openApiConfigExtension;

	@Test
	void testExtendOperation() {
		openApiConfigExtension.extendOperation(operationMock);

		verify(operationMock).addExtension("x-auth-type", "None");
		verify(operationMock).addExtension("x-throttling-tier", "Unlimited");
		verify(operationMock).addExtension("x-wso2-mutual-ssl", "Optional");
	}
}
