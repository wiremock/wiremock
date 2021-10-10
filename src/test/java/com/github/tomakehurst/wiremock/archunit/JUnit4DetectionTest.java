package com.github.tomakehurst.wiremock.archunit;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.runner.RunWith;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.freeze.FreezingArchRule.freeze;

@AnalyzeClasses(packagesOf = WireMockServer.class, importOptions = {
		ImportOption.DoNotIncludeArchives.class,
		ImportOption.DoNotIncludeJars.class
})
class JUnit4DetectionTest {

	private static final String BECAUSE = "we want to migrate to JUnit Jupiter";

	@ArchTest
	static ArchRule junit4PackageShouldNotBeUsed = freeze(
			noClasses().should().dependOnClassesThat().resideInAnyPackage("org.junit").because(BECAUSE));

	@ArchTest
	static ArchRule junit4RunWithShouldNotBeUsed = freeze(
			classes().should().notBeAnnotatedWith(RunWith.class).because(BECAUSE));

	@ArchTest
	static ArchRule junit4ClassRuleShouldNotBeUsed = freeze(
			fields().should().notBeAnnotatedWith(ClassRule.class).because(BECAUSE));
	@ArchTest
	static ArchRule junit4RuleShouldNotBeUsed = freeze(
			fields().should().notBeAnnotatedWith(Rule.class).because(BECAUSE));

}
