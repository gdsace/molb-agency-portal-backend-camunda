package sg.gov.tech.molbagencyportalbackend.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.convertValue
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.runs
import io.mockk.verifyOrder
import org.hibernate.envers.AuditReader
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.multipart.MultipartFile
import sg.gov.tech.molbagencyportalbackend.auth.AuthenticationFacade
import sg.gov.tech.molbagencyportalbackend.dto.dds.DDSUploadResponseDTO
import sg.gov.tech.molbagencyportalbackend.dto.dds.LicenceDocumentType
import sg.gov.tech.molbagencyportalbackend.dto.internal.AgencyApplicationTransfer
import sg.gov.tech.molbagencyportalbackend.dto.internal.AgencyApplicationsRequestParams
import sg.gov.tech.molbagencyportalbackend.dto.internal.ApplicationModelTransfer
import sg.gov.tech.molbagencyportalbackend.dto.internal.DashboardApplicationRequestParams
import sg.gov.tech.molbagencyportalbackend.dto.internal.DashboardApplicationTransfer
import sg.gov.tech.molbagencyportalbackend.dto.internal.DocumentRequestParams
import sg.gov.tech.molbagencyportalbackend.dto.internal.RejectMessagesDTO
import sg.gov.tech.molbagencyportalbackend.dto.internal.WithdrawApplicationRequestDTO
import sg.gov.tech.molbagencyportalbackend.dto.l1t.L1TDocument
import sg.gov.tech.molbagencyportalbackend.dto.l1t.L1TMultipleDocumentRequest
import sg.gov.tech.molbagencyportalbackend.dto.l1t.L1TResponseDTOTransfer
import sg.gov.tech.molbagencyportalbackend.dto.l1t.L1TSingleDocumentRequest
import sg.gov.tech.molbagencyportalbackend.dto.l1t.WithdrawApplicationDTO
import sg.gov.tech.molbagencyportalbackend.dto.l1t.WithdrawApplicationDTO.ApplicationDTO
import sg.gov.tech.molbagencyportalbackend.dto.l1t.WithdrawApplicationDTO.GeneralDTO
import sg.gov.tech.molbagencyportalbackend.exception.ExceptionControllerAdvice
import sg.gov.tech.molbagencyportalbackend.exception.FileDownloadException
import sg.gov.tech.molbagencyportalbackend.exception.NotAuthorisedException
import sg.gov.tech.molbagencyportalbackend.exception.NotFoundException
import sg.gov.tech.molbagencyportalbackend.exception.ValidationException
import sg.gov.tech.molbagencyportalbackend.fixture.ActivityTypeFixture
import sg.gov.tech.molbagencyportalbackend.fixture.AgencyFixture
import sg.gov.tech.molbagencyportalbackend.fixture.ApplicationDetailDTOFixture
import sg.gov.tech.molbagencyportalbackend.fixture.ApplicationFixture
import sg.gov.tech.molbagencyportalbackend.fixture.ApproveApplicationRequestDTOFixture
import sg.gov.tech.molbagencyportalbackend.fixture.CreateApplicationRequestDTOFixture
import sg.gov.tech.molbagencyportalbackend.fixture.DashboardStatisticsDTOFixture
import sg.gov.tech.molbagencyportalbackend.fixture.LicenceModelFixture
import sg.gov.tech.molbagencyportalbackend.fixture.LicenceTypeFixture
import sg.gov.tech.molbagencyportalbackend.fixture.RFAFixture
import sg.gov.tech.molbagencyportalbackend.fixture.ReassignUsersModelFixture
import sg.gov.tech.molbagencyportalbackend.fixture.ResourceResponseDTOFixture
import sg.gov.tech.molbagencyportalbackend.fixture.UserFixture
import sg.gov.tech.molbagencyportalbackend.fixture.UserModelFixture
import sg.gov.tech.molbagencyportalbackend.model.ActivityValue
import sg.gov.tech.molbagencyportalbackend.model.Application
import sg.gov.tech.molbagencyportalbackend.model.ApplicationStatus
import sg.gov.tech.molbagencyportalbackend.model.RFAStatus
import sg.gov.tech.molbagencyportalbackend.repository.ApplicationRepository
import sg.gov.tech.molbagencyportalbackend.repository.LicenceRepository
import sg.gov.tech.molbagencyportalbackend.repository.UserRepository
import sg.gov.tech.molbagencyportalbackend.util.ApplicationConstants
import sg.gov.tech.molbagencyportalbackend.util.DateUtil
import sg.gov.tech.molbagencyportalbackend.util.EncryptLicenceDataUtil
import sg.gov.tech.molbagencyportalbackend.util.aws.AwsSqsUtil
import sg.gov.tech.testing.MolbUnitTesting
import sg.gov.tech.testing.messageEqualTo
import sg.gov.tech.testing.verifyNever
import sg.gov.tech.testing.verifyOnce
import sg.gov.tech.utils.ObjectMapperConfigurer

@MolbUnitTesting
internal class ApplicationServiceTest {

    @MockK
    private lateinit var applicationRepository: ApplicationRepository

    @MockK
    private lateinit var agencyService: AgencyService

    @MockK
    private lateinit var licenceTypeService: LicenceTypeService

    @MockK
    private lateinit var dashboardApplicationTransfer: DashboardApplicationTransfer

    @MockK
    private lateinit var agencyApplicationTransfer: AgencyApplicationTransfer

    @MockK
    private lateinit var applicationModelTransfer: ApplicationModelTransfer

    @MockK
    private lateinit var l1tResponseDTOTransfer: L1TResponseDTOTransfer

    @MockK
    private lateinit var userService: UserService

    @MockK
    private lateinit var l1tIntegrationService: L1TIntegrationService

    @MockK
    private lateinit var licenceService: LicenceService

    @MockK
    private lateinit var licenceRepository: LicenceRepository

    @MockK
    private lateinit var ddsIntegrationService: DDSIntegrationService

    @MockK
    private lateinit var activityTypeService: ActivityTypeService

    @MockK
    private lateinit var awsSqsUtil: AwsSqsUtil

    @MockK
    private lateinit var authenticationFacade: AuthenticationFacade

    @MockK
    private lateinit var encryptService: EncryptLicenceDataUtil

    @MockK
    private lateinit var userRepository: UserRepository

    @MockK
    private lateinit var auditReader: AuditReader

    @MockK
    private lateinit var rfaService: ApplicationRFAService

    @InjectMockKs
    private lateinit var applicationService: ApplicationService

    private val pageRequest = PageRequest.of(0, 2)
    private val openCasesPage = mockk<Page<Application>> {
        every { content } returns DashboardStatisticsDTOFixture.openCases
        every { pageable } returns pageRequest
        every { totalElements } returns 2
    }
    private val unassignedCasesPage = mockk<Page<Application>> {
        every { content } returns DashboardStatisticsDTOFixture.unassignedCases
        every { pageable } returns pageRequest
        every { totalElements } returns 2
    }
    private val applicationsCasesPage = mockk<Page<Application>> {
        every { content } returns DashboardStatisticsDTOFixture.applicationsCases
        every { pageable } returns pageRequest
        every { totalElements } returns 2
    }

    @BeforeEach
    fun setup() {
        every { agencyService.findByCode(any()) } returns AgencyFixture.agency
        every { licenceTypeService.findByLicenceId(any()) } returns LicenceTypeFixture.licenceType
    }

    @Test
    fun `Should return true if applicationNumber exist`() {
        every { applicationRepository.existsByApplicationNumber(any()) } returns true
        assertTrue(applicationService.existsByApplicationNumber("valid_applicationNumber"))
    }

    @Test
    fun `Should return false if applicationNumber does not exist`() {
        every { applicationRepository.existsByApplicationNumber(any()) } returns false
        assertFalse(applicationService.existsByApplicationNumber("valid_applicationNumber"))
    }

    @Test
    fun `Should return correct Login type`() {
        assertEquals(
            "Corppass",
            applicationService.deriveLoginType(CreateApplicationRequestDTOFixture.applicationDTO_CorppassSelf)
        )
        assertEquals(
            "Singpass",
            applicationService.deriveLoginType(CreateApplicationRequestDTOFixture.applicationDTO_SingpassSelf)
        )
    }

    @Test
    fun `Should return created Application`() {
        every {
            encryptService.encryptLicenceNode(any(), any(), true)
        } returns ApplicationFixture.createApplication_SingpassSelf.licenceDataFields
        every { applicationRepository.save(any()) } returns ApplicationFixture.createApplication_SingpassSelf

        every { activityTypeService.getActivityType(any()) } returns ActivityTypeFixture.activityType

        val application =
            applicationService.createApplication(CreateApplicationRequestDTOFixture.createApplicationRequest)

        assertEquals(
            CreateApplicationRequestDTOFixture.createApplicationRequest.application.general.applicationNumber,
            application.applicationNumber
        )
        assertEquals(ApplicationStatus.SUBMITTED, application.status)
    }

