package sg.gov.tech.molbagencyportalbackend.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.convertValue
import org.hibernate.envers.AuditReader
import org.hibernate.envers.query.AuditEntity
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import sg.gov.tech.molbagencyportalbackend.auth.AuthenticationFacade
import sg.gov.tech.molbagencyportalbackend.dto.ApplicationDetailDTO
import sg.gov.tech.molbagencyportalbackend.dto.CancelRFARequestDTO
import sg.gov.tech.molbagencyportalbackend.dto.SendRFADTO
import sg.gov.tech.molbagencyportalbackend.dto.dds.DDSUploadResponseDTO
import sg.gov.tech.molbagencyportalbackend.dto.dds.LicenceDocumentType
import sg.gov.tech.molbagencyportalbackend.dto.internal.AgencyApplicationTransfer
import sg.gov.tech.molbagencyportalbackend.dto.internal.AgencyApplicationsRequestParams
import sg.gov.tech.molbagencyportalbackend.dto.internal.ApplicationModelTransfer
import sg.gov.tech.molbagencyportalbackend.dto.internal.ApplicationsDTO
import sg.gov.tech.molbagencyportalbackend.dto.internal.ApproveApplicationRequestDTO
import sg.gov.tech.molbagencyportalbackend.dto.internal.DashboardApplicationRequestParams
import sg.gov.tech.molbagencyportalbackend.dto.internal.DashboardApplicationTransfer
import sg.gov.tech.molbagencyportalbackend.dto.internal.DashboardStatisticsDTO
import sg.gov.tech.molbagencyportalbackend.dto.internal.DocumentRequestParams
import sg.gov.tech.molbagencyportalbackend.dto.internal.RejectMessagesDTO
import sg.gov.tech.molbagencyportalbackend.dto.internal.WithdrawApplicationRequestDTO
import sg.gov.tech.molbagencyportalbackend.dto.internal.WithdrawApplicationResponseDTO
import sg.gov.tech.molbagencyportalbackend.dto.internal.user.ReassignUserDTOProjection
import sg.gov.tech.molbagencyportalbackend.dto.l1t.ApplicationDTO
import sg.gov.tech.molbagencyportalbackend.dto.l1t.CreateApplicationRequestDTO
import sg.gov.tech.molbagencyportalbackend.dto.l1t.L1TDocument
import sg.gov.tech.molbagencyportalbackend.dto.l1t.L1TErrorDTO
import sg.gov.tech.molbagencyportalbackend.dto.l1t.L1TMultipleDocumentRequest
import sg.gov.tech.molbagencyportalbackend.dto.l1t.L1TResponseDTO
import sg.gov.tech.molbagencyportalbackend.dto.l1t.L1TResponseDTOTransfer
import sg.gov.tech.molbagencyportalbackend.dto.l1t.L1TResultDTO
import sg.gov.tech.molbagencyportalbackend.dto.l1t.L1TSingleDocumentRequest
import sg.gov.tech.molbagencyportalbackend.dto.l1t.WithdrawApplicationDTO
import sg.gov.tech.molbagencyportalbackend.exception.ExceptionControllerAdvice
import sg.gov.tech.molbagencyportalbackend.exception.FileDownloadException
import sg.gov.tech.molbagencyportalbackend.exception.InternalConfigException
import sg.gov.tech.molbagencyportalbackend.exception.NotAuthorisedException
import sg.gov.tech.molbagencyportalbackend.exception.NotFoundException
import sg.gov.tech.molbagencyportalbackend.exception.ValidationException
import sg.gov.tech.molbagencyportalbackend.integration.job.L1TUpdateStatusPayload
import sg.gov.tech.molbagencyportalbackend.model.ActivityValue
import sg.gov.tech.molbagencyportalbackend.model.Application
import sg.gov.tech.molbagencyportalbackend.model.ApplicationStatus
import sg.gov.tech.molbagencyportalbackend.model.ApplyAs
import sg.gov.tech.molbagencyportalbackend.model.LicenceIssuanceType
import sg.gov.tech.molbagencyportalbackend.model.RFAStatus
import sg.gov.tech.molbagencyportalbackend.model.User
import sg.gov.tech.molbagencyportalbackend.model.UserStatus
import sg.gov.tech.molbagencyportalbackend.repository.ApplicationRepository
import sg.gov.tech.molbagencyportalbackend.util.ApplicationConstants
import sg.gov.tech.molbagencyportalbackend.util.DateUtil
import sg.gov.tech.molbagencyportalbackend.util.EncryptLicenceDataUtil
import sg.gov.tech.molbagencyportalbackend.util.aws.AwsSqsUtil
import sg.gov.tech.utils.ObjectMapperConfigurer
import sg.gov.tech.utils.ObjectMapperConfigurer.GlobalObjectMapper
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class ApplicationService(
    private val applicationRepository: ApplicationRepository,
    private val agencyService: AgencyService,
    private val licenceTypeService: LicenceTypeService,
    private val dashboardApplicationTransfer: DashboardApplicationTransfer,
    private val agencyApplicationTransfer: AgencyApplicationTransfer,
    private val applicationModelTransfer: ApplicationModelTransfer,
    private val l1tResponseDTOTransfer: L1TResponseDTOTransfer,
    private val userService: UserService,
    private val l1tIntegrationService: L1TIntegrationService,
    private val licenceService: LicenceService,
    private val ddsIntegrationService: DDSIntegrationService,
    private val activityTypeService: ActivityTypeService,
    private val awsSqsUtil: AwsSqsUtil,
    private val authenticationFacade: AuthenticationFacade,
    private val encryptLicenceUtil: EncryptLicenceDataUtil,
    private val auditReader: AuditReader,
    private val rfaService: ApplicationRFAService
) {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    companion object {
        private const val CORPPASS = "Corppass"
        private const val SINGPASS = "Singpass"
        private const val PROJECT_CODE = "gbl"
    }

    fun existsByApplicationNumber(applicationNumber: String) =
        applicationRepository.existsByApplicationNumber(applicationNumber)

    fun deriveLoginType(applicationDto: ApplicationDTO): String {
        return applicationDto.company.uen?.let { CORPPASS } ?: SINGPASS
    }

    fun createApplication(requestDTO: CreateApplicationRequestDTO): Application {
        val applicationStatus = ApplicationStatus.SUBMITTED

        val formMetaDataScheme: JsonNode = GlobalObjectMapper.convertValue(requestDTO.version)
        val licenceDataValue =
            encryptLicenceUtil.encryptLicenceNode(requestDTO.application.licence, formMetaDataScheme, true)

        val applicationEntity = Application(
            applicationNumber = requestDTO.application.general.applicationNumber,
            agency = agencyService.findByCode(requestDTO.version.agencyCode)!!,
            licenceType = licenceTypeService.findByLicenceId(requestDTO.application.general.licenceID)!!,
            licenceName = requestDTO.application.general.licenceName,
            status = applicationStatus,
            submittedDate = LocalDateTime.parse(
                requestDTO.application.general.dateSent,
                DateTimeFormatter.ofPattern(DateUtil.DATEFORMAT_DATE_TIME)
            ),
            transactionType = requestDTO.application.general.transactionType,
            applyAs = if (requestDTO.application.profile.applyAs == ApplyAs.APPLICANT.value)
                ApplyAs.APPLICANT else ApplyAs.ON_BEHALF,
            loginType = deriveLoginType(requestDTO.application),
            applicant = requestDTO.application.applicant,
            filer = requestDTO.application.filer,
            company = requestDTO.application.company,
            licenceDataFields = licenceDataValue,
            formMetaData = GlobalObjectMapper.convertValue(requestDTO.version),
            applicantName = (
                if (deriveLoginType(requestDTO.application) == CORPPASS) {
                    requestDTO.application.company.companyName
                } else {
                    requestDTO.application.applicant.name
                }
                )!!,
            reviewer = null,
            caseStatus = applicationStatus.getCaseStatus(null),
            activityType = ActivityValue.CREATE_APPLICATION.let {
                activityTypeService.getActivityType(it) ?: throw InternalConfigException("Activity value $it not found")
            }
        )

        applicationRepository.save(applicationEntity)
        logger.info("Application created: ${applicationEntity.applicationNumber}")

        return applicationEntity
    }

    fun createApplicationDetails(requestDTO: CreateApplicationRequestDTO): L1TResponseDTO {
        val applicationEntity = createApplication(requestDTO)
        return l1tResponseDTOTransfer.createSuccessResponseDTO(
            l1tResponseDTOTransfer.createResultDTO(
                requestDTO.operation,
                applicationEntity.applicationNumber,
                applicationEntity.licenceName,
                applicationEntity.transactionType
            )
        )
    }

    fun validateApplicationDetails(requestDTO: CreateApplicationRequestDTO): L1TResponseDTO {
        return l1tResponseDTOTransfer.createSuccessResponseDTO(
            l1tResponseDTOTransfer.createResultDTO(
                requestDTO.operation,
                requestDTO.application.general.applicationNumber,
                requestDTO.application.general.licenceName,
                requestDTO.application.general.transactionType
            )
        )
    }

    fun getDashboardApplications(requestParams: DashboardApplicationRequestParams): DashboardStatisticsDTO {
        val user: User? = userService.getUserByEmailAndIsDeletedFalse(authenticationFacade.getPrincipalName())
        val agencyId: Long = user?.agencyId!!
        val reviewerId: Long = user?.id!!

        val unassignedCasesCount =
            applicationRepository.countAllByAgencyIdAndReviewerIdIsNullAndStatusNotIn(
                agencyId,
                ApplicationStatus.getFinalStatuses()
            )
        logger.debug("Agency: $agencyId -- unassignedCasesCount: $unassignedCasesCount")

        val myOpenCasesCount = applicationRepository.countAllByAgencyIdAndReviewerIdAndStatusIn(
            agencyId,
            reviewerId,
            ApplicationStatus.getAssignedStatuses()
        )
        logger.debug("Agency: $agencyId -- myOpenCasesCount: $myOpenCasesCount")

        val dashboardApplications = if (requestParams.tab.equals("openCases")) {
            applicationRepository.findAllByAgencyIdAndReviewerIdAndStatusIn(
                agencyId,
                reviewerId,
                ApplicationStatus.getAssignedStatuses(),
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
            applicationRepository.findAllByAgencyIdAndReviewerIdIsNullAndStatusNotIn(
                agencyId,
                ApplicationStatus.getFinalStatuses(),
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
        return DashboardStatisticsDTO(
            myOpenCasesCount,
            unassignedCasesCount,
            dashboardApplications.content.map { dashboardApplicationTransfer.toDTO(it) }
        )
    }

    fun getAgencyApplications(requestParams: AgencyApplicationsRequestParams): ApplicationsDTO {
        val agencyId: Long =
            userService.getUserByEmailAndIsDeletedFalse(authenticationFacade.getPrincipalName())?.agencyId!!

        val agencyCasesCount =
            applicationRepository.countAllByAgencyId(
                agencyId
            )
        logger.debug("Agency: $agencyId -- agencyCasesCount: $agencyCasesCount")
        val sortField =
            if (requestParams.sortField == "officerName") "reviewer.name" else requestParams.sortField

        val agencyApplications =
            applicationRepository.findAllByAgencyId(
                agencyId,
                PageRequest.of(
                    requestParams.page,
                    requestParams.limit,
                    Sort.by(
                        Sort.Order(
                            Sort.Direction.valueOf(requestParams.sortOrder.uppercase()),
                            sortField
                        ).ignoreCase()
                    )
                )
            ).toApplicationsPage()
        return ApplicationsDTO(
            data = agencyApplications.content,
            totalCount = agencyApplications.totalElements
        )
    }

    private fun Page<Application>.toApplicationsPage() =
        PageImpl(content.map { agencyApplicationTransfer.toDTO(it) }, pageable, totalElements)

    fun getApplication(applicationNumber: String): Application {
        return applicationRepository.findByApplicationNumber(applicationNumber)
            ?: throw NotFoundException("Can't find the application with reference number: $applicationNumber")
    }

    fun getApplicationDetails(applicationNumber: String): ApplicationDetailDTO {
        getApplication(applicationNumber).let {
            if (userService.getUserByEmailAndIsDeletedFalse(
                    authenticationFacade.getPrincipalName()
                )?.agencyId != it.agency.id
            )
                throw NotAuthorisedException(
                    "User ${authenticationFacade.getPrincipalName()} is not authorised to access " +
                        "other agency application: $applicationNumber"
                )
            else
                return applicationModelTransfer.toDTO(it)
        }
    }

    fun retrieveDocument(
        applicationNumber: String,
        requestParams: DocumentRequestParams
    ): ResponseEntity<ByteArray> {
        val user: User? =
            userService.getUserByEmailAndIsDeletedFalse(authenticationFacade.getPrincipalName())
        val agencyId: Long = user?.agencyId!!

        val application = getApplication(applicationNumber)

        when {
            application.agency.id != agencyId ->
                throw NotAuthorisedException(
                    "User ${authenticationFacade.getPrincipalName()} is not authorised to access " +
                        "other agency application: $applicationNumber"
                )
        }

        val documentNameList = requestParams.documentName
        try {
            if (documentNameList.size > 1) {
                logger.info("Start download for application number $applicationNumber")

                val currentTime = DateUtil.getTimestamp()
                val downloadFileName = "${applicationNumber}_$currentTime.zip"

                val payload = documentNameList.map {
                    L1TDocument(
                        objectName = it,
                        customFileName = "${applicationNumber}_$it"
                    )
                }.let {
                    L1TMultipleDocumentRequest(
                        projectCode = PROJECT_CODE,
                        files = it,
                        downloadFileName = downloadFileName
                    )
                }

                return l1tIntegrationService.downloadMultipleFiles(payload)
            } else {
                documentNameList.first().let {
                    logger.info("Start download for application number $applicationNumber, filename: $it")

                    val payload = L1TSingleDocumentRequest(
                        projectCode = PROJECT_CODE,
                        objectName = it,
                        downloadFileName = "${applicationNumber}_$it"
                    )

                    return l1tIntegrationService.downloadSingleFile(payload)
                }
            }
        } catch (e: Exception) {
            logger.error(ExceptionControllerAdvice.FILE_DOWNLOAD_ERROR_MESSAGE, e)
            throw FileDownloadException(ExceptionControllerAdvice.FILE_DOWNLOAD_ERROR_MESSAGE)
        }
    }

    @Transactional
    fun claimApplication(applicationNumber: String) {
        val user: User? = userService.getUserByEmailAndIsDeletedFalse(authenticationFacade.getPrincipalName())
        val agencyId: Long = user?.agencyId!!

        val application = getApplication(applicationNumber)

        when {
            application.agency.id != agencyId ->
                throw NotAuthorisedException(
                    "User ${authenticationFacade.getPrincipalName()} is not authorised to access " +
                        "other agency application: $applicationNumber"
                )

            application.reviewer != null ->
                throw ValidationException(
                    ExceptionControllerAdvice.APPLICATION_ALREADY_ASSIGNED_MESSAGE,
                    "Application $applicationNumber already assigned to a different officer"
                )

            application.status !in ApplicationStatus.getClaimableStatuses() ->
                throw ValidationException(
                    ExceptionControllerAdvice.APPLICATION_UPDATED_ELSEWHERE_MESSAGE,
                    "Application $applicationNumber cannot be claimed"
                )
        }

        val claimedApplication = application
            .assignAndUpdateStatusAndOwnership(user)
            .let { applicationRepository.save(it) }

        sendL1TUpdateStatusRequest(claimedApplication)
    }

    fun Application.assignAndUpdateStatusAndOwnership(user: User): Application {
        return this.apply {
            reviewer = user
            if (status in ApplicationStatus.getSubmittedStatuses()) {
                status = ApplicationStatus.PROCESSING
            }
            caseStatus = status.getCaseStatus(reviewer)
            activityType = ActivityValue.CLAIM_APPLICATION.let {
                activityTypeService.getActivityType(it) ?: throw InternalConfigException("Activity value $it not found")
            }
        }
    }

    @Transactional
    fun rejectApplication(applicationNumber: String, rejectMessagesDTO: RejectMessagesDTO) {
        val user: User? = userService.getUserByEmailAndIsDeletedFalse(authenticationFacade.getPrincipalName())
        val agencyId: Long = user?.agencyId!!
        val reviewerId = user?.id!!

        val application = getApplication(applicationNumber)

        when {
            application.agency.id != agencyId ->
                throw NotAuthorisedException(
                    "User ${authenticationFacade.getPrincipalName()} is not authorised to access " +
                        "other agency application: $applicationNumber"
                )

            application.reviewer?.id != reviewerId ->
                throw ValidationException(
                    ExceptionControllerAdvice.APPLICATION_ALREADY_ASSIGNED_MESSAGE,
                    "Application $applicationNumber already assigned to a different officer"
                )

            application.status !in ApplicationStatus.getRejectableStatuses() ->
                throw ValidationException(
                    ExceptionControllerAdvice.APPLICATION_UPDATED_ELSEWHERE_MESSAGE,
                    "Application $applicationNumber cannot be rejected"
                )
        }

        val rejectedApplication = application
            .updateRejectStatus(user, rejectMessagesDTO)
            .let { applicationRepository.save(it) }

        sendL1TUpdateStatusRequest(rejectedApplication)
    }

    fun Application.updateRejectStatus(
        user: User,
        rejectMessagesDTO: RejectMessagesDTO
    ): Application {
        return this.apply {
            reviewer = user
            status = ApplicationStatus.REJECTED
            caseStatus = status.getCaseStatus(reviewer)
            internalRemarks = rejectMessagesDTO.internalRemarks
            messageToApplicant = rejectMessagesDTO.messageToApplicant
            activityType = ActivityValue.REJECT_APPLICATION.let {
                activityTypeService.getActivityType(it) ?: throw InternalConfigException("Activity value $it not found")
            }
        }
    }

    @Transactional
    fun approveApplication(
        applicationNumber: String,
        licenceFile: MultipartFile?,
        additionalDocuments: MultipartFile?,
        requestBody: ApproveApplicationRequestDTO
    ) {
        val user: User? = userService.getUserByEmailAndIsDeletedFalse(authenticationFacade.getPrincipalName())
        val agencyId: Long = user?.agencyId!!
        val reviewerId = user?.id!!

        val application = getApplication(applicationNumber)

        when {
            application.agency.id != agencyId ->
                throw NotAuthorisedException(
                    "User ${authenticationFacade.getPrincipalName()} is not authorised to access " +
                        "other agency application: $applicationNumber"
                )

            application.reviewer?.id != reviewerId ->
                throw ValidationException(
                    ExceptionControllerAdvice.APPLICATION_ALREADY_ASSIGNED_MESSAGE,
                    "Application $applicationNumber already assigned to a different officer"
                )

            application.status !in ApplicationStatus.getApprovableStatuses() ->
                throw ValidationException(
                    ExceptionControllerAdvice.APPLICATION_UPDATED_ELSEWHERE_MESSAGE,
                    "Application $applicationNumber cannot be approved"
                )
        }

        val fileUploadResponse = mutableListOf<DDSUploadResponseDTO>()
        if (requestBody.licenceIssuanceType == LicenceIssuanceType.UPLOAD_LICENCE.value &&
            (licenceFile != null && !licenceFile.isEmpty)
        ) {
            val licenceFileUploadResponse =
                ddsIntegrationService.ddsUploadFiles(
                    requestBody.licenceNumber,
                    application,
                    licenceFile
                )
            licenceFileUploadResponse.body?.let {
                it.documentType = LicenceDocumentType.LICENCE_DOC
                fileUploadResponse.add(it)
            }
        }
        if (additionalDocuments != null && !additionalDocuments.isEmpty) {
            val additionalDocumentsUploadResponse =
                ddsIntegrationService.ddsUploadFiles(
                    requestBody.licenceNumber,
                    application,
                    additionalDocuments
                )
            additionalDocumentsUploadResponse.body?.let {
                it.documentType = LicenceDocumentType.ADDITIONAL_DOC
                fileUploadResponse.add(it)
            }
        }

        // update database with licence and additional document details
        licenceService.createLicence(application, requestBody, fileUploadResponse)
        val approvedApplication =
            application.updateApproveStatus(application.reviewer!!, requestBody)
                .let { applicationRepository.save(it) }

        sendL1TUpdateStatusRequest(approvedApplication)
    }

    fun Application.updateApproveStatus(
        user: User,
        requestBody: ApproveApplicationRequestDTO
    ): Application {
        return this.apply {
            status = if (requestBody.approvalType == "Partially Approved")
                ApplicationStatus.PARTIALLY_APPROVED
            else
                ApplicationStatus.APPROVED
            caseStatus = status.getCaseStatus(user)
            setMessageToApplicant(requestBody.agencyMessageToApplicant)
            setInternalRemarks(requestBody.internalRemarks)
            activityType = ActivityValue.APPROVE_APPLICATION.let {
                activityTypeService.getActivityType(it) ?: throw InternalConfigException("Activity value $it not found")
            }
        }
    }

    fun sendL1TUpdateStatusRequest(application: Application, clarificationFields: JsonNode? = null) {
        val l1tUpdateApplicationStatusRequest =
            l1tIntegrationService.createL1TApplicationStatusRequest(application, clarificationFields)
        val l1tUpdateStatusPayload = L1TUpdateStatusPayload(
            referenceNumber = application.applicationNumber,
            l1tStatusPushRequest = l1tUpdateApplicationStatusRequest
        )
        awsSqsUtil.sendL1TUpdateStatusSqsMessage(l1tUpdateStatusPayload)
        logger.info("Update Status to L1T queued for ${l1tUpdateStatusPayload.referenceNumber}")
    }

    fun updatePendingApplicantActionStatus(
        application: Application,
        user: User,
        requestBody: SendRFADTO
    ): Application {
        return application.apply {
            status = ApplicationStatus.PENDING_APPLICANT_ACTION
            caseStatus = status.getCaseStatus(user)
            // application sent for RFA will have the application reviewer removed
            reviewer = null
            setMessageToApplicant(requestBody.agencyMessageToApplicant)
            setInternalRemarks(requestBody.internalRemarks)
            activityType = ActivityValue.SEND_RFA.let {
                activityTypeService.getActivityType(it) ?: throw InternalConfigException("Activity value $it not found")
            }
        }
    }

    fun reassignApplication(applicationNumber: String, userId: Long) {
        val application = getApplication(applicationNumber)
        val loggedUser: User? = userService.getUserByEmailAndIsDeletedFalse(authenticationFacade.getPrincipalName())
        val newAssignee: User? = userService.getUserById(userId)
        val newAssigneeAgencyId: Long = newAssignee?.agencyId!!
        val loggedUserAgencyId: Long = loggedUser?.agencyId!!

        when {
            application.reviewer?.id == null ->
                throw ValidationException(
                    ExceptionControllerAdvice.CANT_REASSIGN_APPLICATION,
                    "Application $applicationNumber cannot be reassigned"
                )

            application.agency.id != loggedUserAgencyId ->
                throw NotAuthorisedException(
                    "User ${loggedUser.id} is not authorised to access " +
                        "other agency application: $applicationNumber"
                )

            (loggedUserAgencyId != newAssigneeAgencyId) ->
                throw ValidationException(
                    ExceptionControllerAdvice.CANT_REASSIGN_TO_DIFF_AGENCY,
                    "User ${loggedUser.id} is not authorised to assign " +
                        "$applicationNumber to user ${newAssignee.id} belonging to different agency"
                )

            (
                application.reviewer?.id != loggedUser.id &&
                    loggedUser.role.authorities.any { it.code == ApplicationConstants.REASSIGN_SELF }
                )
            -> throw ValidationException(
                ExceptionControllerAdvice.APPLICATION_UPDATED_ELSEWHERE_MESSAGE,
                "Application $applicationNumber cannot be reassigned"
            )

            newAssignee.isDeleted ->
                throw NotFoundException(
                    "User ${newAssignee.id} has been removed"
                )

            newAssignee.status != UserStatus.ACTIVE ->
                throw ValidationException(
                    ExceptionControllerAdvice.NEW_USER_INACTIVE_MESSAGE,
                    "User ${newAssignee.id} is marked Inactive"
                )

            newAssignee.role.authorities.find { it.code == ApplicationConstants.PROCESS_APPLICATION } == null ->
                throw ValidationException(
                    ExceptionControllerAdvice.NEW_USER_NO_PERMISSION,
                    "User ${newAssignee.id} " +
                        "does not have necessary permissions to process the application"
                )
            // can also use ApplicationStatus.APPROVED.getCaseStatus()
            // || ApplicationStatus.REJECTED.getCaseStatus() ,
            // but it makes code lengthy so going with constants
            application.caseStatus == ApplicationConstants.CASE_STATUS_CLOSED ->
                throw ValidationException(
                    ExceptionControllerAdvice.APPLICATION_UPDATED_ELSEWHERE_MESSAGE,
                    "Application $applicationNumber cannot be reassigned"
                )
        }
        application.updateApplicationReviewerId(newAssignee).let { applicationRepository.save(it) }
    }

    fun Application.updateApplicationReviewerId(user: User): Application {
        return this.apply { reviewer = user }
    }

    fun withdrawApplication(requestDTO: WithdrawApplicationDTO): L1TResponseDTO {
        val application = applicationRepository
            .findByApplicationNumber(requestDTO.application.general.applicationNumber)
            ?: return L1TResponseDTO(
                L1TResultDTO(
                    requestDTO.operation,
                    requestDTO.application.general.applicationNumber,
                    requestDTO.application.general.licenceName
                ),
                L1TErrorDTO(
                    "Business Logic Error",
                    "Your application has just been submitted and is not in the system yet. " +
                        "This may take up to 2 hours. Please try again later.",
                    emptyList(),
                    "/withdrawApplication",
                    "1.1"
                )
            )
        if (application.status in ApplicationStatus.getFinalStatuses()) {
            return L1TResponseDTO(
                L1TResultDTO(
                    requestDTO.operation,
                    requestDTO.application.general.applicationNumber,
                    requestDTO.application.general.licenceName
                ),
                L1TErrorDTO(
                    "Business Logic Error",
                    "The application has already been processed. " +
                        "Please return to My Submissions to see the latest update.",
                    emptyList(),
                    "/withdrawApplication",
                    "1.1"
                )
            )
        }
        val edited = application.copy(
            status = ApplicationStatus.PENDING_WITHDRAWAL,
            caseStatus = ApplicationStatus.PENDING_WITHDRAWAL.getCaseStatus(application.reviewer)
        )
        applicationRepository.save(edited)
        return L1TResponseDTO(
            L1TResultDTO(
                requestDTO.operation,
                requestDTO.application.general.applicationNumber,
                requestDTO.application.general.licenceName
            ),
            L1TErrorDTO(
                "PENDING_WITHDRAWN",
                "Application No. ${requestDTO.application.general.applicationNumber}" +
                    " is now ${ApplicationStatus.PENDING_WITHDRAWAL.value}",
                emptyList(),
                "/withdrawApplication",
                "1.1"
            )
        )
    }

    fun updateApplicationRFAResponded(
        requestDTO: CreateApplicationRequestDTO,
        application: Application
    ): Application {
        val applicationStatus = if (application.reviewer == null) ApplicationStatus.RFA_RESPONDED
        else ApplicationStatus.PROCESSING

        val formMetaDataScheme: JsonNode = ObjectMapperConfigurer.GlobalObjectMapper.convertValue(requestDTO.version)
        val licenceDataValue =
            encryptLicenceUtil.encryptLicenceNode(requestDTO.application.licence, formMetaDataScheme, true)

        val applicationEntity = application.copy(
            applicationNumber = requestDTO.application.general.applicationNumber,
            agency = agencyService.findByCode(requestDTO.version.agencyCode)!!,
            licenceType = licenceTypeService.findByLicenceId(requestDTO.application.general.licenceID)!!,
            licenceName = requestDTO.application.general.licenceName,
            status = applicationStatus,
            submittedDate = LocalDateTime.parse(
                requestDTO.application.general.dateSent,
                DateTimeFormatter.ofPattern(DateUtil.DATEFORMAT_DATE_TIME)
            ),
            transactionType = requestDTO.application.general.transactionType,
            applyAs = if (requestDTO.application.profile.applyAs == ApplyAs.APPLICANT.value)
                ApplyAs.APPLICANT else ApplyAs.ON_BEHALF,
            loginType = deriveLoginType(requestDTO.application),
            applicant = requestDTO.application.applicant,
            filer = requestDTO.application.filer,
            company = requestDTO.application.company,
            licenceDataFields = licenceDataValue,
            formMetaData = ObjectMapperConfigurer.GlobalObjectMapper.convertValue(requestDTO.version),
            applicantName = (
                if (deriveLoginType(requestDTO.application) == ApplicationService.CORPPASS) {
                    requestDTO.application.company.companyName
                } else {
                    requestDTO.application.applicant.name
                }
                )!!,
            reviewer = if (application.reviewer != null) application.reviewer else null,
            caseStatus = applicationStatus.getCaseStatus(null),
            activityType = ActivityValue.RFA_RESPONDED.let {
                activityTypeService.getActivityType(it) ?: throw InternalConfigException("Activity value $it not found")
            }
        )

        applicationRepository.save(applicationEntity)
        return applicationEntity
    }

    fun getAgencyReassignUsers(applicationNumber: String): List<ReassignUserDTOProjection> {
        val application = getApplication(applicationNumber)

        return application.reviewer?.id?.let {
            userService.getAgencyReassignUsers(
                application.reviewer!!.id!!,
                ApplicationConstants.PROCESS_APPLICATION
            )
        } ?: throw ValidationException(
            ExceptionControllerAdvice.CANT_REASSIGN_APPLICATION,
            "Claim Application $applicationNumber before reassigning"
        )
    }

    fun Application.updateWithdrawalStatus(
        user: User,
        newAppStatus: ApplicationStatus,
        requestBody: WithdrawApplicationRequestDTO,
        remainAssigned: Boolean
    ): Application {
        return this.apply {
            status = newAppStatus
            caseStatus = status.getCaseStatus(user)
            setMessageToApplicant(requestBody.agencyMessageToApplicant)
            setInternalRemarks(requestBody.internalRemarks)
            if (!remainAssigned) reviewer = null
            activityType =
                if (newAppStatus == ApplicationStatus.WITHDRAWN) ActivityValue.APPROVE_APPLICATION_WITHDRAWAL.let {
                    activityTypeService.getActivityType(it)
                        ?: throw InternalConfigException("Activity value $it not found")
                } else ActivityValue.REJECT_APPLICATION_WITHDRAWAL.let {
                    activityTypeService.getActivityType(it)
                        ?: throw InternalConfigException("Activity value $it not found")
                }
        }
    }

    fun getPreviousApplicationStatus(applicationId: Long): ApplicationStatus? {
        val applicationHistory = auditReader.createQuery()
            .forRevisionsOfEntity(Application::class.java, true, false)
            .add(AuditEntity.id().eq(applicationId)).resultList as List<Application>
        val latestAppStatus = applicationHistory.get(applicationHistory.size - 1).status
        for (i in applicationHistory.indices.reversed())
            if (applicationHistory[i].status !== latestAppStatus)
                return applicationHistory[i].status

        return null
    }

    @Transactional
    fun processApplicationWithdrawal(
        applicationNumber: String,
        requestBody: WithdrawApplicationRequestDTO
    ): WithdrawApplicationResponseDTO {
        val user: User? = userService.getUserByEmailAndIsDeletedFalse(authenticationFacade.getPrincipalName())
        val agencyId: Long = user?.agencyId!!
        val reviewerId = user?.id!!
        val application = getApplication(applicationNumber)

        when {
            application.agency.id != agencyId ->
                throw NotAuthorisedException(
                    "User ${authenticationFacade.getPrincipalName()} is not authorised to access " +
                        "other agency application: $applicationNumber"
                )

            application.reviewer?.id != reviewerId ->
                throw ValidationException(
                    ExceptionControllerAdvice.APPLICATION_ALREADY_ASSIGNED_MESSAGE,
                    "Application $applicationNumber already assigned to a different officer"
                )

            // check if application status is Pending Withdrawal
            application.status !in ApplicationStatus.getWithdrawableStatuses() -> throw ValidationException(
                ExceptionControllerAdvice.APPLICATION_UPDATED_ELSEWHERE_MESSAGE,
                "Application $applicationNumber not withdrawable"
            )
        }

        val previousAppStatus = application.id?.let {
            getPreviousApplicationStatus(it) ?: throw InternalConfigException("No changes in application status")
        }

        when (val requestType = requestBody.requestType) {
            "Approve" -> {
                // Update rfa status to cancelled if RFA status is PAA
                val latestRFARecord = application.id?.let { rfaService.getLatestApplicationRFA(it) }
                if (latestRFARecord != null && latestRFARecord?.status == RFAStatus.PENDING_APPLICANT_ACTION)
                    rfaService.updateRFACancelled(application.id!!, latestRFARecord)

                logger.info("Application $applicationNumber status changed to ${ApplicationStatus.WITHDRAWN}")
                val approvedWithdrawalApplication = application.updateWithdrawalStatus(
                    application.reviewer!!,
                    ApplicationStatus.WITHDRAWN,
                    requestBody,
                    true
                ).also { applicationRepository.save(it) }
                sendL1TUpdateStatusRequest(approvedWithdrawalApplication)
                return WithdrawApplicationResponseDTO(approvedWithdrawalApplication.status.value)
            }
            "Reject" -> {
                when (previousAppStatus) {
                    // revert to status before, unassign officer from case
                    in ApplicationStatus.getPendingApplicantStatuses() -> {
                        logger.info("Application $applicationNumber status changed to $previousAppStatus")
                        val rejectWithdrawalApplication = application.updateWithdrawalStatus(
                            application.reviewer!!,
                            previousAppStatus!!,
                            requestBody,
                            false
                        ).also { applicationRepository.save(it) }
                        sendL1TUpdateStatusRequest(rejectWithdrawalApplication)
                        return WithdrawApplicationResponseDTO(rejectWithdrawalApplication.status.value)
                    }
                    // update status to processing, remains assigned
                    in ApplicationStatus.getPendingAgencyStatuses() -> {
                        logger.info("Application $applicationNumber status changed to ${ApplicationStatus.PROCESSING}")
                        val rejectWithdrawalApplication = application.updateWithdrawalStatus(
                            application.reviewer!!,
                            ApplicationStatus.PROCESSING,
                            requestBody,
                            true
                        ).also { applicationRepository.save(it) }
                        sendL1TUpdateStatusRequest(rejectWithdrawalApplication)
                        return WithdrawApplicationResponseDTO(rejectWithdrawalApplication.status.value)
                    }
                    else -> throw InternalConfigException("Application $applicationNumber exist in terminal State")
                }
            }
            else ->
                throw InternalConfigException("Request type $requestType not supported")
        }
    }

    fun updateApplicationCancelRFA(
        application: Application,
        user: User,
        requestBody: CancelRFARequestDTO
    ): Application {
        return application.apply {
            status = ApplicationStatus.PROCESSING
            caseStatus = status.getCaseStatus(user)
            setMessageToApplicant(requestBody.agencyMessageToApplicant)
            setInternalRemarks(requestBody.internalRemarks)
            activityType = ActivityValue.CANCEL_RFA.let {
                activityTypeService.getActivityType(it) ?: throw InternalConfigException("Activity value $it not found")
            }
        }
    }
}
