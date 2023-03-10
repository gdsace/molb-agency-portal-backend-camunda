package sg.gov.tech.molbagencyportalbackend.service

import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import sg.gov.tech.molbagencyportalbackend.auth.AuthenticationFacade
import sg.gov.tech.molbagencyportalbackend.dto.AgencyLicenceDetailRequestParams
import sg.gov.tech.molbagencyportalbackend.dto.AgencyLicenceDocumentsRequestParams
import sg.gov.tech.molbagencyportalbackend.dto.LicenceDetailDTO
import sg.gov.tech.molbagencyportalbackend.dto.dds.DDSUploadResponseDTO
import sg.gov.tech.molbagencyportalbackend.dto.internal.AgencyLicencesRequestParams
import sg.gov.tech.molbagencyportalbackend.dto.internal.ApproveApplicationRequestDTO
import sg.gov.tech.molbagencyportalbackend.dto.internal.DashboardLicenceTransfer
import sg.gov.tech.molbagencyportalbackend.dto.internal.LicenceModelTransfer
import sg.gov.tech.molbagencyportalbackend.dto.internal.LicenceStatisticsDTO
import sg.gov.tech.molbagencyportalbackend.exception.NotAuthorisedException
import sg.gov.tech.molbagencyportalbackend.exception.NotFoundException
import sg.gov.tech.molbagencyportalbackend.integration.job.L1TUpdateStatusPayload
import sg.gov.tech.molbagencyportalbackend.model.Application
import sg.gov.tech.molbagencyportalbackend.model.Licence
import sg.gov.tech.molbagencyportalbackend.model.LicenceStatus
import sg.gov.tech.molbagencyportalbackend.model.User
import sg.gov.tech.molbagencyportalbackend.repository.LicenceRepository
import sg.gov.tech.molbagencyportalbackend.util.DateUtil
import sg.gov.tech.molbagencyportalbackend.util.aws.AwsSqsUtil
import java.time.LocalDate