    @Test
    fun `Should call create application in correct order`() {
        val encryptedLicenceDataFields: JsonNode =
            ObjectMapperConfigurer.GlobalObjectMapper.convertValue("")
        every {
            encryptService.encryptLicenceNode(any(), any(), true)
        } returns encryptedLicenceDataFields
        every { applicationRepository.save(any()) } returns ApplicationFixture.createApplication_SingpassSelf

        every { activityTypeService.getActivityType(any()) } returns ActivityTypeFixture.activityType

        every {
            l1tResponseDTOTransfer.createResultDTO(any(), any(), any(), any())
        } returns ResourceResponseDTOFixture.result
        every {
            l1tResponseDTOTransfer.createSuccessResponseDTO(any())
        } returns ResourceResponseDTOFixture.responseSuccess

        applicationService.createApplicationDetails(CreateApplicationRequestDTOFixture.createApplicationRequest)

        verifyOrder {
            encryptService.encryptLicenceNode(any(), any(), true)
            agencyService.findByCode(any())
            licenceTypeService.findByLicenceId(any())
            applicationRepository.save(any())
            l1tResponseDTOTransfer.createResultDTO(any(), any(), any(), any())
            l1tResponseDTOTransfer.createSuccessResponseDTO(any())
        }
    }

    @Test
    fun `Should call validation in correct order`() {
        every {
            l1tResponseDTOTransfer.createResultDTO(any(), any(), any(), any())
        } returns ResourceResponseDTOFixture.result
        every {
            l1tResponseDTOTransfer.createSuccessResponseDTO(any())
        } returns ResourceResponseDTOFixture.responseSuccess

        applicationService.validateApplicationDetails(CreateApplicationRequestDTOFixture.createApplicationRequest)

        verifyOrder {
            l1tResponseDTOTransfer.createResultDTO(any(), any(), any(), any())
            l1tResponseDTOTransfer.createSuccessResponseDTO(any())
        }
    }

    @Test
    fun `Should return openCase Application`() {
        every { authenticationFacade.getPrincipalName() } returns "supervisor@tech.gov.sg"
        every { userService.getUserByEmailAndIsDeletedFalse("supervisor@tech.gov.sg") } returns UserModelFixture.userA
        // unassignedCases count
        every {
            applicationRepository.countAllByAgencyIdAndReviewerIdIsNullAndStatusNotIn(any(), any())
        } returns DashboardStatisticsDTOFixture.unassignedCases.size

        // openCases count
        every {
            applicationRepository.countAllByAgencyIdAndReviewerIdAndStatusIn(any(), any(), any())
        } returns DashboardStatisticsDTOFixture.openCases.size

        every {
            applicationRepository.findAllByAgencyIdAndReviewerIdAndStatusIn(any(), any(), any(), any())
        } returns openCasesPage

        every { dashboardApplicationTransfer.toDTO(any()) } answers { callOriginal() }

        // openCases
        val applications = applicationService.getDashboardApplications(
            DashboardApplicationRequestParams("openCases", 0, "status", "asc", 10)
        )

        assertEquals(2, applications.openCasesCount)
        assertEquals(2, applications.unassignedCasesCount)
        assertEquals("A1234567", openCasesPage.content[0].applicationNumber)
        assertEquals("A1234568", openCasesPage.content[1].applicationNumber)
    }

    @Test
    fun `Should return unassignedCase Application`() {
        every { authenticationFacade.getPrincipalName() } returns "supervisor@tech.gov.sg"
        every { userService.getUserByEmailAndIsDeletedFalse("supervisor@tech.gov.sg") } returns UserModelFixture.userA
        // unassignedCases count
        every {
            applicationRepository.countAllByAgencyIdAndReviewerIdIsNullAndStatusNotIn(any(), any())
        } returns DashboardStatisticsDTOFixture.unassignedCases.size

        // openCases count
        every {
            applicationRepository.countAllByAgencyIdAndReviewerIdAndStatusIn(any(), any(), any())
        } returns DashboardStatisticsDTOFixture.openCases.size

        every {
            applicationRepository.findAllByAgencyIdAndReviewerIdIsNullAndStatusNotIn(any(), any(), any())
        } returns unassignedCasesPage

        every { dashboardApplicationTransfer.toDTO(any()) } answers { callOriginal() }

        // openCases
        val applications = applicationService.getDashboardApplications(
            DashboardApplicationRequestParams("unassigned", 0, "status", "asc", 10)
        )

        assertEquals(2, applications.openCasesCount)
        assertEquals(2, applications.unassignedCasesCount)
        assertEquals("A1234569", unassignedCasesPage.content[0].applicationNumber)
        assertEquals("A1234570", unassignedCasesPage.content[1].applicationNumber)
    }

    @Test
    fun `Should return agency count and Application list`() {
        every { authenticationFacade.getPrincipalName() } returns "supervisor@tech.gov.sg"
        every { userService.getUserByEmailAndIsDeletedFalse("supervisor@tech.gov.sg") } returns UserModelFixture.userA
        // agency applications count
        every {
            applicationRepository.countAllByAgencyId(any())
        } returns DashboardStatisticsDTOFixture.applicationsCases.size

        every {
            applicationRepository.findAllByAgencyId(any(), any())
        } returns applicationsCasesPage

        every { agencyApplicationTransfer.toDTO(any()) } answers { callOriginal() }

        val agencyApplications = applicationService.getAgencyApplications(
            AgencyApplicationsRequestParams(0, "status", "asc", 10)
        )

        assertEquals(2, agencyApplications.totalCount)
        assertEquals("A1234571", applicationsCasesPage.content[0].applicationNumber)
        assertEquals("A1234572", applicationsCasesPage.content[1].applicationNumber)
    }

    @Test
    fun `should throw NotFoundException is applicationNumber is incorrect`() {
        every {
            applicationRepository.findByApplicationNumber(any())
        } returns null
        assertThrows<NotFoundException> {
            applicationService.getApplication("123")
        }.messageEqualTo("Can't find the application with reference number: 123")
    }

    @Test
    fun `should return Application if the applicationNumber is valid`() {
        every {
            applicationRepository.findByApplicationNumber(any())
        } returns ApplicationFixture.createApplication_SingpassSelf
        val application = applicationService.getApplication("A1234567")
        assertEquals(
            ApplicationFixture.createApplication_SingpassSelf.applicationNumber,
            application.applicationNumber
        )
    }

    @Nested
    inner class ApplicationMasking {
        @Test
        fun `should mask the nric for getDashboardApplications`() {
            every { authenticationFacade.getPrincipalName() } returns "supervisor@tech.gov.sg"
            every {
                userService.getUserByEmailAndIsDeletedFalse("supervisor@tech.gov.sg")
            } returns UserModelFixture.userA
            // unassignedCases count
            every {
                applicationRepository.countAllByAgencyIdAndReviewerIdIsNullAndStatusNotIn(any(), any())
            } returns DashboardStatisticsDTOFixture.unassignedCases.size

            // openCases count
            every {
                applicationRepository.countAllByAgencyIdAndReviewerIdAndStatusIn(any(), any(), any())
            } returns DashboardStatisticsDTOFixture.openCases.size

            every {
                applicationRepository.findAllByAgencyIdAndReviewerIdAndStatusIn(any(), any(), any(), any())
            } returns openCasesPage

            every { dashboardApplicationTransfer.toDTO(any()) } answers { callOriginal() }

            // openCases
            val applications = applicationService.getDashboardApplications(
                DashboardApplicationRequestParams(
                    "openCases",
                    0,
                    "status",
                    "asc",
                    10
                )
            )

            assertEquals(2, applications.openCasesCount)
            assertEquals(2, applications.unassignedCasesCount)
            assertEquals("*****XXXXXA", applications.applications[0].applicant.idNumber)
        }

        @Test
        fun `Should mask pii for getAgencyApplications`() {
            every { authenticationFacade.getPrincipalName() } returns "supervisor@tech.gov.sg"
            every {
                userService.getUserByEmailAndIsDeletedFalse("supervisor@tech.gov.sg")
            } returns UserModelFixture.userA
            // agency applications count
            every {
                applicationRepository.countAllByAgencyId(any())
            } returns DashboardStatisticsDTOFixture.applicationsCases.size

            every {
                applicationRepository.findAllByAgencyId(any(), any())
            } returns applicationsCasesPage

            every { agencyApplicationTransfer.toDTO(any()) } answers { callOriginal() }

            val agencyApplications = applicationService.getAgencyApplications(
                AgencyApplicationsRequestParams(0, "status", "asc", 10)
            )

            assertEquals(2, agencyApplications.totalCount)
            assertEquals(
                "*****XXXXXA",
                agencyApplications.data[0].applicant.idNumber
            )
        }
    }

