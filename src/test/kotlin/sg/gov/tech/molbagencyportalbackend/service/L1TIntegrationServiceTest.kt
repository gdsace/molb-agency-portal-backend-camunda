package sg.gov.tech.molbagencyportalbackend.service

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import sg.gov.tech.molbagencyportalbackend.dto.l1t.L1TStatusPushRequestTransfer
import sg.gov.tech.molbagencyportalbackend.fixture.ApplicationFixture
import sg.gov.tech.molbagencyportalbackend.fixture.L1TUpdateStatusPayloadFixture
import sg.gov.tech.molbagencyportalbackend.fixture.LicenceModelFixture
import sg.gov.tech.molbagencyportalbackend.integration.l1t.L1TDocumentClient
import sg.gov.tech.molbagencyportalbackend.integration.l1t.L1TStatusPushClient
import sg.gov.tech.testing.MolbUnitTesting

@MolbUnitTesting
internal class L1TIntegrationServiceTest {
    @MockK
    private lateinit var l1TDocumentClient: L1TDocumentClient

    @MockK
    private lateinit var l1tStatusPushClient: L1TStatusPushClient

    @MockK
    private lateinit var l1tStatusPushRequestTransfer: L1TStatusPushRequestTransfer

    @MockK
    private lateinit var licenceService: LicenceService

    @InjectMockKs
    private lateinit var l1tIntegrationService: L1TIntegrationService

    @Test
    fun `Should create L1T Application Status Request`() {
        every { licenceService.getByApplicationId(any()) } returns LicenceModelFixture.licence
        every {
            l1tStatusPushRequestTransfer.createL1TApplication(
                any(),
                any()
            )
        } returns L1TUpdateStatusPayloadFixture.getL1TApplication
        every {
            l1tStatusPushRequestTransfer.createL1TStatusPushRequestDTO(
                any(),
                any(),
                any()
            )
        } returns L1TUpdateStatusPayloadFixture.getUpdateApplicationStatusPayload

        val l1tStatusPushRequest =
            l1tIntegrationService.createL1TApplicationStatusRequest(ApplicationFixture.createApplication_SingpassSelf)

        Assertions.assertEquals(L1TUpdateStatusPayloadFixture.getUpdateApplicationStatusPayload, l1tStatusPushRequest)
    }

    @Test
    fun `Should create L1T Licence Status Request`() {
        every {
            l1tStatusPushRequestTransfer.createL1TLicence(
                any()
            )
        } returns L1TUpdateStatusPayloadFixture.getL1TLicence
        every {
            l1tStatusPushRequestTransfer.createL1TStatusPushRequestDTO(
                any(),
                any(),
                any()
            )
        } returns L1TUpdateStatusPayloadFixture.getUpdateLicenceStatusPayload

        val l1tStatusPushRequest =
            l1tIntegrationService.createL1TLicenceStatusRequest(LicenceModelFixture.licence)

        Assertions.assertEquals(L1TUpdateStatusPayloadFixture.getUpdateLicenceStatusPayload, l1tStatusPushRequest)
    }
}
