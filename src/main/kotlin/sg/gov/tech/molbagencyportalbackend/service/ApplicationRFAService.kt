package sg.gov.tech.molbagencyportalbackend.service

import org.hibernate.envers.AuditReader
import org.hibernate.envers.query.AuditEntity
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Lazy
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import sg.gov.tech.molbagencyportalbackend.auth.AuthenticationFacade
import sg.gov.tech.molbagencyportalbackend.dto.ApplicationRFADTO
import sg.gov.tech.molbagencyportalbackend.dto.ApplicationRFAListDTO
import sg.gov.tech.molbagencyportalbackend.dto.ApplicationRFARequestParams
import sg.gov.tech.molbagencyportalbackend.dto.CancelRFARequestDTO
import sg.gov.tech.molbagencyportalbackend.dto.CancelRFAResponseDTO
import sg.gov.tech.molbagencyportalbackend.dto.SendRFADTO
import sg.gov.tech.molbagencyportalbackend.dto.internal.user.ReviewerModelTransfer
import sg.gov.tech.molbagencyportalbackend.dto.l1t.CreateApplicationRequestDTO
import sg.gov.tech.molbagencyportalbackend.dto.l1t.L1TResponseDTO
import sg.gov.tech.molbagencyportalbackend.dto.l1t.L1TResponseDTOTransfer
import sg.gov.tech.molbagencyportalbackend.exception.ExceptionControllerAdvice
import sg.gov.tech.molbagencyportalbackend.exception.InternalConfigException
import sg.gov.tech.molbagencyportalbackend.exception.NotAuthorisedException
import sg.gov.tech.molbagencyportalbackend.exception.NotFoundException
import sg.gov.tech.molbagencyportalbackend.exception.ValidationException
import sg.gov.tech.molbagencyportalbackend.model.Application
import sg.gov.tech.molbagencyportalbackend.model.ApplicationStatus
import sg.gov.tech.molbagencyportalbackend.model.RFA
import sg.gov.tech.molbagencyportalbackend.model.RFAStatus
import sg.gov.tech.molbagencyportalbackend.model.User
import sg.gov.tech.molbagencyportalbackend.repository.ApplicationRepository
import sg.gov.tech.molbagencyportalbackend.repository.RFARepository
import java.time.LocalDateTime

