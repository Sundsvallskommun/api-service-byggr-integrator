package se.sundsvall.byggrintegrator.api.model;

import java.time.LocalDate;
import java.time.Month;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OVKProtocolTest {

	@Test
	void testCreationAndGetters() {
		final var caseNumber = "2022-000001";
		final var description = "Funktionskontroll av ventilationssystem";
		final var date = LocalDate.of(2022, Month.APRIL, 5);
		final var documentName = "TESTÖN 1123 kontroll";
		final var fileId = "1234567";
		final var downloadUrl = "https://test.se/2281/files/1234567?token=token";

		final var protocol = new OVKProtocol(caseNumber, description, date, documentName, fileId, downloadUrl);

		assertThat(protocol.caseNumber()).isEqualTo(caseNumber);
		assertThat(protocol.description()).isEqualTo(description);
		assertThat(protocol.date()).isEqualTo(date);
		assertThat(protocol.documentName()).isEqualTo(documentName);
		assertThat(protocol.fileId()).isEqualTo(fileId);
		assertThat(protocol.downloadUrl()).isEqualTo(downloadUrl);
	}

	@Test
	void testToString() {
		final var protocol = new OVKProtocol("2022-000001", "Funktionskontroll", null, "protokoll.pdf", "1234567", "url");

		assertThat(protocol.toString())
			.contains("caseNumber=2022-000001")
			.contains("description=Funktionskontroll")
			.contains("documentName=protokoll.pdf")
			.contains("fileId=1234567")
			.contains("downloadUrl=url");
	}

	@Test
	void testWithNullValues() {
		final var protocol = new OVKProtocol(null, null, null, null, null, null);

		assertThat(protocol.caseNumber()).isNull();
		assertThat(protocol.description()).isNull();
		assertThat(protocol.date()).isNull();
		assertThat(protocol.documentName()).isNull();
		assertThat(protocol.fileId()).isNull();
		assertThat(protocol.downloadUrl()).isNull();
		assertThat(protocol).isEqualTo(new OVKProtocol(null, null, null, null, null, null));
	}
}
