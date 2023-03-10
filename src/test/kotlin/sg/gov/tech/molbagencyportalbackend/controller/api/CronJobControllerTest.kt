package sg.gov.tech.molbagencyportalbackend.controller.api

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verifyOrder
import org.junit.jupiter.api.Test
import sg.gov.tech.molbagencyportalbackend.controller.job.CronJobController
import sg.gov.tech.molbagencyportalbackend.service.LicenceService
import sg.gov.tech.testing.MolbUnitTesting

@MolbUnitTesting
internal class CronJobControllerTest {
    @MockK
    private lateinit var licenceService: LicenceService

    @InjectMockKs
    private lateinit var cronJobController: CronJobController

    @Test
    fun `should call update-licence-status job successfully`() {
        every { licenceService.updateActiveLicence() } returns mockk()
        every { licenceService.updateExpiredLicence() } returns mockk()

        cronJobController.updateLicenceStatus()

        verifyOrder {
            licenceService.updateActiveLicence()
            licenceService.updateExpiredLicence()
        }
    }
}
