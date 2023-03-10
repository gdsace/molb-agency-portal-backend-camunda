package sg.gov.tech.molbagencyportalbackend.archunit

import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.junit.AnalyzeClasses
import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.RestController

@AnalyzeClasses(
    packages = ["sg.gov.tech.molbagencyportalbackend"],
    importOptions = [ImportOption.DoNotIncludeTests::class]
)
object AnnotationDependencyTest {
    @ArchTest
    val restControllerAnnotationDependency = ArchRuleDefinition.classes()
        .that().areAnnotatedWith(RestController::class.java)
        .should().resideInAPackage("sg.gov.tech.molbagencyportalbackend.controller..")

    @ArchTest
    val serviceAnnotationDependency = ArchRuleDefinition.classes()
        .that().areAnnotatedWith(Service::class.java)
        .should().resideInAPackage("sg.gov.tech.molbagencyportalbackend.service..")

    @ArchTest
    val configurationAnnotationDependency = ArchRuleDefinition.classes()
        .that().areAnnotatedWith(Configuration::class.java)
        .should().resideInAPackage("sg.gov.tech.molbagencyportalbackend.configuration..")
}
