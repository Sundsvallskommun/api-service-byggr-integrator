package se.sundsvall.byggrintegrator.integration.byggr;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.openfeign.FeignBuilderCustomizer;

import se.sundsvall.dept44.configuration.feign.FeignMultiCustomizer;

import feign.soap.SOAPDecoder;
import feign.soap.SOAPEncoder;
import feign.soap.SOAPErrorDecoder;

@ExtendWith(MockitoExtension.class)
class ByggrConfigurationTest {

	@Mock
	private ByggrProperties mockProperties;

	@Mock
	private FeignBuilderCustomizer spyFeignBuilderCustomizer;

	@Spy
	private FeignMultiCustomizer spyFeignMultiCustomizer;

	@Test
	void testFeignBuilderCustomizer() {
		var byggrConfiguration = new ByggrConfiguration();
		int connectTimeoutInSeconds = 1;
		int readTimeoutInSeconds = 2;

		when(mockProperties.connectTimeoutInSeconds()).thenReturn(connectTimeoutInSeconds);
		when(mockProperties.readTimeoutInSeconds()).thenReturn(readTimeoutInSeconds);
		when(spyFeignMultiCustomizer.composeCustomizersToOne()).thenReturn(spyFeignBuilderCustomizer);

		try (MockedStatic<FeignMultiCustomizer> mockFeignMultiCustomizer = Mockito.mockStatic(FeignMultiCustomizer.class)) {
			mockFeignMultiCustomizer.when(FeignMultiCustomizer::create).thenReturn(spyFeignMultiCustomizer);

			var customizer = byggrConfiguration.feignBuilderCustomizer(mockProperties);

			var soapEncoderArgumentCaptor = ArgumentCaptor.forClass(SOAPEncoder.class);
			var soapDecoderArgumentCaptor = ArgumentCaptor.forClass(SOAPDecoder.class);
			var soapErrorDecoderArgumentCaptor = ArgumentCaptor.forClass(SOAPErrorDecoder.class);

			verify(spyFeignMultiCustomizer).withEncoder(soapEncoderArgumentCaptor.capture());
			verify(spyFeignMultiCustomizer).withDecoder(soapDecoderArgumentCaptor.capture());
			verify(spyFeignMultiCustomizer).withErrorDecoder(soapErrorDecoderArgumentCaptor.capture());

			verify(mockProperties).connectTimeoutInSeconds();
			verify(mockProperties).readTimeoutInSeconds();

			verify(spyFeignMultiCustomizer).withRequestTimeoutsInSeconds(connectTimeoutInSeconds, readTimeoutInSeconds);
			verify(spyFeignMultiCustomizer).composeCustomizersToOne();

			assertThat(customizer).isSameAs(spyFeignBuilderCustomizer);
			assertThat(soapEncoderArgumentCaptor.getValue()).isNotNull();
			assertThat(soapDecoderArgumentCaptor.getValue()).isNotNull();
			assertThat(soapErrorDecoderArgumentCaptor.getValue()).isNotNull();
		}
	}
}
