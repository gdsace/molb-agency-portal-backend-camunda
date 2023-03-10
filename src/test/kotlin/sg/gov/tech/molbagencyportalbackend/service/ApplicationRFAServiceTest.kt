package sg.gov.tech.molbagencyportalbackend.service

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verifyOrder
import org.hibernate.envers.AuditReader
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.domain.Page
import sg.gov.tech.molbagencyportalbackend.auth.AuthenticationFacade
import sg.gov.tech.molbagencyportalbackend.dto.ApplicationRFARequestParams
import sg.gov.tech.molbagencyportalbackend.dto.CancelRFARequestDTO
import sg.gov.tech.molbagencyportalbackend.dto.internal.user.ReviewerModelTransfer
import sg.gov.tech.molbagencyportalbackend.dto.l1t.L1TResponseDTOTransfer
import sg.gov.tech.molbagencyportalbackend.exception.InternalConfigException
import sg.gov.tech.molbagencyportalbackend.exception.NotAuthorisedException
import sg.gov.tech.molbagencyportalbackend.exception.ValidationException
import sg.gov.tech.molbagencyportalbackend.fixture.ActivityTypeFixture
import sg.gov.tech.molbagencyportalbackend.fixture.AgencyFixture
import sg.gov.tech.molbagencyportalbackend.fixture.ApplicationFixture
import sg.gov.tech.molbagencyportalbackend.fixture.ApplicationRFAListDTOFixture
import sg.gov.tech.molbagencyportalbackend.fixture.CreateApplicationRequestDTOFixture
import sg.gov.tech.molbagencyportalbackend.fixture.RFAFixture
import sg.gov.tech.molbagencyportalbackend.fixture.ResourceResponseDTOFixture
import sg.gov.tech.molbagencyportalbackend.fixture.ReviewerDTOFixture
import sg.gov.tech.molbagencyportalbackend.fixture.SendRFADTOFixture
import sg.gov.tech.molbagencyportalbackend.fixture.UserFixture
import sg.gov.tech.molbagencyportalbackend.fixture.UserModelFixture
import sg.gov.tech.molbagencyportalbackend.model.ActivityValue
import sg.gov.tech.molbagencyportalbackend.model.ApplicationStatus
import sg.gov.tech.molbagencyportalbackend.model.RFA
import sg.gov.tech.molbagencyportalbackend.model.RFAStatus
import sg.gov.tech.molbagencyportalbackend.repository.ApplicationRepository
import sg.gov.tech.molbagencyportalbackend.repository.RFARepository
import sg.gov.tech.testing.MolbUnitTesting
import sg.gov.tech.testing.messageEqualTo

@MolbUnitTesting
internal class ApplicationRFAServiceTest {

    @MockK
    private lateinit var applicationService: ApplicationService

    @MockK
    private lateinit var applicationRepository: ApplicationRepository

    @MockK
    private lateinit var l1tResponseDTOTransfer: L1TResponseDTOTransfer

    @MockK
    private lateinit var userService: UserService

    @MockK
    private lateinit var authenticationFacade: AuthenticationFacade

    @MockK
    private lateinit var rfaRepository: RFARepository

    @MockK
    private lateinit var auditReader: AuditReader

    @MockK
    private lateinit var reviewerModelTransfer: ReviewerModelTransfer

    @MockK
    private lateinit var activityTypeService: ActivityTypeService

    @InjectMockKs
    private lateinit var applicationRFAService: ApplicationRFAService

