package com.github.tomakehurst.wiremock.archunit;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;

import static org.junit.jupiter.api.Assertions.fail;

@AnalyzeClasses(packagesOf = WireMockServer.class, importOptions = {
		ImportOption.DoNotIncludeArchives.class,
		ImportOption.DoNotIncludeJars.class,
		ImportOption.DoNotIncludeTests.class
})
class ArchitectureTest {

	@ArchTest
	void test() {
		fail("Not yet implemented");
	}

}
