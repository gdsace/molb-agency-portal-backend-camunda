package sg.gov.tech.molbagencyportalbackend.controller.api

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import sg.gov.tech.molbagencyportalbackend.dto.internal.AgencyApplicationsRequestParams
import sg.gov.tech.molbagencyportalbackend.dto.internal.DashboardApplicationRequestParams
import sg.gov.tech.molbagencyportalbackend.dto.internal.DocumentRequestParams
import sg.gov.tech.molbagencyportalbackend.dto.internal.RejectMessagesDTO
import sg.gov.tech.molbagencyportalbackend.dto.internal.WithdrawApplicationRequestDTO
import sg.gov.tech.molbagencyportalbackend.exception.InternalConfigException
import sg.gov.tech.molbagencyportalbackend.fixture.ApplicationFixture
import sg.gov.tech.molbagencyportalbackend.fixture.UserModelFixture
import sg.gov.tech.molbagencyportalbackend.service.ApplicationService
import sg.gov.tech.molbagencyportalbackend.util.FeatureToggle
import sg.gov.tech.testing.MolbUnitTesting
import sg.gov.tech.testing.messageEqualTo
import sg.gov.tech.testing.verifyNever
import sg.gov.tech.testing.verifyOnce

@MolbUnitTesting
internal class ApplicationControllerTest {

    @MockK
    private lateinit var applicationService: ApplicationService

    @MockK
    private lateinit var featureToggle: FeatureToggle

    @InjectMockKs
    private lateinit var applicationController: ApplicationController

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `should return successful response`() {
        every { applicationService.getDashboardApplications(any()) } returns mockk()

        applicationController.getDashboardApplications(
            DashboardApplicationRequestParams("openCases", 0, "status", "asc", 10)
        )

        verifyOnce { applicationService.getDashboardApplications(any()) }
    }

    @Test
    fun `should return the document`() {
        every { applicationService.retrieveDocument(any(), any()) } returns mockk()

        applicationController.getDocument("FC10000X", DocumentRequestParams(listOf()))

        verifyOnce { applicationService.retrieveDocument(any(), any()) }
    }

    @Test
    fun `should return successful response for applications endpoint`() {
        every { applicationService.getAgencyApplications(any()) } returns mockk()

        applicationController.getAgencyApplications(
            AgencyApplicationsRequestParams(0, "status", "asc", 10)
        )

        verifyOnce { applicationService.getAgencyApplications(any()) }
    }

    @Test
    fun `should return successful response for claim application endpoint`() {
        every { applicationService.claimApplication(any()) } returns mockk()

        applicationController.claimApplication("FC10000X")

        verifyOnce { applicationService.claimApplication("FC10000X") }
    }

    @Test
    fun `should return successful response for reject application endpoint`() {
        every { applicationService.rejectApplication(any(), any()) } returns mockk()

        applicationController.rejectApplication("FC10000X", RejectMessagesDTO(null, null))

        verifyOnce { applicationService.rejectApplication("FC10000X", RejectMessagesDTO(null, null)) }
    }

    @Test
    fun `should throw exception if Reassign feature is not enabled`() {
        every { featureToggle.isReassignEnabled() } returns false

        assertThrows<InternalConfigException> {
            applicationController.reassignApplication(
                ApplicationFixture.createApplication_SingpassSelf.applicationNumber,
                UserModelFixture.readOnlyUser.id!!
            )
        }.messageEqualTo(FeatureToggle.REASSIGN_NOT_ENABLED)
        verifyNever { applicationService.reassignApplication(any(), any()) }

        assertThrows<InternalConfigException> {
            applicationController.getUsersForReassign(
                ApplicationFixture.createApplication_SingpassSelf.applicationNumber
            )
        }.messageEqualTo(FeatureToggle.REASSIGN_NOT_ENABLED)
        verifyNever { applicationService.getAgencyReassignUsers(any()) }
    }

    @Test
    fun `should return successful response for withdraw application endpoint`() {
        every { featureToggle.isWithdrawalEnabled() } returns true
        every { applicationService.processApplicationWithdrawal(any(), any()) } returns mockk()

        applicationController.withdrawApplication("xxxxx", WithdrawApplicationRequestDTO("Approve", null, null))

        verifyOnce {
            applicationService.processApplicationWithdrawal(
                "xxxxx",
                WithdrawApplicationRequestDTO("Approve", null, null)
            )
        }
    }
}
