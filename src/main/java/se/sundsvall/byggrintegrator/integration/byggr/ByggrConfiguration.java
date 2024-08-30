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
public class ByggrConfiguration {

	public static final String CLIENT_ID = "byggr";

	private static final JAXBContextFactory JAXB_CONTEXT_FACTORY = new JAXBContextFactory.Builder().build();
	private static final SOAPEncoder.Builder SOAP_ENCODER_BUILDER = new SOAPEncoder.Builder()
		.withFormattedOutput(false)
		.withJAXBContextFactory(JAXB_CONTEXT_FACTORY)
		.withSOAPProtocol(SOAPConstants.SOAP_1_1_PROTOCOL)
		.withWriteXmlDeclaration(true);


	@Bean
	public FeignBuilderCustomizer feignBuilderCustomizer(final ByggrProperties byggrProperties) {
		return FeignMultiCustomizer.create()
			.withDecoder(new SOAPDecoder(JAXB_CONTEXT_FACTORY))
			.withEncoder(SOAP_ENCODER_BUILDER.build())
			.withErrorDecoder(new SOAPErrorDecoder())
			.withRequestTimeoutsInSeconds(byggrProperties.connectTimeoutInSeconds(), byggrProperties.readTimeoutInSeconds())
			.composeCustomizersToOne();
	}
}
