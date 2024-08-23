package se.sundsvall.byggrintegrator.integration.byggr;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("integration.byggr")
public record ByggrProperties(int connectTimeoutInSeconds, int readTimeoutInSeconds) {
}
