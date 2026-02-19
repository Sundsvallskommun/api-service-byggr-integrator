package se.sundsvall.byggrintegrator.integration.byggr.decoder;

import feign.FeignException;
import feign.Response;
import feign.codec.Decoder;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import org.zalando.problem.Problem;

import static org.zalando.problem.Status.BAD_GATEWAY;

public class SOAPJAXBDecoder implements Decoder {

	private static final String FETCH_INFORMATION_ERROR = "Couldn't fetch information from ByggR";

	@Override
	public Object decode(Response response, Type type) throws IOException, FeignException {

		try (InputStream inputStream = response.body().asInputStream()) {
			var jaxbContext = getJAXBContext(resolveRawType(type));
			var unmarshaller = jaxbContext.createUnmarshaller();
			var envelope = (SOAPEnvelope) unmarshaller.unmarshal(inputStream);

			if (envelope.getBody() != null) {
				Object bodyContent = envelope.getBody().getContent();
				if (bodyContent instanceof JAXBElement) {
					return ((JAXBElement<?>) bodyContent).getValue();
				}
				return bodyContent;
			}

			// If the body is null, we throw a problem
			throw Problem.builder()
				.withDetail("SOAP response body is empty")
				.withStatus(BAD_GATEWAY)
				.withTitle(FETCH_INFORMATION_ERROR)
				.build();
		} catch (JAXBException e) {
			throw Problem.builder()
				.withStatus(BAD_GATEWAY)
				.withTitle(FETCH_INFORMATION_ERROR)
				.withDetail(e.getMessage())
				.build();
		}

	}

	private JAXBContext getJAXBContext(Type type) throws JAXBException {
		return JAXBContext.newInstance(SOAPEnvelope.class, (Class<?>) type);
	}

	Type resolveRawType(Type type) {
		while (type instanceof ParameterizedType ptype) {
			type = ptype.getRawType();
		}

		if (!(type instanceof Class)) {
			throw Problem.builder()
				.withStatus(BAD_GATEWAY)
				.withTitle(FETCH_INFORMATION_ERROR)
				.withDetail(String.format("SOAP only supports decoding raw types. Found %s", type))
				.build();
		}

		return type;
	}
}