    @Nested
    @Transactional
    inner class ApplicationDetails {
        @Test
        fun `should return ApplicationDetailDTO if the application number is valid`() {
            every {
                applicationRepository.findByApplicationNumber(any())
            } returns ApplicationFixture.createApplication_SingpassSelf
            every {
                userService.getUserByEmailAndIsDeletedFalse(any())
            } returns UserModelFixture.userA
            every {
                applicationModelTransfer.toDTO(any())
            } returns ApplicationDetailDTOFixture.getApplication

            val applicationDetailDTO = applicationService.getApplicationDetails("A1234567")
            assertEquals(
                ApplicationDetailDTOFixture.getApplication.applicationNumber,
                applicationDetailDTO.applicationNumber
            )
        }

        @Test
        fun `should return ApplicationDetailDTO with null message and remarks`() {
            every {
                applicationRepository.findByApplicationNumber(any())
            } returns ApplicationFixture.createApplication_SingpassSelf
            every {
                userService.getUserByEmailAndIsDeletedFalse(any())
            } returns UserModelFixture.userA
            every {
                applicationModelTransfer.toDTO(any())
            } returns ApplicationDetailDTOFixture.getApplication

            val applicationDetailDTO = applicationService.getApplicationDetails("A1234567")
            assertEquals(
                ApplicationDetailDTOFixture.getApplication.internalRemarks,
                applicationDetailDTO.internalRemarks
            )
            assertEquals(
                ApplicationDetailDTOFixture.getApplication.messageToApplicant,
                applicationDetailDTO.messageToApplicant
            )
            assertEquals(
                ApplicationDetailDTOFixture.getApplication.updatedBy,
                applicationDetailDTO.updatedBy
            )
        }

        @Test
        fun `should return ApplicationDetailDTO with message and remarks`() {
            every {
                applicationRepository.findByApplicationNumber(any())
            } returns ApplicationFixture.createApplication_SingpassSelf
            every {
                userService.getUserByEmailAndIsDeletedFalse(any())
            } returns UserModelFixture.userA
            every {
                applicationModelTransfer.toDTO(any())
            } returns ApplicationDetailDTOFixture.getApplicationWithRemarksMessage

            val applicationDetailDTO = applicationService.getApplicationDetails("A1234567")
            assertEquals(
                ApplicationDetailDTOFixture.getApplicationWithRemarksMessage.internalRemarks,
                applicationDetailDTO.internalRemarks
            )
            assertEquals(
                ApplicationDetailDTOFixture.getApplicationWithRemarksMessage.messageToApplicant,
                applicationDetailDTO.messageToApplicant
            )
            assertEquals(
                ApplicationDetailDTOFixture.getApplication.updatedBy,
                applicationDetailDTO.updatedBy
            )
        }

        @Test
        fun `should throw not authorized exception if user agency is not same as application agency`() {
            val principalEmail = "supervisor@tech.gov.sg"
            val applicationNumber = ApplicationFixture.createApplication_SingpassSelf.applicationNumber
            every {
                userService.getUserByEmailAndIsDeletedFalse(any())
            } returns UserModelFixture.supervisorFromAgencyTwo
            every { authenticationFacade.getPrincipalName() } returns principalEmail
            every {
                applicationRepository.findByApplicationNumber(any())
            } returns ApplicationFixture.createApplication_SingpassSelf
            assertThrows<NotAuthorisedException> {
                applicationService.getApplicationDetails(applicationNumber)
            }.messageEqualTo(
                "User $principalEmail is not authorised to access other agency application: $applicationNumber"
            )
        }
    }

    @Nested
    inner class RetrieveDocument {
        @Test
        fun `should return a zip of multiple files if request contain multiple filename`() {
            val principalEmail = "supervisor@tech.gov.sg"
            val sampleTime = "202208031200000"
            val sampleApplicationNumber = "FN0000001"
            val sampleFilename1 = "filename.pdf"
            val sampleFilename2 = "filename2.pdf"

            val sampleRequestParam = DocumentRequestParams(
                listOf(sampleFilename1, sampleFilename2)
            )
            val expectedPayload = L1TMultipleDocumentRequest(
                "gbl",
                listOf(
                    L1TDocument(
                        sampleFilename1,
                        "${sampleApplicationNumber}_$sampleFilename1"
                    ),
                    L1TDocument(
                        sampleFilename2,
                        "${sampleApplicationNumber}_$sampleFilename2"
                    )
                ),
                "${sampleApplicationNumber}_$sampleTime.zip"
            )

            val sampleResponseEntity = ResponseEntity(
                "sampleFile".toByteArray(),
                HttpHeaders(),
                HttpStatus.OK

            )

            mockkObject(DateUtil)
            every { DateUtil.getTimestamp() } returns sampleTime
            every { authenticationFacade.getPrincipalName() } returns principalEmail
            every { userService.getUserByEmailAndIsDeletedFalse(any()) } returns UserModelFixture.userA
            every {
                applicationRepository.findByApplicationNumber(any())
            } returns ApplicationFixture.createApplication_SingpassSelf
            every { l1tIntegrationService.downloadMultipleFiles(expectedPayload) } returns sampleResponseEntity

            val file =
                applicationService.retrieveDocument(sampleApplicationNumber, sampleRequestParam)

            assertEquals(sampleResponseEntity, file)
        }

        @Test
        fun `should return a single file if request contains single filename`() {
            val principalEmail = "supervisor@tech.gov.sg"
            val sampleApplicationNumber = "FN0000001"
            val sampleFilename = "sampleFile.pdf"
            val sampleRequestParam = DocumentRequestParams(
                listOf(sampleFilename)
            )
            val sampleResponseEntity = ResponseEntity(
                "sampleFile".toByteArray(),
                HttpHeaders(),
                HttpStatus.OK
            )
            val expectedPayload = L1TSingleDocumentRequest(
                "gbl",
                sampleFilename,
                "${sampleApplicationNumber}_$sampleFilename"
            )
            every { authenticationFacade.getPrincipalName() } returns principalEmail
            every { userService.getUserByEmailAndIsDeletedFalse(any()) } returns UserModelFixture.userA
            every {
                applicationRepository.findByApplicationNumber(any())
            } returns ApplicationFixture.createApplication_SingpassSelf
            every { l1tIntegrationService.downloadSingleFile(expectedPayload) } returns sampleResponseEntity

            val file =
                applicationService.retrieveDocument(sampleApplicationNumber, sampleRequestParam)

            assertEquals(sampleResponseEntity, file)
        }

        @Test
        fun `should throw NotFoundException if application is not valid`() {
            val principalEmail = "supervisor@tech.gov.sg"
            val sampleApplicationNumber = "FN0000001"
            val sampleFilename = "sampleFile.pdf"
            val sampleRequestParam = DocumentRequestParams(
                listOf(sampleFilename)
            )
            every { authenticationFacade.getPrincipalName() } returns principalEmail
            every { userService.getUserByEmailAndIsDeletedFalse(any()) } returns UserModelFixture.userA
            every { applicationRepository.findByApplicationNumber(any()) } returns null

            assertThrows<NotFoundException> {
                applicationService.retrieveDocument(sampleApplicationNumber, sampleRequestParam)
            }.messageEqualTo("Can't find the application with reference number: $sampleApplicationNumber")
        }

        @Test
        fun `should throw NotAuthorisedException if application is not valid`() {
            val principalEmail = "supervisor@tech.gov.sg"
            val sampleApplicationNumber = "FN0000001"
            val sampleFilename = "sampleFile.pdf"
            val sampleRequestParam = DocumentRequestParams(
                listOf(sampleFilename)
            )
            every { authenticationFacade.getPrincipalName() } returns principalEmail
            every {
                userService
                    .getUserByEmailAndIsDeletedFalse(any())
            } returns UserModelFixture.supervisorFromAgencyTwo
            every {
                applicationRepository.findByApplicationNumber(any())
            } returns ApplicationFixture.createApplication_SingpassSelf

            assertThrows<NotAuthorisedException> {
                applicationService.retrieveDocument(sampleApplicationNumber, sampleRequestParam)
            }.messageEqualTo(
                "User $principalEmail is not authorised to access other agency application: $sampleApplicationNumber"
            )
        }

        @Test
        fun `should throw FileDownloadException if L1TDocumentClient return error`() {
            val principalEmail = "supervisor@tech.gov.sg"
            val sampleApplicationNumber = "FN0000001"
            val sampleFilename = "sampleFile.pdf"
            val sampleRequestParam = DocumentRequestParams(
                listOf(sampleFilename)
            )
            val expectedPayload = L1TSingleDocumentRequest(
                "gbl",
                sampleFilename,
                "${sampleApplicationNumber}_$sampleFilename"
            )

            val exception = HttpClientErrorException(
                HttpStatus.SERVICE_UNAVAILABLE,
                HttpStatus.SERVICE_UNAVAILABLE.reasonPhrase,
                "{\"code\":\"SERVICE UNAVAILABLE\"}".toByteArray(Charsets.UTF_8),
                Charsets.UTF_8
            )
            every { authenticationFacade.getPrincipalName() } returns principalEmail
            every { userService.getUserByEmailAndIsDeletedFalse(any()) } returns UserModelFixture.userA
            every {
                applicationRepository.findByApplicationNumber(any())
            } returns ApplicationFixture.createApplication_SingpassSelf
            every { l1tIntegrationService.downloadSingleFile(expectedPayload) } throws exception

            assertThrows<FileDownloadException> {
                applicationService.retrieveDocument(sampleApplicationNumber, sampleRequestParam)
            }.messageEqualTo(ExceptionControllerAdvice.FILE_DOWNLOAD_ERROR_MESSAGE)
        }
    }

