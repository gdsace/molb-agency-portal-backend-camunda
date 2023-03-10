package sg.gov.tech.molbagencyportalbackend.controller.api

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import sg.gov.tech.molbagencyportalbackend.dto.ApplicationRFARequestParams
import sg.gov.tech.molbagencyportalbackend.exception.InternalConfigException
import sg.gov.tech.molbagencyportalbackend.fixture.SendRFADTOFixture
import sg.gov.tech.molbagencyportalbackend.service.ApplicationRFAService
import sg.gov.tech.molbagencyportalbackend.util.FeatureToggle
import sg.gov.tech.testing.MolbUnitTesting
import sg.gov.tech.testing.messageEqualTo
import sg.gov.tech.testing.verifyNever
import sg.gov.tech.testing.verifyOnce

@MolbUnitTesting
internal class ApplicationRFAControllerTest {

    @MockK
    private lateinit var applicationRFAService: ApplicationRFAService

    @MockK
    private lateinit var featureToggle: FeatureToggle

    @InjectMockKs
    private lateinit var applicationRFAController: ApplicationRFAController

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `should return the list of rfa and count`() {
        every { featureToggle.isRFAEnabled() } returns true
        every {
            applicationRFAService.getApplicationRFA(any(), any())
        } returns mockk()

        applicationRFAController.getRFAList(
            "FC10000X",
            ApplicationRFARequestParams(0, "rfaNo", "asc", 10)
        )

        verifyOnce { applicationRFAService.getApplicationRFA(any(), any()) }
    }

    @Test
    fun `should throw exception if RFA feature is not enabled`() {
        every { featureToggle.isRFAEnabled() } returns false

        assertThrows<InternalConfigException> {
            applicationRFAController.getRFAList(
                "FC10000X",
                ApplicationRFARequestParams(0, "rfaNo", "asc", 10)
            )
        }.messageEqualTo(FeatureToggle.RFA_NOT_ENABLED)
        verifyNever { applicationRFAService.getApplicationRFA(any(), any()) }

        assertThrows<InternalConfigException> {
            applicationRFAController.sendRFA("FC10000X", SendRFADTOFixture.RFADTO)
        }.messageEqualTo(FeatureToggle.RFA_NOT_ENABLED)
        verifyNever { applicationRFAService.sendRFA(any(), any()) }
    }
}