    @Nested
    inner class GetApplicationRFA {
        @Test
        fun `should throw NotAuthorisedException if application is not valid`() {
            val principalEmail = "supervisor@tech.gov.sg"
            val sampleApplicationNumber = "FN0000001"
            val requestParams = ApplicationRFARequestParams(
                0,
                "rfaNo",
                "asc",
                10
            )

            every { authenticationFacade.getPrincipalName() } returns principalEmail
            every { userService.getUserByEmailAndIsDeletedFalse(any()) } returns UserModelFixture.userC
            every {
                applicationService.getApplication(any())
            } returns ApplicationFixture.createApplication_SingpassSelf

            assertThrows<NotAuthorisedException> {
                applicationRFAService.getApplicationRFA(sampleApplicationNumber, requestParams)
            }.messageEqualTo(
                "User $principalEmail is not authorised to access other agency application: $sampleApplicationNumber"
            )
        }

        @Test
        fun `should retrieve list of rfa - ASC`() {
            val applicationRfa = mockk<Page<RFA>> {
                every { content } returns mutableListOf(RFAFixture.oldRFA, RFAFixture.newRFA)
            }
            val principalEmail = "supervisor@tech.gov.sg"
            val sampleApplicationNumber = "FN0000001"
            val requestParams = ApplicationRFARequestParams(0, "rfaNo", "asc", 10)

            every { authenticationFacade.getPrincipalName() } returns principalEmail
            every { userService.getUserByEmailAndIsDeletedFalse(any()) } returns UserModelFixture.userA
            every {
                applicationService.getApplication(any())
            } returns ApplicationFixture.createApplication_SingpassSelf
            every { rfaRepository.countAllByApplicationId(any()) } returns 2
            every { rfaRepository.findAllByApplicationId(any(), any()) } returns applicationRfa
            every { auditReader.getRevisions(any(), any())[2] } returns 2
            every { auditReader.getRevisions(any(), any())[4] } returns 4
            every {
                auditReader.createQuery().forEntitiesAtRevision(any(), 2).add(any()).singleResult
            } returns ApplicationFixture.applicationWithOldRFA
            every {
                auditReader.createQuery().forEntitiesAtRevision(any(), 4).add(any()).singleResult
            } returns ApplicationFixture.applicationWithNewRFA
            every { userService.getUserById(any()) } returns UserModelFixture.userA
            every { reviewerModelTransfer.toDTO(any()) } returns ReviewerDTOFixture.reviewerDTOA

            val finalResult =
                applicationRFAService.getApplicationRFA(sampleApplicationNumber, requestParams)
            Assertions.assertEquals(2, finalResult.totalCount)
            Assertions.assertEquals(ApplicationRFAListDTOFixture.ascList, finalResult.applicationRFA)
        }

        @Test
        fun `should retrieve list of rfa - DESC`() {
            val applicationRfa = mockk<Page<RFA>> {
                every { content } returns mutableListOf(RFAFixture.newRFA, RFAFixture.oldRFA)
            }
            val principalEmail = "supervisor@tech.gov.sg"
            val sampleApplicationNumber = "FN0000001"
            val requestParams = ApplicationRFARequestParams(2, "rfaNo", "desc", 10)

            every { authenticationFacade.getPrincipalName() } returns principalEmail
            every { userService.getUserByEmailAndIsDeletedFalse(any()) } returns UserModelFixture.userA
            every {
                applicationService.getApplication(any())
            } returns ApplicationFixture.createApplication_SingpassSelf
            every { rfaRepository.countAllByApplicationId(any()) } returns 22
            every { rfaRepository.findAllByApplicationId(any(), any()) } returns applicationRfa
            every { auditReader.getRevisions(any(), any())[2] } returns 2
            every { auditReader.getRevisions(any(), any())[4] } returns 4
            every {
                auditReader.createQuery().forEntitiesAtRevision(any(), 2).add(any()).singleResult
            } returns ApplicationFixture.applicationWithOldRFA
            every {
                auditReader.createQuery().forEntitiesAtRevision(any(), 4).add(any()).singleResult
            } returns ApplicationFixture.applicationWithNewRFA
            every { userService.getUserById(any()) } returns UserModelFixture.userA
            every { reviewerModelTransfer.toDTO(any()) } returns ReviewerDTOFixture.reviewerDTOA

            val finalResult =
                applicationRFAService.getApplicationRFA(sampleApplicationNumber, requestParams)
            Assertions.assertEquals(22, finalResult.totalCount)
            Assertions.assertEquals(ApplicationRFAListDTOFixture.descList, finalResult.applicationRFA)
        }
    }

