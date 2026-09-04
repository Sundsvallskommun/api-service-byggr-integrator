package se.sundsvall.byggrintegrator.api.model;

import java.time.LocalDate;
import java.time.Month;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DecisionDocumentTest {

	@Test
	void testCreationAndGetters() {
		final var id = "470583";
		final var name = "beslut.pdf";
		final var description = "Beslut";
		final var type = "BESLUT";
		final var typeDescription = "Beslut";
		final var documentDate = LocalDate.of(2024, Month.SEPTEMBER, 3);
		final var url = "https://somewhere.com/2281/files/470583?token=token";

		final var document = new DecisionDocument(id, name, description, type, typeDescription, documentDate, url);

		assertThat(document.id()).isEqualTo(id);
		assertThat(document.name()).isEqualTo(name);
		assertThat(document.description()).isEqualTo(description);
		assertThat(document.type()).isEqualTo(type);
		assertThat(document.typeDescription()).isEqualTo(typeDescription);
		assertThat(document.documentDate()).isEqualTo(documentDate);
		assertThat(document.url()).isEqualTo(url);
	}

	@Test
	void testToString() {
		final var document = new DecisionDocument("470583", "beslut.pdf", "Beslut", "BESLUT", "Beslut", null, "url");

		assertThat(document.toString())
			.contains("id=470583")
			.contains("name=beslut.pdf")
			.contains("description=Beslut")
			.contains("type=BESLUT")
			.contains("typeDescription=Beslut")
			.contains("url=url");
	}

	@Test
	void testWithNullValues() {
		final var document = new DecisionDocument(null, null, null, null, null, null, null);

		assertThat(document.id()).isNull();
		assertThat(document.name()).isNull();
		assertThat(document.description()).isNull();
		assertThat(document.type()).isNull();
		assertThat(document.typeDescription()).isNull();
		assertThat(document.documentDate()).isNull();
		assertThat(document.url()).isNull();
		assertThat(document).isEqualTo(new DecisionDocument(null, null, null, null, null, null, null));
	}
}
