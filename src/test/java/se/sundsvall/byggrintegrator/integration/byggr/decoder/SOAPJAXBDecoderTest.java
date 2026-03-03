package se.sundsvall.byggrintegrator.integration.byggr.decoder;

import feign.Response;
import generated.se.sundsvall.arendeexport.v8.GetRelateradeArendenByPersOrgNrAndRoleResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import org.assertj.core.api.Assertions;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.dept44.problem.ThrowableProblem;
import se.sundsvall.dept44.test.annotation.resource.Load;
import se.sundsvall.dept44.test.extension.ResourceLoaderExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_GATEWAY;

@ExtendWith({
	MockitoExtension.class, ResourceLoaderExtension.class
})
class SOAPJAXBDecoderTest {

	@Mock
	private Response mockResponse;

	@Mock
	private Response.Body mockResponseBody;

	private SOAPJAXBDecoder decoder;

	@BeforeEach
	void setup() {
		decoder = new SOAPJAXBDecoder();
		when(mockResponse.body()).thenReturn(mockResponseBody);
	}

	@Test
	void testDecodeSoapMessage(@Load(as = Load.ResourceType.STRING, value = "soap/junit-soap-byggr-get-related-errands-by-legal-id-response.xml") String xml) throws IOException {
		var inputStream = new ByteArrayInputStream(xml.getBytes());
		when(mockResponseBody.asInputStream()).thenReturn(inputStream);

		var result = decoder.decode(mockResponse, GetRelateradeArendenByPersOrgNrAndRoleResponse.class);

		assertThat(result).isInstanceOf(GetRelateradeArendenByPersOrgNrAndRoleResponse.class);
		assertThat(((GetRelateradeArendenByPersOrgNrAndRoleResponse) result).getGetRelateradeArendenByPersOrgNrAndRoleResult().getArende()).hasSize(3);

		verify(mockResponse).body();
		verify(mockResponseBody).asInputStream();
		verifyNoMoreInteractions(mockResponse, mockResponseBody);
	}

	@Test
	void testDecodeMissingBodyInSoapMessage_shouldThrowProblem(@Load(as = Load.ResourceType.STRING, value = "soap/junit-soap-missing-body.xml") String xml) throws IOException {
		var inputStream = new ByteArrayInputStream(xml.getBytes());
		when(mockResponseBody.asInputStream()).thenReturn(inputStream);

		Assertions.assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> decoder.decode(mockResponse, SOAPEnvelope.class))
			.withMessage("Couldn't fetch information from ByggR: SOAP response body is empty")
			.satisfies(problem -> assertThat(problem.getStatus()).isEqualTo(BAD_GATEWAY));

		verify(mockResponse).body();
		verify(mockResponseBody).asInputStream();
		verifyNoMoreInteractions(mockResponse, mockResponseBody);
	}

	@Test
	void testDecodeFaultySoapMessage_shouldThrowProblem() throws IOException {
		var inputStream = new ByteArrayInputStream("faulty".getBytes());
		when(mockResponseBody.asInputStream()).thenReturn(inputStream);

		Assertions.assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> decoder.decode(mockResponse, GetRelateradeArendenByPersOrgNrAndRoleResponse.class))
			.withMessage("Couldn't fetch information from ByggR")
			.satisfies(problem -> assertThat(problem.getStatus()).isEqualTo(BAD_GATEWAY));

		verify(mockResponse).body();
		verify(mockResponseBody).asInputStream();
		verifyNoMoreInteractions(mockResponse, mockResponseBody);
	}

	@Test
	void testdecodeParameterizedType_shouldThrowProblem(@Load(as = Load.ResourceType.STRING, value = "soap/junit-soap-byggr-get-related-errands-by-legal-id-response.xml") String xml) throws IOException {
		var inputStream = new ByteArrayInputStream(xml.getBytes());
		when(mockResponseBody.asInputStream()).thenReturn(inputStream);

		Type nonRawType = new WildcardType() {
			@NotNull
			@Override
			public Type[] getUpperBounds() {
				return new Type[] {
					Object.class
				};
			}

			@NotNull
			@Override
			public Type[] getLowerBounds() {
				return new Type[] {};
			}

			@Override
			public String toString() {
				return "?";
			}
		};

		Assertions.assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> decoder.decode(mockResponse, nonRawType))
			.withMessage("Couldn't fetch information from ByggR: SOAP only supports decoding raw types. Found ?")
			.satisfies(problem -> assertThat(problem.getStatus()).isEqualTo(BAD_GATEWAY));

		verify(mockResponse).body();
		verify(mockResponseBody).asInputStream();
		verifyNoMoreInteractions(mockResponse, mockResponseBody);
	}
}
