package sg.gov.tech.molbagencyportalbackend.service

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verifyOrder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import sg.gov.tech.molbagencyportalbackend.auth.AuthenticationFacade
import sg.gov.tech.molbagencyportalbackend.dto.AgencyLicenceDetailRequestParams
import sg.gov.tech.molbagencyportalbackend.dto.AgencyLicenceDocumentsRequestParams
import sg.gov.tech.molbagencyportalbackend.dto.internal.AgencyLicencesRequestParams
import sg.gov.tech.molbagencyportalbackend.dto.internal.DashboardLicenceTransfer
import sg.gov.tech.molbagencyportalbackend.dto.internal.LicenceModelTransfer
import sg.gov.tech.molbagencyportalbackend.exception.ExceptionControllerAdvice
import sg.gov.tech.molbagencyportalbackend.exception.FileDownloadException
import sg.gov.tech.molbagencyportalbackend.exception.NotAuthorisedException
import sg.gov.tech.molbagencyportalbackend.exception.NotFoundException
import sg.gov.tech.molbagencyportalbackend.fixture.ApplicationFixture
import sg.gov.tech.molbagencyportalbackend.fixture.ApproveApplicationRequestDTOFixture
import sg.gov.tech.molbagencyportalbackend.fixture.LicenceDetailDTOFixture
import sg.gov.tech.molbagencyportalbackend.fixture.LicenceModelFixture
import sg.gov.tech.molbagencyportalbackend.fixture.LicenceTypeFixture
import sg.gov.tech.molbagencyportalbackend.fixture.UserModelFixture
import sg.gov.tech.molbagencyportalbackend.model.Licence
import sg.gov.tech.molbagencyportalbackend.repository.LicenceRepository
import sg.gov.tech.molbagencyportalbackend.util.aws.AwsSqsUtil
import sg.gov.tech.testing.MolbUnitTesting
import sg.gov.tech.testing.messageEqualTo
import sg.gov.tech.testing.verifyOnce

@MolbUnitTesting
internal class LicenceServiceTest {
    @MockK
    private lateinit var licenceRepository: LicenceRepository

    @MockK
    private lateinit var licenceTypeService: LicenceTypeService

    @MockK
    private lateinit var licenceModelTransfer: LicenceModelTransfer

    @MockK
    private lateinit var userService: UserService

    @MockK
    private lateinit var ddsIntegrationService: DDSIntegrationService

    @MockK
    private lateinit var l1tIntegrationService: L1TIntegrationService

    @MockK
    private lateinit var awsSqsUtil: AwsSqsUtil

    @InjectMockKs
    private lateinit var licenceService: LicenceService

    @MockK
    private lateinit var dashboardLicenceTransfer: DashboardLicenceTransfer

    @MockK
    private lateinit var authenticationFacade: AuthenticationFacade

    private val pageRequest = PageRequest.of(0, 2)

    private val licencesPages = mockk<Page<Licence>> {
        every { content } returns LicenceModelFixture.licenceList
        every { pageable } returns pageRequest
        every { totalElements } returns 2
    }

    @Test
    fun `should return true if licence number exists`() {
        every { licenceRepository.existsByLicenceNumber(LicenceModelFixture.licence.licenceNumber) } returns true
        assertTrue(licenceService.existsByLicenceNumber(LicenceModelFixture.licence.licenceNumber))
    }

    @Test
    fun `should return false if licence number does not exists`() {
        every { licenceService.existsByLicenceNumber(any()) } returns false
        assertFalse(licenceService.existsByLicenceNumber("invalid licence number"))
    }

    @Test
    fun `should create a licence for approval`() {
        every { licenceRepository.save(any()) } returns LicenceModelFixture.licence
        every { licenceTypeService.findByLicenceId(any()) } returns LicenceTypeFixture.licenceType
        licenceService.createLicence(
            ApplicationFixture.createApplication_SingpassSelf,
            ApproveApplicationRequestDTOFixture.ApproveApplicationRequestNoFiles,
            mutableListOf()
        )
        verifyOnce {
            licenceRepository.save(any())
        }
    }

