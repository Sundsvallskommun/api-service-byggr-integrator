package se.sundsvall.byggrintegrator.configuration;

import io.swagger.v3.oas.models.Operation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.mockito.Mockito.verify;

@SpringBootTest(classes = OpenApiConfigExtension.class)
@ActiveProfiles("junit")
@ExtendWith(MockitoExtension.class)
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
