package controller.api

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import org.junit.jupiter.api.Test
import sg.gov.tech.molbagencyportalbackend.controller.api.LicenceController
import sg.gov.tech.molbagencyportalbackend.dto.AgencyLicenceDetailRequestParams
import sg.gov.tech.molbagencyportalbackend.dto.internal.AgencyLicencesRequestParams
import sg.gov.tech.molbagencyportalbackend.service.LicenceService
import sg.gov.tech.testing.MolbUnitTesting
import sg.gov.tech.testing.verifyOnce

@MolbUnitTesting
internal class LicenceControllerTest {

    @MockK
    private lateinit var licenceService: LicenceService

    @InjectMockKs
    private lateinit var licenceController: LicenceController

    @Test
    fun `should return successful response for licences endpoint`() {
        every {
            licenceController.getDashboardLicences(any())
        } returns mockk()

        licenceService.getDashboardLicences(
            AgencyLicencesRequestParams(
                "allLicence",
                0,
                "licenceNumber",
                "asc",
                10
            )
        )

        verifyOnce { licenceController.getDashboardLicences(any()) }

        fun `should return successful response when retrieveing licence`() {
            val licenceNumber = "licenceNumber"
            every { licenceService.getLicenceDetails(any()) } returns mockk()

            licenceController.getLicence(AgencyLicenceDetailRequestParams(licenceNumber))

            verifyOnce { licenceService.getLicenceDetails(AgencyLicenceDetailRequestParams(licenceNumber)) }
        }
    }
}
