package sg.gov.tech.molbagencyportalbackend.annotation

// This annotation allows functions to be excluded from JaCoCo Coverage Test
// Reference: https://github.com/jacoco/jacoco/releases/tag/v0.8.2
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE, AnnotationTarget.FUNCTION)
annotation class ExcludeFromGeneratedCoverageTest
