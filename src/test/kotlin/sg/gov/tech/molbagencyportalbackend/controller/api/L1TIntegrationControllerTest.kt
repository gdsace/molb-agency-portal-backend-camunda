package sg.gov.tech.molbagencyportalbackend.controller.api

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import org.junit.jupiter.api.Test
import sg.gov.tech.molbagencyportalbackend.controller.job.L1TIntegrationController
import sg.gov.tech.molbagencyportalbackend.fixture.L1TUpdateStatusPayloadFixture
import sg.gov.tech.molbagencyportalbackend.service.L1TIntegrationService
import sg.gov.tech.testing.MolbUnitTesting
import sg.gov.tech.testing.verifyOnce

@MolbUnitTesting
internal class L1TIntegrationControllerTest {
    @MockK
    private lateinit var l1tIntegrationService: L1TIntegrationService

    @InjectMockKs
    private lateinit var l1tIntegrationController: L1TIntegrationController

    @Test
    fun `should update L1T Status successfully`() {
        every { l1tIntegrationService.updateL1TStatus(any()) } returns mockk()

        l1tIntegrationController.updateL1TStatus(L1TUpdateStatusPayloadFixture.getUpdateApplicationStatus)

        verifyOnce { l1tIntegrationService.updateL1TStatus(any()) }
    }
}