    @Nested
    @Transactional
    inner class ClaimApplication {
        @Test
        fun `should throw NotFoundException given application number does not exist`() {
            val invalidApplicationNumber = "A1234567"
            every {
                applicationRepository.findByApplicationNumber(any())
            } returns null

            assertThrows<NotFoundException> {
                applicationService.claimApplication(invalidApplicationNumber)
            }.messageEqualTo("Can't find the application with reference number: $invalidApplicationNumber")
        }

        @Test
        fun `should throw NotAuthorisedException given application does not belong to same agency as reviewer`() {
            val principalEmail = "supervisor@tech.gov.sg"
            val applicationNumber = "A1234567"
            every { authenticationFacade.getPrincipalName() } returns principalEmail
            every {
                userService.getUserByEmailAndIsDeletedFalse(principalEmail)
            } returns UserModelFixture.userA
            every {
                applicationRepository.findByApplicationNumber(any())
            } returns ApplicationFixture.createApplication_SingpassSelf.copy(
                agency = AgencyFixture.agency.copy(id = 2)
            )

            assertThrows<NotAuthorisedException> {
                applicationService.claimApplication(applicationNumber)
            }.messageEqualTo(
                "User $principalEmail is not authorised to access other agency application: $applicationNumber"
            )
        }

        @Test
        fun `should throw ValidationException given application has assigned reviewer`() {
            every { authenticationFacade.getPrincipalName() } returns "btest@test.com"
            every {
                userService.getUserByEmailAndIsDeletedFalse(any())
            } returns UserModelFixture.userB

            val applicationNumber = "A1234567"
            every {
                applicationRepository.findByApplicationNumber(any())
            } returns ApplicationFixture.createApplication_SingpassSelf.copy(
                reviewer = UserModelFixture.userB
            )

            assertThrows<ValidationException> {
                applicationService.claimApplication(applicationNumber)
            }.messageEqualTo("Application $applicationNumber already assigned to a different officer")
        }

        @Test
        fun `should throw ValidationException given application cannot be claimed`() {
            val applicationNumber = "A1234567"
            every {
                applicationRepository.findByApplicationNumber(any())
            } returns ApplicationFixture.createApplication_SingpassSelf.copy(
                status = ApplicationStatus.APPROVED
            )

            assertThrows<ValidationException> {
                applicationService.claimApplication(applicationNumber)
            }.messageEqualTo("Application $applicationNumber cannot be claimed")
        }

        @Test
        fun `should update application status given valid application fulfils requirement to be claimed`() {
            val applicationNumber = "A1234567"
            val reviewerId = 1L

            val user = UserModelFixture.userA.copy()
            val application = ApplicationFixture.createApplication_SingpassSelf.copy()
            every { authenticationFacade.getPrincipalName() } returns "supervisor@tech.gov.sg"
            every {
                userService.getUserByEmailAndIsDeletedFalse("supervisor@tech.gov.sg")
            } returns UserModelFixture.userA

            every {
                applicationRepository.findByApplicationNumber(any())
            } returns application

            val expectedApplication = application.copy(
                reviewer = user,
                status = ApplicationStatus.PROCESSING,
                caseStatus = ApplicationStatus.PROCESSING.getCaseStatus(user),
                activityType = ActivityTypeFixture.activityType.copy(
                    id = 2,
                    type = ActivityValue.CLAIM_APPLICATION
                )
            )

            every { activityTypeService.getActivityType(any()) } returns ActivityTypeFixture.activityType.copy(
                id = 2,
                type = ActivityValue.CLAIM_APPLICATION
            )

            every { userService.getUserById(reviewerId) } returns user
            every { applicationRepository.save(any()) } returns expectedApplication
            every { l1tIntegrationService.createL1TApplicationStatusRequest(any()) } returns mockk()
            every { awsSqsUtil.sendL1TUpdateStatusSqsMessage(any()) } returns mockk()

            applicationService.claimApplication(applicationNumber)

            verifyOrder {
                applicationRepository.save(expectedApplication)
                awsSqsUtil.sendL1TUpdateStatusSqsMessage(any())
            }
        }

        @Test
        fun `should return application updated with reviewer, status and case status given status in submitted list`() {
            val application = ApplicationFixture.createApplication_SingpassSelf.copy()
            val user = UserModelFixture.userA

            val expectedApplication = ApplicationFixture.createApplication_SingpassSelf.copy(
                reviewer = user,
                status = ApplicationStatus.PROCESSING,
                caseStatus = ApplicationStatus.PROCESSING.getCaseStatus(user),
                activityType = ActivityTypeFixture.activityType.copy(
                    id = 2,
                    type = ActivityValue.CLAIM_APPLICATION
                )
            )

            every { activityTypeService.getActivityType(any()) } returns ActivityTypeFixture.activityType.copy(
                id = 2,
                type = ActivityValue.CLAIM_APPLICATION
            )

            assertEquals(
                expectedApplication,
                applicationService.run { application.assignAndUpdateStatusAndOwnership(user) }
            )
        }

        @Test
        fun `should return application updated with reviewer given status not in submitted list`() {
            val application = ApplicationFixture.createApplication_SingpassSelf.copy(
                status = ApplicationStatus.PENDING_WITHDRAWAL,
                caseStatus = ApplicationStatus.PENDING_WITHDRAWAL.getCaseStatus(null)
            )
            val user = UserModelFixture.userA

            val expectedApplication = application.copy(
                reviewer = user,
                status = ApplicationStatus.PENDING_WITHDRAWAL,
                caseStatus = ApplicationStatus.PENDING_WITHDRAWAL.getCaseStatus(user),
                activityType = ActivityTypeFixture.activityType.copy(
                    id = 2,
                    type = ActivityValue.CLAIM_APPLICATION
                )
            )

            every { activityTypeService.getActivityType(any()) } returns ActivityTypeFixture.activityType.copy(
                id = 2,
                type = ActivityValue.CLAIM_APPLICATION
            )

            assertEquals(
                expectedApplication,
                applicationService.run { application.assignAndUpdateStatusAndOwnership(user) }
            )
        }
    }

