package com.minsait.onesait.initializrv2.archunit;

import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_THROW_GENERIC_EXCEPTIONS;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.minsait.onesait.initializrv2")
public class CodingRulesTest {

	@ArchTest
	private final ArchRule no_generic_exceptions = NO_CLASSES_SHOULD_THROW_GENERIC_EXCEPTIONS;

	@ArchTest
	private final ArchRule interfaces_must_not_be_placed_in_implementation_packages = noClasses().that()
			.resideInAPackage("..impl..").should().beInterfaces();

}