package se.sundsvall.byggrintegrator.configuration;

import io.swagger.v3.oas.models.Operation;
import java.util.Optional;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import se.sundsvall.dept44.util.jacoco.ExcludeFromJacocoGeneratedCoverageReport;

@Configuration
@ExcludeFromJacocoGeneratedCoverageReport
public class OpenApiConfigExtension {

	private static final String FILES_ENDPOINT = "/{municipalityId}/files/{fileId}";

	@Bean
	OpenApiCustomizer addNoAuthEndpoints() {

		return openApi -> Optional.ofNullable(openApi.getPaths())
			.map(path -> path.get(FILES_ENDPOINT))
			.flatMap(openApiPath -> Optional.ofNullable(openApiPath.getGet()))
			.ifPresent(this::extendOperation);
	}

	void extendOperation(Operation operation) {
		operation.addExtension("x-auth-type", "None");
		operation.addExtension("x-throttling-tier", "Unlimited");
		operation.addExtension("x-wso2-mutual-ssl", "Optional");
	}
}
