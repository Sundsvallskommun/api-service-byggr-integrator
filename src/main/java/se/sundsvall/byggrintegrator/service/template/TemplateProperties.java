package se.sundsvall.byggrintegrator.service.template;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "neighborhood-notification-template.byggr-files")
public record TemplateProperties(String domain, String subDirectory) {
}
