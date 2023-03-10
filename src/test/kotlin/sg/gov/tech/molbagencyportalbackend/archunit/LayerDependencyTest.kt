package sg.gov.tech.molbagencyportalbackend.archunit

import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.junit.AnalyzeClasses
import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.library.Architectures

@AnalyzeClasses(
    packages = ["sg.gov.tech.molbagencyportalbackend"],
    importOptions = [ImportOption.DoNotIncludeTests::class]
)
object LayerDependencyTest {
    @ArchTest
    val layerDependency = Architectures.layeredArchitecture()
        .layer("Controller").definedBy("sg.gov.tech.molbagencyportalbackend.controller..")
        .layer("Service").definedBy("sg.gov.tech.molbagencyportalbackend.service..")
        .layer("Persistence").definedBy("sg.gov.tech.molbagencyportalbackend.repository..")
        .layer("Configuration").definedBy("sg.gov.tech.molbagencyportalbackend.configuration..")
        .layer("Validator").definedBy("sg.gov.tech.molbagencyportalbackend.validator..")
        .layer("Auth").definedBy("sg.gov.tech.molbagencyportalbackend.auth..")
        .whereLayer("Controller").mayNotBeAccessedByAnyLayer()
        .whereLayer("Service").mayOnlyBeAccessedByLayers("Controller", "Validator")
        .whereLayer("Persistence").mayOnlyBeAccessedByLayers("Service")
        .whereLayer("Configuration").mayOnlyBeAccessedByLayers("Auth")
}