@Service
class ApplicationRFAService(
    @Lazy private val applicationService: ApplicationService,
    private val applicationRepository: ApplicationRepository,
    private val l1tResponseDTOTransfer: L1TResponseDTOTransfer,
    private val userService: UserService,
    private val authenticationFacade: AuthenticationFacade,
    private val rfaRepository: RFARepository,
    private val auditReader: AuditReader,
    private val reviewerModelTransfer: ReviewerModelTransfer
) {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    fun getLatestApplicationRFA(applicationID: Long) = rfaRepository.getFirstByApplicationIdOrderByIdDesc(applicationID)

    fun getApplicationRFA(
        applicationNumber: String,
        requestParams: ApplicationRFARequestParams
    ): ApplicationRFAListDTO {
        val agencyId: Long =
            userService.getUserByEmailAndIsDeletedFalse(authenticationFacade.getPrincipalName())?.agencyId!!

        val application = applicationService.getApplication(applicationNumber)

        when {
            application.agency.id != agencyId ->
                throw NotAuthorisedException(
                    "User ${authenticationFacade.getPrincipalName()} is not authorised to access " +
                        "other agency application: $applicationNumber"
                )
        }

        val sortField = if (requestParams.sortField == "rfaNo") "revisionIdIndexUpdated" else requestParams.sortField
        val applicationRfa = rfaRepository.findAllByApplicationId(
            application.id!!,
            PageRequest.of(
                requestParams.page,
                requestParams.limit,
                Sort.by(
                    Sort.Order(Sort.Direction.valueOf(requestParams.sortOrder.uppercase()), sortField).ignoreCase()
                )
            )
        )

        val totalRfaCount = application.id?.let { rfaRepository.countAllByApplicationId(it) }
        val modifiedList = applicationRfa.content.mapIndexed { rfaIndex, it ->
            val latestRevisionId =
                auditReader.getRevisions(Application::class.java, application.id)[it.revisionIdIndexUpdated]
            val applicationHistory = auditReader.createQuery()
                .forEntitiesAtRevision(Application::class.java, latestRevisionId)
                .add(AuditEntity.id().eq(application.id)).singleResult as Application

            val rfaNumber = if (requestParams.sortOrder.uppercase() == "ASC") {
                requestParams.page * requestParams.limit + 1 + rfaIndex
            } else {
                totalRfaCount!!.minus(requestParams.page * requestParams.limit + rfaIndex)
            }

            ApplicationRFADTO(
                rfaNo = rfaNumber,
                sendDate = it.createdAt,
                responseDate = it.responseDate?.toLocalDate(),
                rfaStatus = it.status,
                agencyMessageToApplicant = applicationHistory.messageToApplicant,
                internalRemarks = applicationHistory.internalRemarks,
                applicantResponse = it.applicantRemarks,
                updatedBy = it.updatedBy?.let {
                    userService.getUserById(it.toLong())
                        ?.let { it1 -> reviewerModelTransfer.toDTO(it1) }
                }
            )
        }
        return ApplicationRFAListDTO(totalCount = totalRfaCount!!, applicationRFA = modifiedList)
    }

    @Transactional
    fun sendRFA(applicationNumber: String, requestBody: SendRFADTO) {
        val user: User? = userService.getUserByEmailAndIsDeletedFalse(authenticationFacade.getPrincipalName())
        val agencyId: Long = user?.agencyId!!

        val application = applicationService.getApplication(applicationNumber)

        when {
            application.agency.id != agencyId ->
                throw NotAuthorisedException(
                    "User ${authenticationFacade.getPrincipalName()} is not authorised to access " +
                        "other agency application: $applicationNumber"
                )

            // check if there exists rfa in PAA state then do not create RFA record
            application.id?.let {
                rfaRepository.existsByApplicationIdAndStatus(it, RFAStatus.PENDING_APPLICANT_ACTION)
            } == true ->
                throw ValidationException(
                    ExceptionControllerAdvice.RFA_EXIST_MESSAGE,
                    "RFA for Application $applicationNumber is already Pending Applicant Action"
                )

            // check if the current application can be sent for RFA
            application.status !in ApplicationStatus.getPendingApplicantActionStatuses() ->
                throw ValidationException(
                    ExceptionControllerAdvice.APPLICATION_UPDATED_ELSEWHERE_MESSAGE,
                    "Application $applicationNumber cannot be RFAed"
                )

            application.reviewer?.id != user.id ->
                throw ValidationException(
                    ExceptionControllerAdvice.APPLICATION_ALREADY_ASSIGNED_MESSAGE,
                    "Application $applicationNumber already assigned to a different officer"
                )
        }

        // save RFA to the RFA table
        saveRFARecord(application, requestBody)
        // update application status and details
        val updatedApplication =
            applicationService.updatePendingApplicantActionStatus(application, application.reviewer!!, requestBody)
                .let { applicationRepository.save(it) }
        // update status at L1T
        applicationService.sendL1TUpdateStatusRequest(updatedApplication, requestBody.clarificationFields)
    }

    private fun saveRFARecord(application: Application, requestBody: SendRFADTO) {
        val rfa = RFA(
            application = application,
            revisionIdIndexUpdated = auditReader.getRevisions(
                Application::class.java,
                application.id
            ).lastIndex.plus(1),
            status = RFAStatus.PENDING_APPLICANT_ACTION,
            clarificationFields = requestBody.clarificationFields,
            applicantRemarks = null,
            responseDate = null,
            revisionIdIndexCreated = auditReader.getRevisions(
                Application::class.java,
                application.id
            ).lastIndex.plus(1)
        )
        rfaRepository.save(rfa)
    }

    @Transactional
    fun clarifyApplicationRFA(requestDTO: CreateApplicationRequestDTO): L1TResponseDTO {
        val application = applicationService.getApplication(requestDTO.application.general.applicationNumber)
        // update RFA
        updateRFASubmitted(application.id!!, requestDTO)
        // update application
        val applicationEntity = applicationService.updateApplicationRFAResponded(requestDTO, application)
        logger.info("Application updated: ${applicationEntity.applicationNumber}")

        return l1tResponseDTOTransfer.createSuccessResponseDTO(
            l1tResponseDTOTransfer.createResultDTO(
                requestDTO.operation,
                applicationEntity.applicationNumber,
                applicationEntity.licenceName,
                applicationEntity.transactionType
            )
        )
    }

    fun updateRFASubmitted(
        applicationId: Long,
        requestDTO: CreateApplicationRequestDTO
    ): RFA {
        val rfa = getRFAByApplicationIdAndStatus(applicationId, RFAStatus.PENDING_APPLICANT_ACTION)
        val rfaEntity = rfa[0].copy(
            revisionIdIndexUpdated = auditReader.getRevisions(Application::class.java, applicationId).lastIndex.plus(1),
            status = RFAStatus.RFA_RESPONDED,
            applicantRemarks = requestDTO.application.getRemarks(),
            responseDate = LocalDateTime.now()
        )
        rfaRepository.save(rfaEntity)
        return rfaEntity
    }

    fun getRFAByApplicationIdAndStatus(applicationId: Long, rfaStatus: RFAStatus): List<RFA> {
        val rfaRecords = rfaRepository.getRFAByApplicationIdAndStatus(applicationId, rfaStatus)
        if (rfaRecords.isEmpty())
            throw NotFoundException("No RFA record found for applicationId: $applicationId")
        else
            return rfaRecords
    }

    fun updateRFACancelled(applicationId: Long, latestRFA: RFA) {
        val rfaEntity = latestRFA.copy(
            revisionIdIndexUpdated = auditReader.getRevisions(Application::class.java, applicationId).lastIndex.plus(1),
            status = RFAStatus.CANCELLED
        )
        rfaRepository.save(rfaEntity)
    }

    @Transactional
    fun cancelRFA(applicationNumber: String, requestBody: CancelRFARequestDTO): CancelRFAResponseDTO {
        val user: User? = userService.getUserByEmailAndIsDeletedFalse(authenticationFacade.getPrincipalName())
        val agencyId: Long = user?.agencyId!!
        val reviewerId = user?.id!!

        val application = applicationService.getApplication(applicationNumber)

        // application validation
        when {
            application.agency.id != agencyId ->
                throw NotAuthorisedException(
                    "User ${authenticationFacade.getPrincipalName()} is not authorised to access " +
                        "other agency application: $applicationNumber"
                )
            application.status == ApplicationStatus.RFA_RESPONDED  ->
                throw ValidationException(
                    ExceptionControllerAdvice.APPLICANT_ALREADY_RESPONDED,
                    "Applicant has already responded to the RFA for Application No. $applicationNumber"
                )
            application.reviewer?.id != reviewerId ->
                throw ValidationException(
                    ExceptionControllerAdvice.APPLICATION_ALREADY_ASSIGNED_MESSAGE,
                    "Application $applicationNumber already assigned to a different officer"
                )

            // check if application status is PAA
            application.status != ApplicationStatus.PENDING_APPLICANT_ACTION -> throw ValidationException(
                ExceptionControllerAdvice.APPLICATION_UPDATED_ELSEWHERE_MESSAGE,
                "Application $applicationNumber RFA not cancellable"
            )
        }

        // rfa validation
        val latestRFA = application.id?.let {
            getLatestApplicationRFA(it)
                ?: throw InternalConfigException("Application $applicationNumber has no RFA records")
        }

        if (latestRFA?.status != RFAStatus.PENDING_APPLICANT_ACTION) throw ValidationException(
            ExceptionControllerAdvice.APPLICATION_UPDATED_ELSEWHERE_MESSAGE,
            "Application $applicationNumber RFA not cancellable"
        )

        // TODOo Edge cases handling

        // Allow rfa to be cancelled
        updateRFACancelled(application.id!!, latestRFA)
        val updatedApplication = applicationService.updateApplicationCancelRFA(application, user, requestBody).also {
            applicationRepository.save(it)
        }
        applicationService.sendL1TUpdateStatusRequest(updatedApplication)
        return CancelRFAResponseDTO(updatedApplication.status.value)
    }
}
