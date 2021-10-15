package com.github.tomakehurst.wiremock.archunit;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.runner.RunWith;

import static com.tngtech.archunit.base.DescribedPredicate.describe;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.freeze.FreezingArchRule.freeze;

@AnalyzeClasses(packagesOf = WireMockServer.class, importOptions = {
		ImportOption.DoNotIncludeArchives.class,
		ImportOption.DoNotIncludeJars.class
})
class JUnit4DetectionTest {

	private static final DescribedPredicate<? super JavaClass> EXCLUDE_WIREMOCKJUNITRULETEST = describe(
			"exclude WireMockJUnitRuleTest",
			clazz -> !clazz.getName().contains("WireMockJUnitRuleTest"));

	private static final String BECAUSE = "we want to migrate to JUnit Jupiter";

	@ArchTest
	static ArchRule junit4PackageShouldNotBeUsed = freeze(
			noClasses()
					.that(EXCLUDE_WIREMOCKJUNITRULETEST)
					.should().dependOnClassesThat().resideInAnyPackage("org.junit")
					.as("org.junit should not be used")
					.because(BECAUSE));

	@ArchTest
	static ArchRule junit4RunWithShouldNotBeUsed = freeze(
			classes()
					.that(EXCLUDE_WIREMOCKJUNITRULETEST)
					.should().notBeAnnotatedWith(RunWith.class)
					.as("RunWith should not be used")
					.because(BECAUSE));

	@ArchTest
	static ArchRule junit4ClassRuleShouldNotBeUsed = freeze(
			fields()
					.that().areDeclaredInClassesThat(EXCLUDE_WIREMOCKJUNITRULETEST)
					.should().notBeAnnotatedWith(ClassRule.class)
					.as("ClassRule should not be used")
					.because(BECAUSE));

	@ArchTest
	static ArchRule junit4RuleShouldNotBeUsed = freeze(
			fields()
					.that().areDeclaredInClassesThat(EXCLUDE_WIREMOCKJUNITRULETEST)
					.should().notBeAnnotatedWith(Rule.class)
					.as("Rule should not be used")
					.because(BECAUSE));

}
