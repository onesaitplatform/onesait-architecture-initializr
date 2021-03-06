package com.minsait.onesait.initializrv2.archunit;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.Architectures.LayeredArchitecture;

public class LayerTest {

    private JavaClasses javaClasses;

    @BeforeEach
    public void init() {
        this.javaClasses = new ClassFileImporter().importPackages("com.minsait.onesait.initializrv2");
    }

    @Test
    public void givenControllerLayer_thenDependsOnServiceLayer() {
        ArchRule controllerRule = classes()
                                        .that()
                                        .resideInAPackage("..controllers..")
                                        .should().dependOnClassesThat()
                                        .resideInAPackage("..services..");
        
        controllerRule.check(this.javaClasses);
    }

    @Test
    public void givenALayerArchitecture_thenNoLayerViolationShouldExist() {
        LayeredArchitecture architecture = layeredArchitecture()
                                                            .layer("controller").definedBy("..controllers..")
                                                            .layer("service").definedBy("..services.impl")
                                                            .whereLayer("controller").mayNotBeAccessedByAnyLayer()
                                                            .whereLayer("service").mayOnlyBeAccessedByLayers("controller");
        
                                                        
        architecture.check(this.javaClasses);
    }    
    
}