    @Nested
    @Transactional
    inner class RejectApplication {
        @Test
        fun `should throw NotFoundException given application number does not exist`() {
            val invalidApplicationNumber = "A1234567"
            every {
                applicationRepository.findByApplicationNumber(any())
            } returns null

            assertThrows<NotFoundException> {
                applicationService.rejectApplication(
                    invalidApplicationNumber,
                    RejectMessagesDTO(null, null)
                )
            }.messageEqualTo("Can't find the application with reference number: $invalidApplicationNumber")
        }

        @Test
        fun `should throw NotAuthorisedException given application does not belong to same agency as reviewer`() {
            val applicationNumber = "A1234567"
            every {
                applicationRepository.findByApplicationNumber(any())
            } returns ApplicationFixture.createApplication_SingpassSelf.copy(
                agency = AgencyFixture.agency.copy(id = 2)
            )

            assertThrows<NotAuthorisedException> {
                applicationService.rejectApplication(
                    applicationNumber,
                    RejectMessagesDTO(null, null)
                )
            }.messageEqualTo(
                "User supervisor@tech.gov.sg is not authorised to access other agency application: $applicationNumber"
            )
        }

        @Test
        fun `should throw ValidationException given application not belongs to same reviewer`() {
            val applicationNumber = "A1234567"
            val user = UserModelFixture.userB.copy()
            every { authenticationFacade.getPrincipalName() } returns "supervisor@tech.gov.sg"
            every {
                userService.getUserByEmailAndIsDeletedFalse("supervisor@tech.gov.sg")
            } returns UserModelFixture.userA

            every {
                applicationRepository.findByApplicationNumber(any())
            } returns ApplicationFixture.createApplication_SingpassSelf.copy(
                status = ApplicationStatus.PROCESSING,
                reviewer = user
            )

            assertThrows<ValidationException> {
                applicationService.rejectApplication(
                    applicationNumber,
                    RejectMessagesDTO(null, null)
                )
            }.messageEqualTo("Application $applicationNumber already assigned to a different officer")
        }

        @Test
        fun `should throw ValidationException given application cannot be rejected`() {
            val applicationNumber = "A1234567"
            val user = UserModelFixture.userA.copy()
            every {
                applicationRepository.findByApplicationNumber(any())
            } returns ApplicationFixture.createApplication_SingpassSelf.copy(
                reviewer = user,
                status = ApplicationStatus.APPROVED
            )

            assertThrows<ValidationException> {
                applicationService.rejectApplication(
                    applicationNumber,
                    RejectMessagesDTO(null, null)
                )
            }.messageEqualTo("Application $applicationNumber cannot be rejected")
        }

        @Test
        fun `should update application status given valid requirement to be rejected with null message`() {
            val applicationNumber = "A1234567"
            val reviewerId = 1L

            val user = UserModelFixture.userA.copy()
            val application = ApplicationFixture.createApplication_SingpassSelf.copy(
                reviewer = user,
                status = ApplicationStatus.PROCESSING,
                caseStatus = ApplicationStatus.PROCESSING.getCaseStatus(user)
            )
            every { authenticationFacade.getPrincipalName() } returns "supervisor@tech.gov.sg"
            every {
                userService.getUserByEmailAndIsDeletedFalse("supervisor@tech.gov.sg")
            } returns UserModelFixture.userA

            every {
                applicationRepository.findByApplicationNumber(any())
            } returns application

            val expectedApplication = application.copy(
                reviewer = user,
                status = ApplicationStatus.REJECTED,
                caseStatus = ApplicationStatus.REJECTED.getCaseStatus(user),
                activityType = ActivityTypeFixture.activityType.copy(
                    id = 3,
                    type = ActivityValue.REJECT_APPLICATION
                )
            )

            every { activityTypeService.getActivityType(any()) } returns ActivityTypeFixture.activityType.copy(
                id = 3,
                type = ActivityValue.REJECT_APPLICATION
            )

            every { userService.getUserById(reviewerId) } returns user
            every { applicationRepository.save(any()) } returns expectedApplication
            every { l1tIntegrationService.createL1TApplicationStatusRequest(any()) } returns mockk()
            every { awsSqsUtil.sendL1TUpdateStatusSqsMessage(any()) } returns mockk()

            applicationService.rejectApplication(applicationNumber, RejectMessagesDTO(null, null))

            verifyOrder {
                applicationRepository.save(expectedApplication)
                awsSqsUtil.sendL1TUpdateStatusSqsMessage(any())
            }
        }

        @Test
        fun `should update application status given fulfils requirement to be rejected with some message`() {
            val applicationNumber = "A1234567"
            val reviewerId = 1L

            val user = UserModelFixture.userA.copy()
            val application = ApplicationFixture.createApplication_SingpassSelf.copy(
                reviewer = user,
                status = ApplicationStatus.PROCESSING,
                caseStatus = ApplicationStatus.PROCESSING.getCaseStatus(user)
            )
            every { authenticationFacade.getPrincipalName() } returns "supervisor@tech.gov.sg"
            every {
                userService.getUserByEmailAndIsDeletedFalse("supervisor@tech.gov.sg")
            } returns UserModelFixture.userA

            every {
                applicationRepository.findByApplicationNumber(any())
            } returns application

            val expectedApplication = application.copy(
                reviewer = user,
                status = ApplicationStatus.REJECTED,
                caseStatus = ApplicationStatus.REJECTED.getCaseStatus(user),
                activityType = ActivityTypeFixture.activityType.copy(
                    id = 3,
                    type = ActivityValue.REJECT_APPLICATION
                ),
                internalRemarks = "This is a test remarks",
                messageToApplicant = "This is a test message"
            )

            every { activityTypeService.getActivityType(any()) } returns ActivityTypeFixture.activityType.copy(
                id = 3,
                type = ActivityValue.REJECT_APPLICATION
            )
            every { userService.getUserById(reviewerId) } returns user
            every { applicationRepository.save(any()) } returns expectedApplication

            applicationService.rejectApplication(
                applicationNumber,
                RejectMessagesDTO(
                    "This is a test remarks",
                    "This is a test message"
                )
            )

            verifyOnce { applicationRepository.save(expectedApplication) }
        }

        @Test
        fun `should return application updated status and case status given status in rejection list`() {
            val application = ApplicationFixture.createApplication_SingpassSelf.copy()
            val user = UserModelFixture.userA

            val expectedApplication = ApplicationFixture.createApplication_SingpassSelf.copy(
                reviewer = user,
                status = ApplicationStatus.PROCESSING,
                caseStatus = ApplicationStatus.PROCESSING.getCaseStatus(user),
                activityType = ActivityTypeFixture.activityType.copy(
                    id = 3,
                    type = ActivityValue.REJECT_APPLICATION
                )
            )

            every { activityTypeService.getActivityType(any()) } returns ActivityTypeFixture.activityType.copy(
                id = 3,
                type = ActivityValue.REJECT_APPLICATION
            )

            assertEquals(
                expectedApplication,
                applicationService.run { application.assignAndUpdateStatusAndOwnership(user) }
            )
        }
    }

    @Nested
    inner class ApproveApplication {
        @Test
        fun `should return success if approve application without documents`() {
            val application = ApplicationFixture.createApplication_SingpassSelf.copy(
                reviewer = UserModelFixture.userA,
                status = ApplicationStatus.PROCESSING,
                caseStatus = ApplicationStatus.PROCESSING.getCaseStatus(UserModelFixture.userA)
            )
            val expectedApplication = ApplicationFixture.createApplication_SingpassSelf.copy(
                reviewer = UserModelFixture.userA,
                status = ApplicationStatus.PARTIALLY_APPROVED,
                caseStatus = ApplicationStatus.PARTIALLY_APPROVED.getCaseStatus(application.reviewer),
                messageToApplicant = "test message to applicant",
                internalRemarks = "test internal remark",
                activityType = ActivityTypeFixture.activityType.copy(
                    id = 4,
                    type = ActivityValue.APPROVE_APPLICATION
                )
            )
            every { applicationService.getApplication(application.applicationNumber) } returns application
            every { licenceService.createLicence(any(), any(), any()) } just runs
            every { licenceRepository.save(any()) } returns LicenceModelFixture.licence
            every { applicationRepository.save(any()) } returns expectedApplication
            every { activityTypeService.getActivityType(any()) } returns ActivityTypeFixture.activityType.copy(
                id = 3,
                type = ActivityValue.APPROVE_APPLICATION
            )
            every { l1tIntegrationService.createL1TApplicationStatusRequest(any()) } returns mockk()
            every { awsSqsUtil.sendL1TUpdateStatusSqsMessage(any()) } returns mockk()

            applicationService.approveApplication(
                application.applicationNumber,
                null,
                null,
                ApproveApplicationRequestDTOFixture.ApproveApplicationRequestNoFiles
            )

            verifyOnce {
                licenceService.createLicence(
                    application,
                    ApproveApplicationRequestDTOFixture.ApproveApplicationRequestNoFiles,
                    mutableListOf()
                )
            }
            verifyOrder {
                applicationRepository.save(expectedApplication)
                awsSqsUtil.sendL1TUpdateStatusSqsMessage(any())
            }
        }

        @Test
        fun `should return success if approve application with valid files`() {
            val mockLicenceFile = mock(MultipartFile::class.java, "MockLicence.pdf")
            val mockAdditionalDocFile = mock(MultipartFile::class.java, "AdditionalDoc.pdf")
            val application = ApplicationFixture.createApplication_SingpassSelf.copy(
                reviewer = UserModelFixture.userA,
                status = ApplicationStatus.PROCESSING,
                caseStatus = ApplicationStatus.PROCESSING.getCaseStatus(UserModelFixture.userA)
            )
            val expectedApplication = ApplicationFixture.createApplication_SingpassSelf.copy(
                reviewer = UserModelFixture.userA,
                status = ApplicationStatus.PARTIALLY_APPROVED,
                caseStatus = ApplicationStatus.PARTIALLY_APPROVED.getCaseStatus(application.reviewer),
                activityType = ActivityTypeFixture.activityType.copy(
                    id = 4,
                    type = ActivityValue.APPROVE_APPLICATION
                )
            )
            val sampleLicenceResponseEntity = ResponseEntity(
                DDSUploadResponseDTO(LicenceDocumentType.LICENCE_DOC, "123LicenceDocID", "MockLicence.pdf"),
                HttpHeaders(),
                HttpStatus.OK
            )
            val sampleAddDocResponseEntity = ResponseEntity(
                DDSUploadResponseDTO(
                    LicenceDocumentType.ADDITIONAL_DOC,
                    "123LicenceDocID",
                    "AdditionalDoc.pdf"
                ),
                HttpHeaders(),
                HttpStatus.OK
            )
            every { applicationService.getApplication(application.applicationNumber) } returns application
            every { licenceService.existsByLicenceNumber(any()) } returns false
            every { licenceService.createLicence(any(), any(), any()) } just runs
            every { licenceRepository.save(any()) } returns LicenceModelFixture.licence
            every { applicationRepository.save(any()) } returns expectedApplication
            every {
                ddsIntegrationService.ddsUploadFiles(any(), any(), any())
            } returns sampleLicenceResponseEntity
            every {
                ddsIntegrationService.ddsUploadFiles(any(), any(), any())
            } returns sampleAddDocResponseEntity
            every { activityTypeService.getActivityType(any()) } returns ActivityTypeFixture.activityType.copy(
                id = 4,
                type = ActivityValue.APPROVE_APPLICATION
            )
            every { l1tIntegrationService.createL1TApplicationStatusRequest(any()) } returns mockk()
            every { awsSqsUtil.sendL1TUpdateStatusSqsMessage(any()) } returns mockk()

            applicationService.approveApplication(
                application.applicationNumber,
                mockLicenceFile,
                mockAdditionalDocFile,
                ApproveApplicationRequestDTOFixture.ApproveApplicationRequestWithFiles
            )
            verifyOrder {
                ddsIntegrationService.ddsUploadFiles(any(), any(), any())
                ddsIntegrationService.ddsUploadFiles(any(), any(), any())
            }
        }

        @Test
        fun `should throw ValidationException if application cannot be approved`() {
            val application = ApplicationFixture.createApplication_SingpassSelf.copy(
                reviewer = UserModelFixture.userA,
                status = ApplicationStatus.REJECTED,
                caseStatus = ApplicationStatus.REJECTED.getCaseStatus(UserModelFixture.userA)
            )
            every { authenticationFacade.getPrincipalName() } returns "supervisor@tech.gov.sg"
            every { userService.getUserByEmailAndIsDeletedFalse(any()) } returns UserModelFixture.userA
            every { applicationService.getApplication(application.applicationNumber) } returns application
            assertThrows<ValidationException> {
                applicationService.approveApplication(
                    application.applicationNumber,
                    null,
                    null,
                    ApproveApplicationRequestDTOFixture.ApproveApplicationRequestNoFiles
                )
            }.messageEqualTo("Application ${application.applicationNumber} cannot be approved")
        }
    }

