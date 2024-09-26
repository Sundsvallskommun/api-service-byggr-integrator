package se.sundsvall.byggrintegrator.service.util;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("service.byggr.filter-util")
public record ByggrFilterProperties(ApplicantProperties applicant, NotificationProperties notifications) {

	public record ApplicantProperties(List<String> roles) {
	}

	public record NotificationProperties(List<String> unwantedEventTypes) {
	}

}
