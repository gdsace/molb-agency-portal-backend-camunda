package sg.gov.tech.molbagencyportalbackend.controller.resource

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import sg.gov.tech.molbagencyportalbackend.dto.l1t.WithdrawApplicationDTO
import sg.gov.tech.molbagencyportalbackend.exception.InternalConfigException
import sg.gov.tech.molbagencyportalbackend.fixture.ApplicationFixture
import sg.gov.tech.molbagencyportalbackend.fixture.CreateApplicationRequestDTOFixture
import sg.gov.tech.molbagencyportalbackend.service.ApplicationRFAService
import sg.gov.tech.molbagencyportalbackend.service.ApplicationService
import sg.gov.tech.molbagencyportalbackend.util.FeatureToggle
import sg.gov.tech.testing.MolbUnitTesting
import sg.gov.tech.testing.messageEqualTo
import sg.gov.tech.testing.verifyNever
import sg.gov.tech.testing.verifyOnce

@MolbUnitTesting
internal class ApplicationResourceControllerTest {

    @MockK
    private lateinit var applicationService: ApplicationService

    @MockK
    private lateinit var applicationRFAService: ApplicationRFAService

    @MockK
    private lateinit var featureToggle: FeatureToggle

    @InjectMockKs
    private lateinit var applicationResourceController: ApplicationResourceController

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `should return successful response`() {
        every { applicationService.createApplicationDetails(any()) } returns mockk()
        applicationResourceController.createApplication(CreateApplicationRequestDTOFixture.createApplicationRequest)
        verifyOnce { applicationService.createApplicationDetails(any()) }
    }

    @Test
    fun `should return successful response for validation`() {
        every { applicationService.validateApplicationDetails(any()) } returns mockk()
        applicationResourceController.validateApplication(CreateApplicationRequestDTOFixture.createApplicationRequest)
        verifyOnce { applicationService.validateApplicationDetails(any()) }
    }

    @Test
    fun `should return successful response for clarification`() {
        every { featureToggle.isRFAEnabled() } returns true
        every { applicationRFAService.clarifyApplicationRFA(any()) } returns mockk()
        applicationResourceController.clarifyApplication(
            CreateApplicationRequestDTOFixture.createApplicationRequest.copy(
                operation = "clarification"
            )
        )
        verifyOnce { applicationRFAService.clarifyApplicationRFA(any()) }
    }

    @Test
    fun `should throw exception if RFA feature is not enabled`() {
        every { featureToggle.isRFAEnabled() } returns false

        assertThrows<InternalConfigException> {
            applicationResourceController.clarifyApplication(
                CreateApplicationRequestDTOFixture.createApplicationRequest.copy(
                    operation = "clarification"
                )
            )
        }.messageEqualTo(FeatureToggle.RFA_NOT_ENABLED)
        verifyNever { applicationRFAService.clarifyApplicationRFA(any()) }
    }

    @Test
    fun `should throw exception if Withdrawal feature is not enabled`() {
        every { featureToggle.isWithdrawalEnabled() } returns false

        val withdrawApplicationDTO = WithdrawApplicationDTO(
            "withdrawApplication",
            WithdrawApplicationDTO.ApplicationDTO(
                WithdrawApplicationDTO.GeneralDTO(
                    ApplicationFixture.createApplication_SingpassSelf.applicationNumber,
                    "01/06/2022 18:19:25",
                    "Application for Temporary Change of Use Permit",
                    "23"
                )
            )
        )

        assertThrows<InternalConfigException> {
            applicationResourceController.withdrawApplication(withdrawApplicationDTO)
        }.messageEqualTo(FeatureToggle.WITHDRAWAL_NOT_ENABLED)
        verifyNever { applicationService.withdrawApplication(withdrawApplicationDTO) }
    }
}
