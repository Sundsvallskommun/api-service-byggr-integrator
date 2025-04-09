package se.sundsvall.byggrintegrator.integration.byggr;

import static se.sundsvall.byggrintegrator.integration.byggr.ByggrConfiguration.CLIENT_ID;

import generated.se.sundsvall.arendeexport.v4.GetRemisserByPersOrgNr;
import generated.se.sundsvall.arendeexport.v4.GetRemisserByPersOrgNrResponse;
import generated.se.sundsvall.arendeexport.v8.GetArende;
import generated.se.sundsvall.arendeexport.v8.GetArendeResponse;
import generated.se.sundsvall.arendeexport.v8.GetDocument;
import generated.se.sundsvall.arendeexport.v8.GetDocumentResponse;
import generated.se.sundsvall.arendeexport.v8.GetHandlingTyper;
import generated.se.sundsvall.arendeexport.v8.GetHandlingTyperResponse;
import generated.se.sundsvall.arendeexport.v8.GetRelateradeArendenByPersOrgNrAndRole;
import generated.se.sundsvall.arendeexport.v8.GetRelateradeArendenByPersOrgNrAndRoleResponse;
import generated.se.sundsvall.arendeexport.v8.GetRoller;
import generated.se.sundsvall.arendeexport.v8.GetRollerResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@CircuitBreaker(name = CLIENT_ID)
@FeignClient(name = CLIENT_ID, url = "${integration.byggr.url}", configuration = ByggrConfiguration.class)
public interface ByggrClient {

	String TEXT_XML_UTF8 = "text/xml;charset=UTF-8";
	String VERSION_8 = "SOAPAction=www.tekis.se/ServiceContract/V8/IExportArenden/";
	String VERSION_4 = "SOAPAction=www.tekis.se/ServiceContract/V4/IExportArenden/";

	@PostMapping(consumes = TEXT_XML_UTF8, produces = TEXT_XML_UTF8, headers = {
		VERSION_8 + "GetRelateradeArendenByPersOrgNrAndRole"
	})
	GetRelateradeArendenByPersOrgNrAndRoleResponse getRelateradeArendenByPersOrgNrAndRole(GetRelateradeArendenByPersOrgNrAndRole request);

	@PostMapping(consumes = TEXT_XML_UTF8, produces = TEXT_XML_UTF8, headers = {
		VERSION_8 + "GetRoller"
	})
	GetRollerResponse getRoller(GetRoller request);

	@PostMapping(consumes = TEXT_XML_UTF8, produces = TEXT_XML_UTF8, headers = {
		VERSION_8 + "GetArende"
	})
	GetArendeResponse getArende(GetArende request);

	@PostMapping(consumes = TEXT_XML_UTF8, produces = TEXT_XML_UTF8, headers = {
		VERSION_8 + "GetDocument"
	})
	GetDocumentResponse getDocument(GetDocument request);

	@PostMapping(consumes = TEXT_XML_UTF8, produces = TEXT_XML_UTF8, headers = {
		VERSION_8 + "GetHandlingTyper"
	})
	GetHandlingTyperResponse getHandlingTyper(GetHandlingTyper request);

	@PostMapping(consumes = TEXT_XML_UTF8, headers = {
		VERSION_4 + "GetRemisserByPersOrgNr"
	})
	GetRemisserByPersOrgNrResponse getRemisserByPersOrgNr(GetRemisserByPersOrgNr getRemisserByPersOrgNr);

}
