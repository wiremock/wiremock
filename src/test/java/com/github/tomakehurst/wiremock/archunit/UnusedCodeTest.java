package com.github.tomakehurst.wiremock.archunit;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.tngtech.archunit.core.domain.JavaAccess;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaMethodCall;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import static com.tngtech.archunit.base.DescribedPredicate.describe;
import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.JavaMember.Predicates.declaredIn;
import static com.tngtech.archunit.core.domain.properties.HasName.Utils.namesOf;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.library.freeze.FreezingArchRule.freeze;

@AnalyzeClasses(packagesOf = WireMockServer.class, importOptions = {
		ImportOption.DoNotIncludeArchives.class,
		ImportOption.DoNotIncludeJars.class,
		ImportOption.DoNotIncludeTests.class
})
class UnusedCodeTest {

	private static ArchCondition<? super JavaClass> beReferencedClass = new ArchCondition<JavaClass>("be referenced") {
		@Override
		public void check(JavaClass javaClass, ConditionEvents events) {
			Set<JavaAccess<?>> accesses = new HashSet<>(javaClass.getAccessesToSelf());
			accesses.removeAll(javaClass.getAccessesFromSelf());
			if (accesses.isEmpty() && javaClass.getDirectDependenciesToSelf().isEmpty()) {
				events.add(new SimpleConditionEvent(javaClass, false, String.format("%s is unreferenced in %s",
						javaClass.getDescription(), javaClass.getSourceCodeLocation())));
			}
		}
	};

	private static ArchRule classesShouldNotBeUnused = classes()
			.that(describe("do not implement interface", clazz -> clazz.getAllRawInterfaces().isEmpty()))
			.and(describe("do not extend class", clazz -> 1 == clazz.getAllRawSuperclasses().size()))
			.should(beReferencedClass);

	@ArchTest
	static ArchRule classesShouldNotBeUnusedFrozen = freeze(classesShouldNotBeUnused
			.as("should use all classes")
			.because("unused classes should be removed"));

	private static ArchCondition<? super JavaMethod> beReferencedMethod = new ArchCondition<JavaMethod>("be referenced") {
		@Override
		public void check(JavaMethod javaMethod, ConditionEvents events) {
			Set<JavaMethodCall> accesses = new HashSet<>(javaMethod.getAccessesToSelf());
			accesses.removeAll(javaMethod.getAccessesFromSelf());
			if (accesses.isEmpty()) {
				events.add(new SimpleConditionEvent(javaMethod, false, String.format("%s is unreferenced in %s",
						javaMethod.getDescription(), javaMethod.getSourceCodeLocation())));
			}
		}
	};

	private static Predicate<JavaMethod> hasMatchingNameAndParameters(JavaMethod input) {
		return m -> m.getName().equals(input.getName())
				&& m.getRawParameterTypes().size() == input.getRawParameterTypes().size()
				&& (m.getDescriptor().equals(input.getDescriptor())
						|| namesOf(m.getRawParameterTypes()).containsAll(namesOf(input.getRawParameterTypes())));
	}

	private static ArchRule methodsShouldNotBeUnused = methods()
			.that(describe("are not declared in super type",
					input -> !input.getOwner().getAllRawSuperclasses().stream()
							.flatMap(c -> c.getMethods().stream()).anyMatch(hasMatchingNameAndParameters(input))))
			.and(describe("are not declared in interface",
					input -> !input.getOwner().getAllRawInterfaces().stream()
							.flatMap(i -> i.getMethods().stream()).anyMatch(hasMatchingNameAndParameters(input))))
			.and().doNotHaveName("main")
			.and().haveNameNotContaining("lambda")
			.and(not(declaredIn(JavaClass.Predicates.ENUMS.or(JavaClass.Predicates.ANNOTATIONS))))
			.and(not(declaredIn(describe("are not declared in Builder",
					input -> input.getName().endsWith("Builder")))))
			.and().areNotDeclaredIn(com.github.tomakehurst.wiremock.client.WireMock.class)
			.and().areNotDeclaredIn(com.github.tomakehurst.wiremock.core.WireMockConfiguration.class)
			.and().areNotDeclaredIn(com.github.tomakehurst.wiremock.junit5.WireMockExtension.class)
			.and(not(describe("are not getters", input -> input.getParameterTypes().isEmpty()
					&& (input.getName().startsWith("get") || input.getName().startsWith("is")))))
			.and(not(describe("are not builders", input -> input.getParameterTypes().size() <= 1
					&& input.getOwner().tryGetField(input.getName()).isPresent())))
			.should(beReferencedMethod);

	/**
	 * Detect methods that are *likely* unused throughout this code base, and the code base of our users.
	 * This test has the potential to flag false positives, in which case
	 */
	@ArchTest
	static ArchRule methodsShouldNotBeUnusedFrozen = freeze(methodsShouldNotBeUnused
			.as("should use all methods")
			.because("unused methods should be removed"));

	private static ArchRule nonPublicMethodsShouldNotBeUnused = methods()
			.that(describe("are not declared in super type",
					input -> !input.getOwner().getAllRawSuperclasses().stream()
							.flatMap(c -> c.getMethods().stream()).anyMatch(hasMatchingNameAndParameters(input))))
			.and(describe("are not declared in interface",
					input -> !input.getOwner().getAllRawInterfaces().stream()
							.flatMap(i -> i.getMethods().stream()).anyMatch(hasMatchingNameAndParameters(input))))
			.and().haveNameNotContaining("lambda")
			.and().areNotPublic()
			.should(beReferencedMethod);

	@ArchTest
	static ArchRule nonPublicMethodsShouldNotBeUnusedFrozen = freeze(nonPublicMethodsShouldNotBeUnused
			.as("should use all non public methods")
			.because("unused methods should be removed"));

}