    @Nested
    inner class ReassignApplication {
        @Test
        fun `should reassign application to User B (Agency Officer)`() {
            every { authenticationFacade.getPrincipalName() } returns "Atest"
            every { userService.getUserByEmailAndIsDeletedFalse(any()) } returns UserModelFixture.userA // loggedIn User
            every {
                UserModelFixture.userB.id?.let { userService.getUserById(it) }
            } returns UserModelFixture.userB // new Assignee

            every {
                applicationService.getApplication(any())
            } returns ApplicationFixture.createApplication_SingpassSelf.copy(
                status = ApplicationStatus.PROCESSING,
                reviewer = UserFixture.userA
            )
            every { applicationRepository.save(any()) } returns ApplicationFixture.createApplication_SingpassSelf.copy(
                reviewer = UserFixture.userB
            )
            applicationService.reassignApplication(
                ApplicationFixture.createApplication_SingpassSelf.applicationNumber,
                UserModelFixture.userB.id!!
            )

            verifyOrder { applicationRepository.save(any()) }
        }

        @Test
        fun `should throw ValidationException when Supervisor tries to assign his own Application to OfficerRO`() {
            every { authenticationFacade.getPrincipalName() } returns "Atest"
            every { userService.getUserByEmailAndIsDeletedFalse(any()) } returns UserModelFixture.userA // loggedIn User
            every {
                UserModelFixture.readOnlyUser.id?.let { userService.getUserById(it) }
            } returns UserModelFixture.readOnlyUser // new Assignee

            every {
                applicationService.getApplication(any())
            } returns ApplicationFixture.createApplication_SingpassSelf.copy(
                status = ApplicationStatus.PROCESSING,
                reviewer = UserFixture.userA
            )
            assertThrows<ValidationException> {
                applicationService.reassignApplication(
                    ApplicationFixture.createApplication_SingpassSelf.applicationNumber,
                    UserModelFixture.readOnlyUser.id!!
                )
            }.messageEqualTo("User 4 does not have necessary permissions to process the application")
        }

        @Test
        fun `should throw ValidationException when Supervisor tries to assign an Unclaimed Application`() {
            every { authenticationFacade.getPrincipalName() } returns "Atest"
            every { userService.getUserByEmailAndIsDeletedFalse(any()) } returns UserModelFixture.userA // loggedIn User
            every {
                UserModelFixture.userB.id?.let { userService.getUserById(it) }
            } returns UserModelFixture.userB // new Assignee

            every {
                applicationService.getApplication(any())
            } returns ApplicationFixture.createApplication_SingpassSelf.copy(
                status = ApplicationStatus.SUBMITTED,
                reviewer = null
            )
            assertThrows<ValidationException> {
                applicationService.reassignApplication(
                    ApplicationFixture.createApplication_SingpassSelf.applicationNumber,
                    UserModelFixture.userB.id!!
                )
            }.messageEqualTo("Application A1234567 cannot be reassigned")
        }

        @Test
        fun `should throw ValidationException when Supervisor reassigns an App to Officer|Supervisor of diff agency`() {
            every { authenticationFacade.getPrincipalName() } returns "Atest"
            every { userService.getUserByEmailAndIsDeletedFalse(any()) } returns UserModelFixture.userA // loggedIn User
            every {
                UserModelFixture.supervisorFromAgencyTwo.id?.let { userService.getUserById(it) }
            } returns UserModelFixture.supervisorFromAgencyTwo // new Assignee

            every {
                applicationService.getApplication(any())
            } returns ApplicationFixture.createApplication_SingpassSelf.copy(
                status = ApplicationStatus.PROCESSING,
                reviewer = UserModelFixture.userA
            )
            assertThrows<ValidationException> {
                applicationService.reassignApplication(
                    ApplicationFixture.createApplication_SingpassSelf.applicationNumber,
                    UserModelFixture.supervisorFromAgencyTwo.id!!
                )
            }.messageEqualTo("User 1 is not authorised to assign A1234567 to user 7 belonging to different agency")
        }

        @Test
        fun `should throw NotAuthorized Exception when Supervisor reassign an App of diff Agency`() {
            every { authenticationFacade.getPrincipalName() } returns "Ctest"
            every {
                userService.getUserByEmailAndIsDeletedFalse(any())
            } returns UserModelFixture.supervisorFromAgencyTwo // loggedIn User
            every {
                UserModelFixture.userA.id?.let { userService.getUserById(it) }
            } returns UserModelFixture.userA // new Assignee

            every {
                applicationService.getApplication(any())
            } returns ApplicationFixture.createApplication_SingpassSelf.copy(
                status = ApplicationStatus.PROCESSING,
                reviewer = UserModelFixture.userB
            )
            assertThrows<NotAuthorisedException> {
                applicationService.reassignApplication(
                    ApplicationFixture.createApplication_SingpassSelf.applicationNumber,
                    UserModelFixture.userA.id!!
                )
            }.messageEqualTo("User 7 is not authorised to access other agency application: A1234567")
        }

        @Test
        fun `should throw NotAuthorized Exception when Officer reassign an App which is not his`() {
            every { authenticationFacade.getPrincipalName() } returns "Btest"
            every { userService.getUserByEmailAndIsDeletedFalse(any()) } returns UserModelFixture.userB // loggedIn User
            every {
                UserModelFixture.officerB.id?.let { userService.getUserById(it) }
            } returns UserModelFixture.officerB // new Assignee

            every {
                applicationService.getApplication(any())
            } returns ApplicationFixture.createApplication_SingpassSelf.copy(
                status = ApplicationStatus.PROCESSING,
                reviewer = UserModelFixture.userA
            )
            assertThrows<ValidationException> {
                applicationService.reassignApplication(
                    ApplicationFixture.createApplication_SingpassSelf.applicationNumber,
                    UserModelFixture.officerB.id!!
                )
            }.messageEqualTo("Application A1234567 cannot be reassigned")
        }

        @Test
        fun `should throw NotFound Exception when Supervisor reassign an App to a deleted User`() {
            every { authenticationFacade.getPrincipalName() } returns "Supervisor"
            every { userService.getUserByEmailAndIsDeletedFalse(any()) } returns UserModelFixture.userA // loggedIn User
            every {
                UserModelFixture.deletedUser.id?.let { userService.getUserById(it) }
            } returns UserModelFixture.deletedUser // new Assignee

            every {
                applicationService.getApplication(any())
            } returns ApplicationFixture.createApplication_SingpassSelf.copy(
                status = ApplicationStatus.PROCESSING,
                reviewer = UserModelFixture.userB
            )
            assertThrows<NotFoundException> {
                applicationService.reassignApplication(
                    ApplicationFixture.createApplication_SingpassSelf.applicationNumber,
                    UserModelFixture.deletedUser.id!!
                )
            }.messageEqualTo("User 9 has been removed")
        }

        @Test
        fun `should throw ValidationException when Supervisor reassign an App to an Inactive User`() {
            every { authenticationFacade.getPrincipalName() } returns "Supervisor"
            every { userService.getUserByEmailAndIsDeletedFalse(any()) } returns UserModelFixture.userA // loggedIn User
            every {
                UserModelFixture.inActiveUser.id?.let { userService.getUserById(it) }
            } returns UserModelFixture.inActiveUser // new Assignee

            every {
                applicationService.getApplication(any())
            } returns ApplicationFixture.createApplication_SingpassSelf.copy(
                status = ApplicationStatus.PROCESSING,
                reviewer = UserModelFixture.userB
            )
            assertThrows<ValidationException> {
                applicationService.reassignApplication(
                    ApplicationFixture.createApplication_SingpassSelf.applicationNumber,
                    UserModelFixture.inActiveUser.id!!
                )
            }.messageEqualTo("User 8 is marked Inactive")
        }

        @Test
        fun `should throw Validation Exception when Supervisor reassign a Closed Application`() {
            every { authenticationFacade.getPrincipalName() } returns "Supervisor"
            every { userService.getUserByEmailAndIsDeletedFalse(any()) } returns UserModelFixture.userA // loggedIn User
            every {
                UserModelFixture.readOnlyUser.id?.let { userService.getUserById(it) }
            } returns UserModelFixture.userB // new Assignee

            every {
                applicationService.getApplication(any())
            } returns ApplicationFixture.createApplication_SingpassSelf.copy(
                status = ApplicationStatus.PROCESSING,
                reviewer = UserModelFixture.userA,
                caseStatus = ApplicationConstants.CASE_STATUS_CLOSED
            )
            assertThrows<ValidationException> {
                applicationService.reassignApplication(
                    ApplicationFixture.createApplication_SingpassSelf.applicationNumber,
                    UserModelFixture.userB.id!!
                )
            }.messageEqualTo("Application A1234567 cannot be reassigned")
        }
    }

