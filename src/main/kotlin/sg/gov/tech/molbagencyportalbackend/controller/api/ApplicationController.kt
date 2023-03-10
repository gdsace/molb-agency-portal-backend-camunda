package sg.gov.tech.molbagencyportalbackend.controller.api

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import sg.gov.tech.molbagencyportalbackend.dto.ApplicationDetailDTO
import sg.gov.tech.molbagencyportalbackend.dto.internal.AgencyApplicationsRequestParams
import sg.gov.tech.molbagencyportalbackend.dto.internal.ApplicationsDTO
import sg.gov.tech.molbagencyportalbackend.dto.internal.ApproveApplicationRequestDTO
import sg.gov.tech.molbagencyportalbackend.dto.internal.DashboardApplicationRequestParams
import sg.gov.tech.molbagencyportalbackend.dto.internal.DashboardStatisticsDTO
import sg.gov.tech.molbagencyportalbackend.dto.internal.DocumentRequestParams
import sg.gov.tech.molbagencyportalbackend.dto.internal.RejectMessagesDTO
import sg.gov.tech.molbagencyportalbackend.dto.internal.WithdrawApplicationRequestDTO
import sg.gov.tech.molbagencyportalbackend.dto.internal.WithdrawApplicationResponseDTO
import sg.gov.tech.molbagencyportalbackend.dto.internal.user.ReassignUserDTOProjection
import sg.gov.tech.molbagencyportalbackend.exception.InternalConfigException
import sg.gov.tech.molbagencyportalbackend.service.ApplicationService
import sg.gov.tech.molbagencyportalbackend.util.FeatureToggle
import javax.validation.Valid

@RestController
@RequestMapping("/api")
class ApplicationController(
    private val applicationService: ApplicationService,
    private val featureToggle: FeatureToggle
) {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    @GetMapping("/dashboard/applications")
    fun getDashboardApplications(@Valid requestParams: DashboardApplicationRequestParams): DashboardStatisticsDTO {
        logger.info("Retrieving Dashboard Applications")
        return applicationService.getDashboardApplications(requestParams)
    }

    @GetMapping("/applications")
    fun getAgencyApplications(@Valid requestParams: AgencyApplicationsRequestParams): ApplicationsDTO {
        logger.info("Retrieving Agency Applications")
        return applicationService.getAgencyApplications(requestParams)
    }

    @GetMapping("/application/{applicationNumber}")
    fun getApplication(@PathVariable applicationNumber: String): ApplicationDetailDTO {
        logger.info("Retrieving application : $applicationNumber")
        return applicationService.getApplicationDetails(applicationNumber)
    }

    @GetMapping("application/{applicationNumber}/document")
    fun getDocument(
        @PathVariable applicationNumber: String,
        requestParams: DocumentRequestParams
    ): ResponseEntity<ByteArray> {
        return applicationService.retrieveDocument(applicationNumber, requestParams)
    }

    @PreAuthorize("hasAuthority('process_application')")
    @PostMapping("application/{applicationNumber}/claim")
    fun claimApplication(
        @PathVariable applicationNumber: String
    ) {
        logger.info("Claiming application: $applicationNumber")
        return applicationService.claimApplication(applicationNumber)
    }

    @PreAuthorize("hasAuthority('process_application')")
    @PostMapping("application/{applicationNumber}/reject")
    fun rejectApplication(
        @PathVariable applicationNumber: String,
        @RequestBody requestBody: RejectMessagesDTO
    ) {
        logger.info("Rejecting application: $applicationNumber")
        return applicationService.rejectApplication(applicationNumber, requestBody)
    }

    @PreAuthorize("hasAuthority('process_application')")
    @PostMapping("application/{applicationNumber}/approve")
    fun approveApplication(
        @PathVariable applicationNumber: String,
        @RequestPart(value = "licenceFile", required = false) licenceFile: MultipartFile?,
        @RequestPart(value = "additionalDocuments", required = false) additionalDocuments: MultipartFile?,
        @Valid @RequestPart(value = "approveFormData", required = true)
        approveFormData: ApproveApplicationRequestDTO
    ) {
        logger.info("Approving application: $applicationNumber")
        applicationService.approveApplication(applicationNumber, licenceFile, additionalDocuments, approveFormData)
    }

    @PreAuthorize("hasAuthority('reassign_all') or hasAuthority('reassign_self')")
    @PostMapping("/application/{applicationNumber}/reassign/{userId}")
    fun reassignApplication(
        @PathVariable applicationNumber: String,
        @PathVariable userId: Long
    ) {
        if (!featureToggle.isReassignEnabled()) throw InternalConfigException(FeatureToggle.REASSIGN_NOT_ENABLED)

        logger.info("Reassigning application: $applicationNumber to user with Id $userId")
        applicationService.reassignApplication(applicationNumber, userId)
    }

    @PreAuthorize("hasAuthority('reassign_all') or hasAuthority('reassign_self')")
    @GetMapping("/application/{applicationNumber}/reassign/users")
    fun getUsersForReassign(@PathVariable applicationNumber: String): List<ReassignUserDTOProjection> {
        if (!featureToggle.isReassignEnabled()) throw InternalConfigException(FeatureToggle.REASSIGN_NOT_ENABLED)

        logger.info("Getting Users For Reassign")
        return applicationService.getAgencyReassignUsers(applicationNumber)
    }

    @PreAuthorize("hasAuthority('process_application')")
    @PostMapping("application/{applicationNumber}/withdraw")
    fun withdrawApplication(
        @PathVariable applicationNumber: String,
        @Valid @RequestBody
        requestBody: WithdrawApplicationRequestDTO
    ): WithdrawApplicationResponseDTO {
        if (!featureToggle.isWithdrawalEnabled()) throw InternalConfigException(FeatureToggle.WITHDRAWAL_NOT_ENABLED)
        logger.info("${requestBody.requestType} withdrawal request of application: $applicationNumber")
        return applicationService.processApplicationWithdrawal(applicationNumber, requestBody)
    }
}
