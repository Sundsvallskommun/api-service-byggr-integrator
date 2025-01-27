package se.sundsvall.byggrintegrator.integration.byggr.decoder;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAnyElement;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

class SOAPBodyTest {

	@Test
	void testClassAnnotations() {
		var xmlAccessorType = SOAPBody.class.getAnnotation(XmlAccessorType.class);
		assertThat(xmlAccessorType).isNotNull();
		assertThat(xmlAccessorType.value()).isEqualTo(XmlAccessType.FIELD);
	}

	@Test
	void testFieldAnnotation() throws NoSuchFieldException {
		var contentField = SOAPBody.class.getDeclaredField("content");
		var xmlAnyElement = contentField.getAnnotation(XmlAnyElement.class);
		assertThat(xmlAnyElement).isNotNull();
		assertThat(xmlAnyElement.lax()).isTrue();
	}

	@Test
	void testBean() {
		MatcherAssert.assertThat(SOAPBody.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanEquals(),
			hasValidBeanHashCode(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {
		var content = new Object();
		var bean = new SOAPBody();
		bean.setContent(content);

		assertThat(bean).hasNoNullFieldsOrProperties();
		assertThat(bean.getContent()).isEqualTo(content);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(new SOAPBody()).hasAllNullFieldsOrProperties();
	}
}