    @Nested
    inner class SendRFA {
        @Test
        fun `should throw not authorized exception if user belongs to different agency`() {
            every { authenticationFacade.getPrincipalName() } returns "supervisor@tech.gov.sg"
            every { userService.getUserByEmailAndIsDeletedFalse(any()) } returns UserModelFixture.userC
            every { applicationService.getApplication(any()) } returns ApplicationFixture.createApplication_SingpassSelf

            assertThrows<NotAuthorisedException> {
                applicationRFAService.sendRFA("testApplication123", SendRFADTOFixture.RFADTO)
            }.messageEqualTo(
                "User supervisor@tech.gov.sg is not authorised to access " +
                    "other agency application: testApplication123"
            )
        }

        @Test
        fun `should throw Validation exception if application cannot be sent for RFA`() {
            every { authenticationFacade.getPrincipalName() } returns "supervisor@tech.gov.sg"
            every { userService.getUserByEmailAndIsDeletedFalse(any()) } returns UserModelFixture.userA
            every {
                applicationService.getApplication(any())
            } returns ApplicationFixture.createApplication_SingpassSelf.copy(
                status = ApplicationStatus.APPROVED
            )
            every { rfaRepository.existsByApplicationIdAndStatus(any(), any()) } returns false

            assertThrows<ValidationException> {
                applicationRFAService.sendRFA("testApplication123", SendRFADTOFixture.RFADTO)
            }.messageEqualTo("Application testApplication123 cannot be RFAed")
        }

        @Test
        fun `should throw ValidationException if RFA already exists in PAA`() {
            every {
                authenticationFacade.getPrincipalName()
            } returns "supervisor@tech.gov.sg"
            every { userService.getUserByEmailAndIsDeletedFalse(any()) } returns UserModelFixture.userA
            every {
                applicationService.getApplication(any())
            } returns ApplicationFixture.createApplication_SingpassSelf.copy(
                status = ApplicationStatus.PROCESSING,
                reviewer = UserModelFixture.userA
            )
            every { rfaRepository.existsByApplicationIdAndStatus(any(), any()) } returns true

            assertThrows<ValidationException> {
                applicationRFAService.sendRFA("testApplication123", SendRFADTOFixture.RFADTO)
            }.messageEqualTo("RFA for Application testApplication123 is already Pending Applicant Action")
        }

        @Test
        fun `should save the rfa and update status if valid request`() {
            every { authenticationFacade.getPrincipalName() } returns "supervisor@tech.gov.sg"
            every { userService.getUserByEmailAndIsDeletedFalse(any()) } returns UserModelFixture.userA
            every {
                applicationService.getApplication(any())
            } returns ApplicationFixture.createApplication_SingpassSelf.copy(
                status = ApplicationStatus.PROCESSING,
                reviewer = UserFixture.userA
            )
            every { rfaRepository.existsByApplicationIdAndStatus(any(), any()) } returns false
            every { auditReader.getRevisions(any(), any()).lastIndex } returns 1
            every { rfaRepository.save(any()) } returns RFAFixture.createRFA
            every { applicationService.updatePendingApplicantActionStatus(any(), any(), any()) } returns
                ApplicationFixture.createApplication_SingpassSelf.copy(
                    status = ApplicationStatus.PENDING_APPLICANT_ACTION,
                    reviewer = null
                )
            every { applicationRepository.save(any()) } returns ApplicationFixture.createApplication_SingpassSelf.copy(
                status = ApplicationStatus.PENDING_APPLICANT_ACTION,
                reviewer = null
            )
            every { applicationService.sendL1TUpdateStatusRequest(any(), any()) } returns mockk()

            every { activityTypeService.getActivityType(any()) } returns ActivityTypeFixture.activityType.copy(
                id = 3,
                type = ActivityValue.SEND_RFA
            )

            applicationRFAService.sendRFA(
                ApplicationFixture.createApplication_SingpassSelf.applicationNumber,
                SendRFADTOFixture.RFADTO
            )

            verifyOrder {
                rfaRepository.existsByApplicationIdAndStatus(any(), any())
                rfaRepository.save(any())
            }
        }
    }

    @Nested
    inner class ClarifyApplication {
        @Test
        fun `should update RFA record for clarify operation`() {
            every { rfaRepository.getRFAByApplicationIdAndStatus(any(), any()) } returns listOf(
                RFAFixture.createRFA.copy(
                    status = RFAStatus.PENDING_APPLICANT_ACTION
                )
            )
            every { auditReader.getRevisions(any(), any()).lastIndex } returns 3
            every { rfaRepository.save(any()) } returns RFAFixture.createRFA
            every {
                l1tResponseDTOTransfer.createSuccessResponseDTO(any())
            } returns ResourceResponseDTOFixture.responseSuccess

            val rfa = applicationRFAService.updateRFASubmitted(
                ApplicationFixture.createApplication_SingpassSelf.id!!,
                CreateApplicationRequestDTOFixture.createApplicationRequest
            )

            Assertions.assertEquals(RFAStatus.RFA_RESPONDED, rfa.status)
            Assertions.assertNotNull(rfa.responseDate)
            Assertions.assertEquals("test", rfa.applicantRemarks)
        }
    }

