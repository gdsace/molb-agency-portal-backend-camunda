package sg.gov.tech.molbagencyportalbackend.controller.diagnostic

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import sg.gov.tech.testing.MolbUnitTesting

@MolbUnitTesting
internal class DiagnosticControllerTest {
    @Test
    fun `should return diagnostic version when system property is set`() {
        val diagnosticVersion = "1234.abcd5678"
        System.setProperty("diagnostic.version", diagnosticVersion)
        assertThat(DiagnosticController().getVersionInfo().version).isEqualTo(diagnosticVersion)
    }

    @Test
    fun `should return development version when system property is not set`() {
        System.clearProperty("diagnostic.version")
        assertThat(DiagnosticController().getVersionInfo().version).isEqualTo("development")
    }
}