    @Test
    fun `Should return all agency licences`() {
        every { authenticationFacade.getPrincipalName() } returns "supervisor@tech.gov.sg"
        every {
            userService.getUserByEmailAndIsDeletedFalse(any())
        } returns UserModelFixture.userA
        every {
            licenceRepository.countLicenceByApplicationAgencyIdNot(any())
        } returns LicenceModelFixture.licenceList.size

        every {
            licenceRepository.countLicenceByApplicationAgencyId(any())
        } returns LicenceModelFixture.licenceList.size

        every {
            licenceRepository.findLicenceByApplicationAgencyIdNot(any(), any())
        } returns licencesPages

        every { dashboardLicenceTransfer.toDTO(any()) } answers { callOriginal() }

        val agencyLicences = licenceService.getDashboardLicences(
            AgencyLicencesRequestParams(
                "otherLicences",
                0,
                "licenceNumber",
                "asc",
                10
            )
        )

        assertEquals(2, agencyLicences.licenceCount)
        assertEquals(2, agencyLicences.agencyLicenceCount)
        assertEquals(1, licencesPages.content[0].id)
        assertEquals(2, licencesPages.content[1].id)
    }

    @Test
    fun `Should return other agency licences`() {
        every {
            licenceRepository.countLicenceByApplicationAgencyIdNot(any())
        } returns LicenceModelFixture.licenceList.size

        every {
            licenceRepository.countLicenceByApplicationAgencyId(any())
        } returns LicenceModelFixture.licenceList.size

        every {
            licenceRepository.findLicenceByApplicationAgencyIdNot(any(), any())
        } returns licencesPages

        every { dashboardLicenceTransfer.toDTO(any()) } answers { callOriginal() }

        val agencyLicences = licenceService.getDashboardLicences(
            AgencyLicencesRequestParams(
                "otherLicences",
                0,
                "licenceNumber",
                "asc",
                10
            )
        )

        assertEquals(2, agencyLicences.licenceCount)
        assertEquals(2, agencyLicences.agencyLicenceCount)
        assertEquals(1, licencesPages.content[0].id)
        assertEquals(2, licencesPages.content[1].id)
    }

    @Test
    fun `Should return agency licences`() {
        every { authenticationFacade.getPrincipalName() } returns "supervisor@tech.gov.sg"
        every {
            userService.getUserByEmailAndIsDeletedFalse(any())
        } returns UserModelFixture.userA
        every {
            licenceRepository.countLicenceByApplicationAgencyIdNot(any())
        } returns LicenceModelFixture.licenceList.size

        every {
            licenceRepository.countLicenceByApplicationAgencyId(any())
        } returns LicenceModelFixture.licenceList.size

        every {
            licenceRepository.findLicenceByApplicationAgencyId(any(), any())
        } returns licencesPages

        every { dashboardLicenceTransfer.toDTO(any()) } answers { callOriginal() }

        val agencyLicences = licenceService.getDashboardLicences(
            AgencyLicencesRequestParams(
                "licences",
                0,
                "licenceNumber",
                "asc",
                10
            )
        )

        assertEquals(2, agencyLicences.licenceCount)
        assertEquals(2, agencyLicences.agencyLicenceCount)
        assertEquals(1, licencesPages.content[0].id)
        assertEquals(2, licencesPages.content[1].id)
    }

    @Test
    fun `should retrieve licence if licence exists`() {
        val licenceNumber = LicenceModelFixture.licence.licenceNumber
        every { licenceRepository.findByLicenceNumber(any()) } returns LicenceModelFixture.licence

        val retrievedLicence = licenceService.getLicence(licenceNumber)

        assertEquals(retrievedLicence, LicenceModelFixture.licence)
    }