    @Nested
    inner class CancelRFA {
        @BeforeEach
        fun setup() {
            val authUser = "supervisor@tech.gov.sg"
            val user = UserModelFixture.userA.copy()

            val application = ApplicationFixture.createApplication_SingpassSelf.copy(
                reviewer = user,
                status = ApplicationStatus.PENDING_APPLICANT_ACTION
            )

            every { applicationService.getApplication(any()) } returns application
            every { authenticationFacade.getPrincipalName() } returns authUser
            every { userService.getUserByEmailAndIsDeletedFalse(authUser) } returns user

            every { rfaRepository.getFirstByApplicationIdOrderByIdDesc(any()) } returns RFAFixture.baseRFA

            every { auditReader.getRevisions(any(), any()).lastIndex } returns 6
            every { rfaRepository.save(any()) } returns RFAFixture.baseRFA

            every {
                applicationService.updateApplicationCancelRFA(any(), any(), any())
            } returns application.copy(status = ApplicationStatus.PROCESSING)
            every { applicationRepository.save(any()) } returns application.copy(status = ApplicationStatus.PROCESSING)
            every { applicationService.sendL1TUpdateStatusRequest(any()) } returns mockk()
        }

        @AfterEach
        fun tearDown() {
            clearAllMocks()
        }

        @Test
        fun `Should throw NotAuthorisedException if application does not belongs to the same agency as reviewer`() {
            val applicationNumber = "cancelApp1234"
            every { applicationService.getApplication(any()) } returns
                ApplicationFixture.createApplication_SingpassSelf.copy(agency = AgencyFixture.agency.copy(id = 2))

            assertThrows<NotAuthorisedException> {
                applicationRFAService.cancelRFA(applicationNumber, CancelRFARequestDTO(null, null))
            }.messageEqualTo(
                "User supervisor@tech.gov.sg is not authorised to access other agency application: $applicationNumber"
            )
        }

        @Test
        fun `Should throw ValidationException if application does not belong to the same reviewer`() {
            val applicationNumber = "cancelApp1234"
            every { applicationService.getApplication(any()) } returns // different user
                ApplicationFixture.createApplication_SingpassSelf.copy(reviewer = UserModelFixture.userB.copy())

            assertThrows<ValidationException> {
                applicationRFAService.cancelRFA(applicationNumber, CancelRFARequestDTO(null, null))
            }.messageEqualTo("Application $applicationNumber already assigned to a different officer")
        }

        @Test
        fun `Should throw ValidationException if application cannot be RFA Cancelled`() {
            val applicationNumber = "cancelApp1234"
            every { applicationService.getApplication(any()) } returns
                ApplicationFixture.createApplication_SingpassSelf.copy(
                    reviewer = UserModelFixture.userA.copy(),
                    status = ApplicationStatus.PROCESSING
                )

            assertThrows<ValidationException> {
                applicationRFAService.cancelRFA(applicationNumber, CancelRFARequestDTO(null, null))
            }.messageEqualTo("Application $applicationNumber RFA not cancellable")
        }

        @Test
        fun `Should throw InternalConfigException if application has no RFA records`() {
            val applicationNumber = "noRFAApplication"
            every { rfaRepository.getFirstByApplicationIdOrderByIdDesc(any()) } returns null

            assertThrows<InternalConfigException> {
                applicationRFAService.cancelRFA(applicationNumber, CancelRFARequestDTO(null, null))
            }.messageEqualTo("Application $applicationNumber has no RFA records")
        }

        @Test
        fun `Should throw ValidationException if latest RFA record is not cancellable`() {
            val applicationNumber = "noRFAApplication"
            every { rfaRepository.getFirstByApplicationIdOrderByIdDesc(any()) } returns RFAFixture.baseRFA.copy(
                status = RFAStatus.RFA_RESPONDED
            )

            assertThrows<ValidationException> {
                applicationRFAService.cancelRFA(applicationNumber, CancelRFARequestDTO(null, null))
            }.messageEqualTo("Application $applicationNumber RFA not cancellable")
        }

        @Test
        fun `Should cancel RFA given valid requirement`() {
            val finalAppStatus = applicationRFAService.cancelRFA("", CancelRFARequestDTO("", ""))
            assertEquals(ApplicationStatus.PROCESSING.value, finalAppStatus.applicationStatus)
            verifyOrder {
                applicationService.getApplication(any())
                applicationService.updateApplicationCancelRFA(any(), any(), any())
                applicationRepository.save(any())
                applicationService.sendL1TUpdateStatusRequest(any())
            }
        }
    }
}
