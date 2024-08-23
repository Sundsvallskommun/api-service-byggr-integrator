package se.sundsvall.byggrintegrator.integration.byggr;

import org.springframework.cloud.openfeign.FeignBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import se.sundsvall.dept44.configuration.feign.FeignConfiguration;
import se.sundsvall.dept44.configuration.feign.FeignMultiCustomizer;

import feign.jaxb.JAXBContextFactory;
import feign.soap.SOAPDecoder;
import feign.soap.SOAPEncoder;
import feign.soap.SOAPErrorDecoder;
import jakarta.xml.soap.SOAPConstants;

@Import(FeignConfiguration.class)
public class ByggrIntegratorConfiguration {

	public static final String CLIENT_ID = "byggr";

	public static final JAXBContextFactory JAXB_CONTEXT_FACTORY = new JAXBContextFactory.Builder().build();

	@Bean
	public FeignBuilderCustomizer feignBuilderCustomizer(final ByggrProperties byggrProperties) {
		return FeignMultiCustomizer.create()
			.withDecoder(new SOAPDecoder(JAXB_CONTEXT_FACTORY))
			.withErrorDecoder(new SOAPErrorDecoder())
			.withEncoder(new SOAPEncoder.Builder()
				.withFormattedOutput(false)
				.withJAXBContextFactory(JAXB_CONTEXT_FACTORY)
				.withSOAPProtocol(SOAPConstants.SOAP_1_1_PROTOCOL)
				.withWriteXmlDeclaration(true)
				.build())
			.withRequestTimeoutsInSeconds(byggrProperties.connectTimeoutInSeconds(), byggrProperties.readTimeoutInSeconds())
			.composeCustomizersToOne();
	}
}