@Service
class LicenceService(
    private val licenceRepository: LicenceRepository,
    private val dashboardLicenceTransfer: DashboardLicenceTransfer,
    private val licenceModelTransfer: LicenceModelTransfer,
    private val userService: UserService,
    private val ddsIntegrationService: DDSIntegrationService,
    private val l1tIntegrationService: L1TIntegrationService,
    private val awsSqsUtil: AwsSqsUtil,
    private val authenticationFacade: AuthenticationFacade
) {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    fun createLicence(
        application: Application,
        requestBody: ApproveApplicationRequestDTO,
        dashboardDocumentServiceResponse: List<DDSUploadResponseDTO>
    ) {
        val licence = Licence(
            application = application,
            licenceNumber = requestBody.licenceNumber,
            licenceName = application.licenceName,
            loginType = application.loginType,
            uen = application.company.uen,
            nric = application.applicant.id.idNumber ?: "",
            licenceType = application.licenceType,
            licenceDocuments = dashboardDocumentServiceResponse.ifEmpty { null },
            issueDate = DateUtil.getLocalDateFormat(requestBody.issueDate, DateUtil.DATEFORMAT_DATE),
            startDate = DateUtil.getLocalDateFormat(requestBody.startDate, DateUtil.DATEFORMAT_DATE),
            expiryDate = requestBody.expiryDate?.let {
                DateUtil.getLocalDateFormat(it, DateUtil.DATEFORMAT_DATE)
            },
            dueForRenewal = false
        ).setInitialStatus().setLicenceIssuanceType(requestBody.licenceIssuanceType)
        licenceRepository.save(licence)
    }

    fun getLicence(licenceNumber: String): Licence {
        return licenceRepository.findByLicenceNumber(licenceNumber)
            ?: throw NotFoundException("Can't find the licence with licence number: $licenceNumber")
    }

    fun getLicenceDetails(licenceNumber: AgencyLicenceDetailRequestParams): LicenceDetailDTO {
        getLicence(licenceNumber.licenceNumber).let {
            val showApplications =
                (
                    userService.getUserByEmailAndIsDeletedFalse(authenticationFacade.getPrincipalName())
                        ?.agencyId == it.licenceType.agency.id
                    )
            return licenceModelTransfer.toDetailDTO(it, showApplications)
        }
    }

    fun existsByLicenceNumber(licenceNumber: String): Boolean =
        licenceRepository.existsByLicenceNumber(licenceNumber)

    fun getByApplicationId(applicationId: Long): Licence? =
        licenceRepository.findByApplicationId(applicationId)

    fun getDashboardLicences(requestParams: AgencyLicencesRequestParams): LicenceStatisticsDTO {
        val user: User? =
            userService.getUserByEmailAndIsDeletedFalse(authenticationFacade.getPrincipalName())
        val agencyId: Long = user?.agencyId!!

        val agencyLicencesCount =
            licenceRepository.countLicenceByApplicationAgencyId(
                agencyId
            )
        logger.debug("Agency: $agencyId -- agencyLicencesCount: $agencyLicencesCount")

        val licencesCount = licenceRepository.countLicenceByApplicationAgencyIdNot(
            agencyId
        )
        logger.debug("Other Agencies Licences Count: $licencesCount")

        val dashboardLicences = if (requestParams.tab.equals("otherLicences")) {
            licenceRepository.findLicenceByApplicationAgencyIdNot(
                agencyId,
                PageRequest.of(
                    requestParams.page,
                    requestParams.limit,
                    Sort.by(
                        Sort.Order(
                            Sort.Direction.valueOf(requestParams.sortOrder.uppercase()),
                            requestParams.sortField
                        ).ignoreCase()
                    )
                )
            )
        } else {
            licenceRepository.findLicenceByApplicationAgencyId(
                agencyId,
                PageRequest.of(
                    requestParams.page,
                    requestParams.limit,
                    Sort.by(
                        Sort.Order(
                            Sort.Direction.valueOf(requestParams.sortOrder.uppercase()),
                            requestParams.sortField
                        ).ignoreCase()
                    )
                )
            )
        }
        return LicenceStatisticsDTO(
            agencyLicencesCount,
            licencesCount,
            dashboardLicences.content.map { dashboardLicenceTransfer.toDTO(it) }
        )
    }

    fun getLicenceDocument(requestParams: AgencyLicenceDocumentsRequestParams): ResponseEntity<ByteArray> {
        val licence = getLicence(requestParams.licenceNumber)
        val agencyId: Long =
            userService.getUserByEmailAndIsDeletedFalse(authenticationFacade.getPrincipalName())?.agencyId!!
        if (licence.application?.agency?.id != agencyId)
            throw NotAuthorisedException(
                "User is not authorised to download file(s) for other agency licence: ${requestParams.licenceNumber}"
            )

        return ddsIntegrationService.ddsDownloadFile(licence, requestParams.documentId)
    }

    fun updateActiveLicence(): LicenceStatusUpdateJobCounter {
        val licencesToActivate =
            licenceRepository.findAllByStartDateIsLessThanEqualAndStatus(
                LocalDate.now(),
                LicenceStatus.INACTIVE
            )
        val licenceStatusUpdateCounter = LicenceStatusUpdateJobCounter(
            processCount = licencesToActivate?.size ?: 0,
            successCount = 0,
            errorCount = 0
        )

        licencesToActivate?.map {
            updateLicenceStatus(it, LicenceStatus.ACTIVE, licenceStatusUpdateCounter)
        }
        logger.info(
            "Total Licences Processed To Active: ${licenceStatusUpdateCounter.processCount}; " +
                "Success: ${licenceStatusUpdateCounter.successCount}; " +
                "Error: ${licenceStatusUpdateCounter.errorCount}"
        )

        return licenceStatusUpdateCounter
    }

    fun updateExpiredLicence(): LicenceStatusUpdateJobCounter {
        val licencesToExpire =
            licenceRepository.findAllByExpiryDateIsLessThanAndStatus(
                LocalDate.now(),
                LicenceStatus.ACTIVE
            )
        val licenceStatusUpdateCounter = LicenceStatusUpdateJobCounter(
            processCount = licencesToExpire?.size ?: 0,
            successCount = 0,
            errorCount = 0
        )

        licencesToExpire?.map {
            updateLicenceStatus(it, LicenceStatus.EXPIRED, licenceStatusUpdateCounter)
        }
        logger.info(
            "Total Licences Processed To Expire: ${licenceStatusUpdateCounter.processCount}; " +
                "Success: ${licenceStatusUpdateCounter.successCount}; " +
                "Error: ${licenceStatusUpdateCounter.errorCount}"
        )

        return licenceStatusUpdateCounter
    }

    fun updateLicenceStatus(
        licence: Licence,
        licenceStatus: LicenceStatus,
        licenceStatusUpdateJobCounter: LicenceStatusUpdateJobCounter
    ) {
        try {
            val updatedLicence = licence.copy(status = licenceStatus)

            sendL1TUpdateStatusRequest(updatedLicence)
            licenceRepository.save(updatedLicence)

            logger.info("Updated Licence ${updatedLicence.licenceNumber} to ${licenceStatus.value}")
            licenceStatusUpdateJobCounter.successCount++
        } catch (e: Exception) {
            licenceStatusUpdateJobCounter.errorList.add(licence.licenceNumber)
            logger.error("${licence.licenceNumber}: " + e.localizedMessage)
            logger.error(e.stackTraceToString())
            licenceStatusUpdateJobCounter.errorCount++
        }
    }

    private fun sendL1TUpdateStatusRequest(licence: Licence) {
        val l1tUpdateLicenceStatusRequest =
            l1tIntegrationService.createL1TLicenceStatusRequest(licence)
        val l1tUpdateStatusPayload = L1TUpdateStatusPayload(
            referenceNumber = licence.licenceNumber,
            l1tStatusPushRequest = l1tUpdateLicenceStatusRequest
        )
        awsSqsUtil.sendL1TUpdateStatusSqsMessage(l1tUpdateStatusPayload)
        logger.info("Update Status to L1T queued for ${l1tUpdateStatusPayload.referenceNumber}")
    }
}

data class LicenceStatusUpdateJobCounter(
    val processCount: Int,
    var successCount: Int,
    var errorCount: Int,
    val errorList: MutableList<String> = mutableListOf()
)

data class LicenceStatusUpdateJobStatistics(
    val active: LicenceStatusUpdateJobCounter,
    val expired: LicenceStatusUpdateJobCounter
)
