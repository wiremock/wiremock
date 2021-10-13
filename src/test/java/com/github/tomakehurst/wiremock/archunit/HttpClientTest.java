package com.github.tomakehurst.wiremock.archunit;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.HttpAdminClient;
import com.github.tomakehurst.wiremock.common.HttpClientUtils;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packagesOf = WireMockServer.class, importOptions = {
		ImportOption.DoNotIncludeArchives.class,
		ImportOption.DoNotIncludeJars.class,
		ImportOption.DoNotIncludeTests.class
})
class HttpClientTest {

	@ArchTest
	static ArchRule httpClientShouldNotLeak = noClasses()
			.that().resideOutsideOfPackage("..http..")
			.and().areNotAssignableTo(HttpAdminClient.class)
			.and().areNotAssignableTo(HttpClientUtils.class)
			.should().dependOnClassesThat().resideInAPackage("org.apache.hc..")
			.orShould().dependOnClassesThat().resideInAPackage("org.apache.http..")
			.as("Apache HttpClient should be limited to http package")
			.because("we want to make the third party dependency optional");

}