    @Test
    fun `should throw NotFoundException if the licence does not exists`() {
        val licenceNumber = LicenceModelFixture.licence.licenceNumber
        every { licenceRepository.findByLicenceNumber(any()) } returns null

        assertThrows<NotFoundException> {
            licenceService.getLicence(licenceNumber)
        }.messageEqualTo("Can't find the licence with licence number: $licenceNumber")
    }

    @Test
    fun `should retrieve licence details if licence exists`() {
        val licenceNumber = LicenceModelFixture.licence.licenceNumber
        every {
            userService.getUserByEmailAndIsDeletedFalse(any())
        } returns UserModelFixture.userA
        every { licenceRepository.findByLicenceNumber(any()) } returns LicenceModelFixture.licence
        every {
            licenceModelTransfer.toDetailDTO(
                any(),
                true
            )
        } returns LicenceDetailDTOFixture.getLicence

        val retrievedLicenceDetails =
            licenceService.getLicenceDetails(AgencyLicenceDetailRequestParams(licenceNumber))

        assertEquals(retrievedLicenceDetails, LicenceDetailDTOFixture.getLicence)
    }

    @Test
    fun `should retrieve licence details without application info if application belong to different agency`() {
        every { authenticationFacade.getPrincipalName() } returns "supervisor@tech.gov.sg"

        val licenceNumber = LicenceModelFixture.licence.licenceNumber
        every {
            userService.getUserByEmailAndIsDeletedFalse(any())
        } returns UserModelFixture.supervisorFromAgencyTwo
        every { licenceRepository.findByLicenceNumber(any()) } returns LicenceModelFixture.licence
        every {
            licenceModelTransfer.toDetailDTO(any(), false)
        } returns LicenceDetailDTOFixture.getLicenceWoApplication

        val retrievedLicenceDetails =
            licenceService.getLicenceDetails(AgencyLicenceDetailRequestParams(licenceNumber))

        assertEquals(retrievedLicenceDetails, LicenceDetailDTOFixture.getLicenceWoApplication)
    }

    @Test
    fun `should return NotFound Exception if licence does not exist`() {
        every {
            licenceRepository.findByLicenceNumber(any())
        } returns null
        assertThrows<NotFoundException> {
            licenceService.getLicence(LicenceModelFixture.licence.licenceNumber)
        }.messageEqualTo(
            "Can't find the licence with licence number: ${LicenceModelFixture.licence.licenceNumber}"
        )
    }

    @Test
    fun `should return licence when licence with licence number is found`() {
        every {
            licenceRepository.findByLicenceNumber(any())
        } returns LicenceModelFixture.licence
        licenceService.getLicence("123test")
        assertEquals("123test", LicenceModelFixture.licence.licenceNumber)
    }

    @Test
    fun `should return file when valid params are passed`() {
        val sampleLicenceNumber = LicenceModelFixture.licence.licenceNumber
        val sampleDocumentId = "65276d6a-2ee5-4bd6-bc57-eaa37aa5f54a"
        val sampleResponseEntity = ResponseEntity(
            "sampleFile".toByteArray(),
            HttpHeaders(),
            HttpStatus.OK
        )
        every { licenceService.getLicence(sampleLicenceNumber) } returns LicenceModelFixture.licence
        every {
            ddsIntegrationService.ddsDownloadFile(
                LicenceModelFixture.licence,
                sampleDocumentId
            )
        } returns sampleResponseEntity

        val response = licenceService.getLicenceDocument(
            AgencyLicenceDocumentsRequestParams(sampleLicenceNumber, sampleDocumentId)
        )
        assertEquals(response, sampleResponseEntity)
    }