    @Nested
    inner class ClarifyApplication {
        @Test
        fun `should update application for clarify operation`() {
            every {
                encryptService.encryptLicenceNode(any(), any(), true)
            } returns ApplicationFixture.createApplication_SingpassSelf.licenceDataFields
            every { applicationRepository.save(any()) } returns ApplicationFixture.createApplication_SingpassSelf
            every { activityTypeService.getActivityType(any()) } returns ActivityTypeFixture.activityType

            val application = applicationService.updateApplicationRFAResponded(
                CreateApplicationRequestDTOFixture.createApplicationRequest,
                ApplicationFixture.createApplication_SingpassSelf
            )

            assertEquals(
                CreateApplicationRequestDTOFixture.createApplicationRequest.application.general.applicationNumber,
                application.applicationNumber
            )
            assertEquals(ApplicationStatus.RFA_RESPONDED, application.status)
        }
    }

    @Nested
    inner class GetReassignableUsers {
        @Test
        fun `should return all the users belonging to Agency 1 with process_application authority`() {
            every { authenticationFacade.getPrincipalName() } returns "Atest"
            every { userService.getUserByEmailAndIsDeletedFalse(any()) } returns UserFixture.userA // loggedIn User
            every {
                applicationService.getApplication("A1234567")
            } returns ApplicationFixture.createApplication_SingpassSelf.copy(reviewer = UserFixture.userA)
            every {
                userRepository
                    .getUsersForReassignAsProjection(
                        UserFixture.userA.agencyId!!,
                        ApplicationConstants.PROCESS_APPLICATION,
                        UserFixture.userA.id!!
                    )
            } returns ReassignUsersModelFixture.usersFromAgencyTwo
            val application = applicationService.getApplication("A1234567")
            assertEquals(
                ApplicationFixture.createApplication_SingpassSelf.applicationNumber,
                application.applicationNumber
            )
            val userToExclude = application.reviewer?.id

            val users =
                userRepository
                    .getUsersForReassignAsProjection(
                        UserFixture.userA.agencyId!!,
                        ApplicationConstants.PROCESS_APPLICATION,
                        userToExclude!!
                    )
            assertEquals(2, users.size)
        }

        @Test
        fun `should give 0 Users belonging to Agency 2`() {
            every { authenticationFacade.getPrincipalName() } returns "diff agency"
            every {
                userService.getUserByEmailAndIsDeletedFalse(any())
            } returns UserFixture.userOfDifferentAgency // loggedIn User
            every {
                applicationService.getApplication("A1234567")
            } returns ApplicationFixture.createApplication_SingpassSelf
                .copy(reviewer = UserFixture.userOfDifferentAgency)
            val application = applicationService.getApplication("A1234567")
            every {
                userRepository.getUsersForReassignAsProjection(
                    UserFixture.userOfDifferentAgency.agencyId!!,
                    ApplicationConstants.PROCESS_APPLICATION,
                    application.reviewer!!.id!!
                )
            } returns emptyList()

            assertEquals(
                ApplicationFixture.createApplication_SingpassSelf.applicationNumber,
                application.applicationNumber
            )
            val users = userRepository.getUsersForReassignAsProjection(
                UserFixture.userOfDifferentAgency.agencyId!!,
                ApplicationConstants.PROCESS_APPLICATION,
                application.reviewer!!.id!!
            )
            assertEquals(0, users.size)
        }

        @Test
        fun `should err when withdrawing nonexistent applicaiton`() {
            every {
                applicationRepository.findByApplicationNumber(any())
            } returns null
            val dto = WithdrawApplicationDTO(
                "withdrawApplication",
                ApplicationDTO(
                    GeneralDTO(
                        ApplicationFixture.createApplication_SingpassSelf.applicationNumber,
                        "01/06/2022 18:19:25",
                        "Application for Temporary Change of Use Permit",
                        "23"
                    )
                )
            )
            val result = applicationService.withdrawApplication(dto)
            assertEquals("Business Logic Error", result.error?.status)
        }

        @Test
        fun `should err when withdrawing application in terminal state`() {
            every {
                applicationRepository.findByApplicationNumber(any())
            } returns ApplicationFixture.createApplication_SingpassSelf.copy(
                status = ApplicationStatus.APPROVED,
                reviewer = UserFixture.userA
            )
            val dto = WithdrawApplicationDTO(
                "withdrawApplication",
                ApplicationDTO(
                    GeneralDTO(
                        ApplicationFixture.createApplication_SingpassSelf.applicationNumber,
                        "01/06/2022 18:19:25",
                        "Application for Temporary Change of Use Permit",
                        "23"
                    )
                )
            )
            val result = applicationService.withdrawApplication(dto)
            assertEquals("Business Logic Error", result.error?.status)
        }

        @Test
        fun `should succeed withdrawing application`() {
            every {
                applicationRepository.findByApplicationNumber(any())
            } returns ApplicationFixture.createApplication_SingpassSelf.copy(
                status = ApplicationStatus.PROCESSING,
                reviewer = UserFixture.userA
            )
            every {
                applicationRepository.save(any())
            } returns ApplicationFixture.createApplication_SingpassSelf
            val dto = WithdrawApplicationDTO(
                "withdrawApplication",
                ApplicationDTO(
                    GeneralDTO(
                        ApplicationFixture.createApplication_SingpassSelf.applicationNumber,
                        "01/06/2022 18:19:25",
                        "Application for Temporary Change of Use Permit",
                        "23"
                    )
                )
            )
            val result = applicationService.withdrawApplication(dto)
            assertEquals("PENDING_WITHDRAWN", result.error?.status)
        }
    }

    @Nested
    inner class SoftDeleteApplicationRecord {
        @Test
        fun `should get application detail if application not soft deleted`() {
            every {
                applicationRepository.findByApplicationNumber(any())
            } returns ApplicationFixture.createApplication_SingpassSelf
            every {
                userService.getUserByEmailAndIsDeletedFalse(any())
            } returns UserModelFixture.userA
            every {
                applicationModelTransfer.toDTO(any())
            } returns ApplicationDetailDTOFixture.getApplication

            val applicationDetailDTO = applicationService.getApplicationDetails("A1234567")
            assertEquals(
                ApplicationDetailDTOFixture.getApplication.applicationNumber,
                applicationDetailDTO.applicationNumber
            )
        }

        @Test
        fun `should throw 404 not found if application is soft deleted`() {
            val mockApplicationSoftDeteled = ApplicationFixture.createApplication_SingpassSelf.copy(
                isDeleted = true
            )
            every {
                applicationRepository.findByApplicationNumber(any())
            } returns null
            assertThrows<NotFoundException> {
                applicationService.getApplication(mockApplicationSoftDeteled.applicationNumber)
            }.messageEqualTo(
                "Can't find the application with reference number: ${mockApplicationSoftDeteled.applicationNumber}"
            )
        }
    }

