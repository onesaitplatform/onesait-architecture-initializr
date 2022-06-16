package com.minsait.onesait.initializrv2.archunit;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;

public class ClassesTest {
    
    private JavaClasses javaClasses;

    @BeforeEach
    public void init() {
        this.javaClasses = new ClassFileImporter().importPackages("com.minsait.onesait.initializrv2");
    }

    @Test
    public void givenClasses_thenAreContainedInCorrectPackages() {
        ArchRule rule1 = classes()
                                .that().haveSimpleNameEndingWith("Controller")
                                .should().resideInAPackage("..controllers");

        ArchRule rule2 = classes()
                                .that().haveSimpleNameEndingWith("Service")
                                .should().resideInAPackage("..services");

        rule1.check(this.javaClasses);
        rule2.check(this.javaClasses);
    }
    
    @Test
    public void givenService_thenMustBeInterfaces() {
        ArchRule rule1 = classes()
                                .that().resideInAPackage("..services")
                                .should().beInterfaces();

        rule1.check(this.javaClasses);
    }  
}