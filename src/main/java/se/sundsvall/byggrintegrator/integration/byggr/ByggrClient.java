package se.sundsvall.byggrintegrator.integration.byggr;

import static se.sundsvall.byggrintegrator.integration.byggr.ByggrConfiguration.CLIENT_ID;

import generated.se.sundsvall.arendeexport.GetArende;
import generated.se.sundsvall.arendeexport.GetArendeResponse;
import generated.se.sundsvall.arendeexport.GetDocument;
import generated.se.sundsvall.arendeexport.GetDocumentResponse;
import generated.se.sundsvall.arendeexport.GetRelateradeArendenByPersOrgNrAndRole;
import generated.se.sundsvall.arendeexport.GetRelateradeArendenByPersOrgNrAndRoleResponse;
import generated.se.sundsvall.arendeexport.GetRoller;
import generated.se.sundsvall.arendeexport.GetRollerResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@CircuitBreaker(name = CLIENT_ID)
@FeignClient(name = CLIENT_ID, url = "${integration.byggr.url}", configuration = ByggrConfiguration.class)
public interface ByggrClient {

	String TEXT_XML_UTF8 = "text/xml;charset=UTF-8";

	@PostMapping(consumes = TEXT_XML_UTF8, produces = TEXT_XML_UTF8, headers = {
		"SOAPAction=www.tekis.se/ServiceContract/V8/IExportArenden/GetRelateradeArendenByPersOrgNrAndRole"
	})
	GetRelateradeArendenByPersOrgNrAndRoleResponse getRelateradeArendenByPersOrgNrAndRole(GetRelateradeArendenByPersOrgNrAndRole request);

	@PostMapping(consumes = TEXT_XML_UTF8, produces = TEXT_XML_UTF8, headers = {
		"SOAPAction=www.tekis.se/ServiceContract/V8/IExportArenden/GetRoller"
	})
	GetRollerResponse getRoller(GetRoller request);

	@PostMapping(consumes = TEXT_XML_UTF8, produces = TEXT_XML_UTF8, headers = {
		"SOAPAction=www.tekis.se/ServiceContract/V8/IExportArenden/GetArende"
	})
	GetArendeResponse getArende(GetArende request);

	@PostMapping(consumes = TEXT_XML_UTF8, produces = TEXT_XML_UTF8, headers = {
		"SOAPAction=www.tekis.se/ServiceContract/V8/IExportArenden/GetDocument"
	})
	GetDocumentResponse getDocument(GetDocument request);
}