    @Nested
    inner class WithdrawApplication {
        @Test
        fun `Should return correct previous application status from application history`() {
            every {
                auditReader.createQuery().forRevisionsOfEntity(any(), true, false)
                    .add(any()).resultList
            } returns ApplicationFixture.applicationHistory
            val previousAppStatus = applicationService.getPreviousApplicationStatus(1)
            assertEquals(ApplicationStatus.PROCESSING, previousAppStatus)
        }

        @Test
        fun `Should return correct previous application status from application history with last 2 same statuses`() {
            every {
                auditReader.createQuery().forRevisionsOfEntity(any(), true, false)
                    .add(any()).resultList
            } returns ApplicationFixture.applicationHistorySameLatestStatus
            val previousAppStatus = applicationService.getPreviousApplicationStatus(1)
            assertEquals(ApplicationStatus.SUBMITTED, previousAppStatus)
        }

        @BeforeEach
        fun setup() {
            val authUser = "supervisor@tech.gov.sg"
            val user = UserModelFixture.userA.copy()

            val application = ApplicationFixture.withdrawApplication.copy(reviewer = user)
            every { applicationRepository.findByApplicationNumber(any()) } returns application
            every { authenticationFacade.getPrincipalName() } returns authUser
            every { userService.getUserByEmailAndIsDeletedFalse(authUser) } returns user

            every {
                auditReader.createQuery().forRevisionsOfEntity(any(), true, false)
                    .add(any()).resultList
            } returns ApplicationFixture.applicationHistory

            every { rfaService.getLatestApplicationRFA(any()) } returns RFAFixture.baseRFA.copy(
                status = RFAStatus.RFA_RESPONDED
            )
            every { activityTypeService.getActivityType(any()) } returns ActivityTypeFixture.activityType.copy(
                type = ActivityValue.APPROVE_APPLICATION_WITHDRAWAL
            )
            every { applicationRepository.save(any()) } returns application.copy()
            every { l1tIntegrationService.createL1TApplicationStatusRequest(any()) } returns mockk()
            every { awsSqsUtil.sendL1TUpdateStatusSqsMessage(any()) } returns mockk()
        }

        @AfterEach
        fun tearDown() {
            clearAllMocks()
        }

        @Test
        fun `Should throw NotFoundException if application does not exist`() {
            val invalidApplication = "winvalidWithdrawApp1234"
            every { applicationRepository.findByApplicationNumber(any()) } returns null

            assertThrows<NotFoundException> {
                applicationService.processApplicationWithdrawal(
                    invalidApplication,
                    WithdrawApplicationRequestDTO("Approve", null, null)
                )
            }.messageEqualTo("Can't find the application with reference number: $invalidApplication")
        }

        @Test
        fun `Should throw NotAuthorisedException if application does not belongs to the same agency as reviewer`() {
            val applicationNumber = "withdrawApp1234"
            every { applicationRepository.findByApplicationNumber(any()) } returns
                ApplicationFixture.withdrawApplication.copy(agency = AgencyFixture.agency.copy(id = 2))

            assertThrows<NotAuthorisedException> {
                applicationService.processApplicationWithdrawal(
                    applicationNumber,
                    WithdrawApplicationRequestDTO("Approve", null, null)
                )
            }.messageEqualTo(
                "User supervisor@tech.gov.sg is not authorised to access other agency application: $applicationNumber"
            )
        }

        @Test
        fun `Should throw ValidationException if application does not belong to the same reviewer`() {
            val applicationNumber = "withdrawApp1234"
            every { applicationRepository.findByApplicationNumber(any()) } returns
                ApplicationFixture.withdrawApplication.copy(reviewer = UserModelFixture.userB.copy()) // different user

            assertThrows<ValidationException> {
                applicationService.processApplicationWithdrawal(
                    applicationNumber,
                    WithdrawApplicationRequestDTO("Approve", null, null)
                )
            }.messageEqualTo("Application $applicationNumber already assigned to a different officer")
        }

        @Test
        fun `Should throw ValidationException if application cannot be withdrawn`() {
            val applicationNumber = "withdrawApp1234"
            every { applicationRepository.findByApplicationNumber(any()) } returns
                ApplicationFixture.withdrawApplication.copy(
                    reviewer = UserModelFixture.userA.copy(),
                    status = ApplicationStatus.WITHDRAWN // invalid status
                )

            assertThrows<ValidationException> {
                applicationService.processApplicationWithdrawal(
                    applicationNumber,
                    WithdrawApplicationRequestDTO("Approve", null, null)
                )
            }.messageEqualTo("Application $applicationNumber not withdrawable")
        }

        @Test
        fun `Should approve withdrawal of application given valid requirement`() {
            val finalAppStatus = applicationService.processApplicationWithdrawal(
                "withdrawableApp1234",
                WithdrawApplicationRequestDTO("Approve", null, null)
            )

            assertEquals(ApplicationStatus.WITHDRAWN.value, finalAppStatus.applicationStatus)
            verifyNever { rfaService.updateRFACancelled(any(), any()) }
            verifyOrder {
                rfaService.getLatestApplicationRFA(any())
                applicationRepository.save(any())
                awsSqsUtil.sendL1TUpdateStatusSqsMessage(any())
            }
        }

        @Test
        fun `Should approve withdrawal and update RFA Status if PAA`() {
            every { rfaService.getLatestApplicationRFA(any()) } returns RFAFixture.baseRFA.copy(
                status = RFAStatus.PENDING_APPLICANT_ACTION
            )
            every { rfaService.updateRFACancelled(any(), any()) } returns Unit
            every { activityTypeService.getActivityType(any()) } returns ActivityTypeFixture.activityType.copy(
                type = ActivityValue.APPROVE_APPLICATION
            )

            val finalAppStatus = applicationService.processApplicationWithdrawal(
                "withdrawableApp12345",
                WithdrawApplicationRequestDTO("Approve", null, null)
            )

            assertEquals(ApplicationStatus.WITHDRAWN.value, finalAppStatus.applicationStatus)
            verifyOrder {
                rfaService.getLatestApplicationRFA(any())
                rfaService.updateRFACancelled(any(), any())
                applicationRepository.save(any())
                awsSqsUtil.sendL1TUpdateStatusSqsMessage(any())
            }
        }

        @Test
        fun `Should reject withdrawal with Processing status given Pending Agency action`() {
            every {
                auditReader.createQuery().forRevisionsOfEntity(any(), true, false)
                    .add(any()).resultList
            } returns ApplicationFixture.applicationHistorySameLatestStatus
            every { activityTypeService.getActivityType(any()) } returns ActivityTypeFixture.activityType.copy(
                type = ActivityValue.REJECT_APPLICATION_WITHDRAWAL
            )

            val finalAppStatus = applicationService.processApplicationWithdrawal(
                "withdrawableApp1234",
                WithdrawApplicationRequestDTO("Reject", null, null)
            )

            assertEquals(ApplicationStatus.PROCESSING.value, finalAppStatus.applicationStatus)
            verifyNever {
                rfaService.getLatestApplicationRFA(any())
                rfaService.updateRFACancelled(any(), any())
            }
            verifyOrder {
                applicationRepository.save(any())
                awsSqsUtil.sendL1TUpdateStatusSqsMessage(any())
            }
        }

        @Test
        fun `Should reject withdrawal with previous application status`() {
            every {
                auditReader.createQuery().forRevisionsOfEntity(any(), true, false)
                    .add(any()).resultList
            } returns ApplicationFixture.applicationHistoryPAA
            every { activityTypeService.getActivityType(any()) } returns ActivityTypeFixture.activityType.copy(
                type = ActivityValue.REJECT_APPLICATION_WITHDRAWAL
            )

            val finalAppStatus = applicationService.processApplicationWithdrawal(
                "withdrawableApp1234",
                WithdrawApplicationRequestDTO("Reject", null, null)
            )

            assertEquals(ApplicationStatus.PENDING_APPLICANT_ACTION.value, finalAppStatus.applicationStatus)
            verifyNever {
                rfaService.getLatestApplicationRFA(any())
                rfaService.updateRFACancelled(any(), any())
            }
            verifyOrder {
                applicationRepository.save(any())
                awsSqsUtil.sendL1TUpdateStatusSqsMessage(any())
            }
        }
    }
}
