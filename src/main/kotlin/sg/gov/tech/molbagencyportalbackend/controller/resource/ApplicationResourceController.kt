package sg.gov.tech.molbagencyportalbackend.controller.resource

import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import sg.gov.tech.molbagencyportalbackend.dto.l1t.CreateApplicationRequestDTO
import sg.gov.tech.molbagencyportalbackend.dto.l1t.L1TResponseDTO
import sg.gov.tech.molbagencyportalbackend.dto.l1t.WithdrawApplicationDTO
import sg.gov.tech.molbagencyportalbackend.exception.InternalConfigException
import sg.gov.tech.molbagencyportalbackend.service.ApplicationRFAService
import sg.gov.tech.molbagencyportalbackend.service.ApplicationService
import sg.gov.tech.molbagencyportalbackend.util.FeatureToggle
import javax.validation.Valid

@RestController
@RequestMapping("/resource")
class ApplicationResourceController(
    private val applicationService: ApplicationService,
    private val applicationRFAService: ApplicationRFAService,
    private val featureToggle: FeatureToggle
) {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    @PostMapping("/application")
    fun createApplication(
        @RequestBody @Valid
        requestBody: CreateApplicationRequestDTO
    ): L1TResponseDTO {
        logger.info("Received Create Application request for: ${requestBody.application.general.applicationNumber}")
        return applicationService.createApplicationDetails(requestBody)
    }

    @PostMapping("/validation")
    fun validateApplication(
        @RequestBody @Valid
        requestBody: CreateApplicationRequestDTO
    ): L1TResponseDTO {
        logger.info("Received Application Validation request for: ${requestBody.application.general.applicationNumber}")
        return applicationService.validateApplicationDetails(requestBody)
    }

    @PutMapping("/clarification")
    fun clarifyApplication(
        @RequestBody @Valid
        requestBody: CreateApplicationRequestDTO
    ): L1TResponseDTO {
        if (!featureToggle.isRFAEnabled()) throw InternalConfigException(FeatureToggle.RFA_NOT_ENABLED)

        logger.info("Received Application Validation request for: ${requestBody.application.general.applicationNumber}")
        return applicationRFAService.clarifyApplicationRFA(requestBody)
    }

    @PutMapping("/withdrawApplication")
    fun withdrawApplication(
        @RequestBody @Valid
        requestBody: WithdrawApplicationDTO
    ): L1TResponseDTO {
        if (!featureToggle.isWithdrawalEnabled()) throw InternalConfigException(FeatureToggle.WITHDRAWAL_NOT_ENABLED)

        logger.info("Received Application Withdrawal request for: ${requestBody.application.general.applicationNumber}")
        return applicationService.withdrawApplication(requestBody)
    }
}