    @Test
    fun `should not return file when agency id is different`() {
        val sampleLicenceNumber = LicenceModelFixture.licence.licenceNumber
        val sampleDocumentId = "65276d6a-2ee5-4bd6-bc57-eaa37aa5f54a"
        val sampleResponseEntity = ResponseEntity(
            "sampleFile".toByteArray(),
            HttpHeaders(),
            HttpStatus.OK
        )
        every { licenceService.getLicence(sampleLicenceNumber) } returns LicenceModelFixture.licence
        every { authenticationFacade.getPrincipalName() } returns "supervisor@tech.gov.sg"
        every {
            userService.getUserByEmailAndIsDeletedFalse(any())
        } returns UserModelFixture.supervisorFromAgencyTwo
        every {
            ddsIntegrationService.ddsDownloadFile(
                LicenceModelFixture.licence,
                sampleDocumentId
            )
        } returns sampleResponseEntity
        assertThrows<NotAuthorisedException> {
            licenceService.getLicenceDocument(
                AgencyLicenceDocumentsRequestParams(sampleLicenceNumber, sampleDocumentId)
            )
        }.messageEqualTo("User is not authorised to download file(s) for other agency licence: $sampleLicenceNumber")
    }

    @Test
    fun `should return FileDownloadException when unable to download file`() {
        val sampleLicenceNumber = LicenceModelFixture.licence.licenceNumber
        val sampleDocumentId = "65276d6a-2ee5-4bd6-bc57-eaa37aa5f54a"
        every { licenceService.getLicence(sampleLicenceNumber) } returns LicenceModelFixture.licence
        every {
            userService.getUserByEmailAndIsDeletedFalse(any())
        } returns UserModelFixture.userA
        every {
            ddsIntegrationService.ddsDownloadFile(
                LicenceModelFixture.licence,
                sampleDocumentId
            )
        } throws FileDownloadException(ExceptionControllerAdvice.FILE_DOWNLOAD_ERROR_MESSAGE)

        assertThrows<FileDownloadException> {
            licenceService.getLicenceDocument(
                AgencyLicenceDocumentsRequestParams(sampleLicenceNumber, sampleDocumentId)
            )
        }.messageEqualTo(ExceptionControllerAdvice.FILE_DOWNLOAD_ERROR_MESSAGE)
    }

    @Nested
    inner class L1TLicenceStatusUpdateJob {
        @Test
        fun `should return 2 success and 0 errors when updating licence to active`() {
            every {
                licenceRepository.findAllByStartDateIsLessThanEqualAndStatus(any(), any())
            } returns LicenceModelFixture.licenceList
            every { licenceRepository.save(any()) } returns mockk()
            every { l1tIntegrationService.createL1TLicenceStatusRequest(any()) } returns mockk()
            every { awsSqsUtil.sendL1TUpdateStatusSqsMessage(any()) } returns mockk()

            val licenceStatusUpdateCounter = licenceService.updateActiveLicence()

            assertEquals(2, licenceStatusUpdateCounter.processCount)
            assertEquals(2, licenceStatusUpdateCounter.successCount)
            assertEquals(0, licenceStatusUpdateCounter.errorCount)
            assertEquals(0, licenceStatusUpdateCounter.errorList.size)
            verifyOrder {
                awsSqsUtil.sendL1TUpdateStatusSqsMessage(any())
                licenceRepository.save(any())
            }
        }

        @Test
        fun `should return 2 success and 0 errors when updating licence to expired`() {
            every {
                licenceRepository.findAllByExpiryDateIsLessThanAndStatus(any(), any())
            } returns LicenceModelFixture.licenceListToExpired
            every { licenceRepository.save(any()) } returns mockk()
            every { l1tIntegrationService.createL1TLicenceStatusRequest(any()) } returns mockk()
            every { awsSqsUtil.sendL1TUpdateStatusSqsMessage(any()) } returns mockk()

            val licenceStatusUpdateCounter = licenceService.updateExpiredLicence()

            assertEquals(2, licenceStatusUpdateCounter.processCount)
            assertEquals(2, licenceStatusUpdateCounter.successCount)
            assertEquals(0, licenceStatusUpdateCounter.errorCount)
            assertEquals(0, licenceStatusUpdateCounter.errorList.size)
            verifyOrder {
                awsSqsUtil.sendL1TUpdateStatusSqsMessage(any())
                licenceRepository.save(any())
            }
        }
    }

