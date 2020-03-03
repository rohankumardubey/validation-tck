/**
 * Jakarta Bean Validation TCK
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.beanvalidation.tck.tests.integration.cdi.managedobjects;

import static org.hibernate.beanvalidation.tck.util.ConstraintViolationAssert.assertThat;
import static org.hibernate.beanvalidation.tck.util.ConstraintViolationAssert.pathWith;
import static org.testng.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;

import org.hibernate.beanvalidation.tck.beanvalidation.Sections;
import org.hibernate.beanvalidation.tck.tests.AbstractTCKTest;
import org.hibernate.beanvalidation.tck.util.IntegrationTest;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.audit.annotations.SpecAssertion;
import org.jboss.test.audit.annotations.SpecVersion;
import org.testng.annotations.Test;

/**
 * Tests that declaring a value extractor for the same type in validation.xml and in the
 * service loader does not throw an exception.
 *
 * @author Guillaume Smet
 */
@IntegrationTest
@SpecVersion(spec = "beanvalidation", version = "3.0.0")
public class ManagedXmlAndServiceLoaderValueExtractorsTest extends AbstractTCKTest {

	@Inject
	private Validator defaultValidator;

	@Deployment
	public static WebArchive createTestArchive() {
		return webArchiveBuilder()
				.withTestClassPackage( ManagedXmlAndServiceLoaderValueExtractorsTest.class )
				.withValidationXml( "validation-ManagedXmlAndServiceLoaderValueExtractorsTest.xml" )
				.withEmptyBeansXml()
				.withResource(
						"jakarta.validation.valueextraction.ValueExtractor-throwsException",
						"META-INF/services/jakarta.validation.valueextraction.ValueExtractor",
						true
				)
				.build();
	}

	@Test
	@SpecAssertion(section = Sections.INTEGRATION_DEPENDENCYINJECTION_CUSTOMCONFIGURATION, id = "b")
	@SpecAssertion(section = Sections.CONSTRAINTDECLARATIONVALIDATIONPROCESS_VALIDATIONROUTINE_VALUEEXTRACTORRESOLUTION_REGISTERING, id = "e")
	public void testValueExtractorsAreSubjectToDependencyInjection() {
		assertNotNull( defaultValidator );

		Set<ConstraintViolation<Foo>> violations = defaultValidator.validate( Foo.invalid() );

		assertThat( violations ).containsOnlyPaths(
				pathWith()
						.property( "property" )
						.containerElement( Greeter.MESSAGE, true, null, null, Map.class, 0 ),
				pathWith()
						.property( "property" )
						.containerElement( Greeter.MESSAGE, true, null, null, Map.class, 1 )
		);
	}

	private static class Foo {

		private final Map<@NotNull String, @NotNull String> property = new HashMap<>();

		private static Foo invalid() {
			Foo foo = new Foo();
			foo.property.put( null, null );
			return foo;
		}
	}
}
