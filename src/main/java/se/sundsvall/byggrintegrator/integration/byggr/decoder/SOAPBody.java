package se.sundsvall.byggrintegrator.integration.byggr.decoder;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAnyElement;
import lombok.Data;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class SOAPBody {

	@XmlAnyElement(lax = true)
	private Object content;
}