    @Test
    fun `mask pii for getDashboardLicences`() {
        every { authenticationFacade.getPrincipalName() } returns "supervisor@tech.gov.sg"
        every {
            userService.getUserByEmailAndIsDeletedFalse(any())
        } returns UserModelFixture.userA
        every {
            licenceRepository.countLicenceByApplicationAgencyIdNot(any())
        } returns LicenceModelFixture.licenceList.size

        every {
            licenceRepository.countLicenceByApplicationAgencyId(any())
        } returns LicenceModelFixture.licenceList.size

        every {
            licenceRepository.findLicenceByApplicationAgencyId(any(), any())
        } returns licencesPages

        every { dashboardLicenceTransfer.toDTO(any()) } answers { callOriginal() }

        val agencyLicences = licenceService.getDashboardLicences(
            AgencyLicencesRequestParams(
                "licences",
                0,
                "licenceNumber",
                "asc",
                10
            )
        )

        assertEquals(2, agencyLicences.licenceCount)
        assertEquals(2, agencyLicences.agencyLicenceCount)
        assertEquals("NRIC: *****XXXXXA", agencyLicences.licences[0].licenceHolderId)
    }

    @Test
    fun `mask pii for getLicenceDetails`() {
        every { authenticationFacade.getPrincipalName() } returns "supervisor@tech.gov.sg"
        val licenceNumber = LicenceModelFixture.licence.licenceNumber
        every {
            userService.getUserByEmailAndIsDeletedFalse(any())
        } returns UserModelFixture.userA
        every { licenceRepository.findByLicenceNumber(any()) } returns LicenceModelFixture.licence
        every {
            licenceModelTransfer.toDetailDTO(
                LicenceModelFixture.licence,
                true
            )
        } answers { callOriginal() }
        with(licenceModelTransfer) {
            every { any<Licence>().toLicenceDTO(true) } answers { callOriginal() }
        }
        val retrievedLicenceDetails =
            licenceService.getLicenceDetails(AgencyLicenceDetailRequestParams(licenceNumber))

        assertEquals("*****XXXXXA", retrievedLicenceDetails.applicant?.id?.idNumber)
    }

    @Test
    fun `should return licence detail if the licence is not soft deleted`() {
        val licenceNumber = LicenceModelFixture.licence.licenceNumber
        every {
            userService.getUserByEmailAndIsDeletedFalse(any())
        } returns UserModelFixture.userA
        every { licenceRepository.findByLicenceNumber(any()) } returns LicenceModelFixture.licence
        every {
            licenceModelTransfer.toDetailDTO(
                any(),
                true
            )
        } returns LicenceDetailDTOFixture.getLicence

        val retrievedLicenceDetails =
            licenceService.getLicenceDetails(AgencyLicenceDetailRequestParams(licenceNumber))

        assertEquals(retrievedLicenceDetails, LicenceDetailDTOFixture.getLicence)
    }

    @Test
    fun `should return 404 not found if the licence record is deleted`() {
        val licenceSoftDeleted = LicenceModelFixture.licence.copy(
            isDeleted = true
        )
        every { authenticationFacade.getPrincipalName() } returns "supervisor@tech.gov.sg"
        every { licenceRepository.findByLicenceNumber(licenceSoftDeleted.licenceNumber) } returns null
        assertThrows<NotFoundException> {
            licenceService.getLicence(licenceSoftDeleted.licenceNumber)
        }.messageEqualTo("Can't find the licence with licence number: ${licenceSoftDeleted.licenceNumber}")
    }
}
