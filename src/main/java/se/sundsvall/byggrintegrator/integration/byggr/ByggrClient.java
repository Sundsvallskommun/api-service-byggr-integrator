package se.sundsvall.byggrintegrator.integration.byggr;

import static se.sundsvall.byggrintegrator.integration.byggr.ByggrIntegratorConfiguration.CLIENT_ID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

import generated.se.sundsvall.arendeexport.GetRelateradeArendenByPersOrgNrAndRole;
import generated.se.sundsvall.arendeexport.GetRelateradeArendenByPersOrgNrAndRoleResponse;
import generated.se.sundsvall.arendeexport.GetRoller;
import generated.se.sundsvall.arendeexport.GetRollerResponse;

@FeignClient(name = CLIENT_ID, url = "${integration.byggr.url}", configuration = ByggrIntegratorConfiguration.class)
public interface ByggrClient {

	String TEXT_XML_UTF8 = "text/xml;charset=UTF-8";

	@PostMapping(consumes = TEXT_XML_UTF8, produces = TEXT_XML_UTF8, headers = { "SOAPAction=www.tekis.se/ServiceContract/V4/IExportArenden/GetRelateradeArendenByPersOrgNrAndRole" })
	GetRelateradeArendenByPersOrgNrAndRoleResponse getRelateradeArendenByPersOrgNrAndRole(GetRelateradeArendenByPersOrgNrAndRole request);

	@PostMapping(consumes = TEXT_XML_UTF8, produces = TEXT_XML_UTF8, headers = { "SOAPAction=www.tekis.se/ServiceContract/V4/IExportArenden/GetRoller" })
	GetRollerResponse getRoller(GetRoller request);
}
