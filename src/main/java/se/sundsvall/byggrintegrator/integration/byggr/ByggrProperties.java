package se.sundsvall.byggrintegrator.integration.byggr;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("integration.byggr")
public record ByggrProperties(int connectTimeoutInSeconds, int readTimeoutInSeconds, MapperProperties mapper) {

	public record MapperProperties(ApplicantProperties applicant, NotificationProperties notifications) {
	}

	public record ApplicantProperties(List<String> roles) {
	}

	public record NotificationProperties(List<String> unwantedHandelseslag) {
	}

}
