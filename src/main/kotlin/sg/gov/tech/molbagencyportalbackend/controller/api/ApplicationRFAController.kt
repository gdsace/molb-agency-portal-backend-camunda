package sg.gov.tech.molbagencyportalbackend.controller.api

import org.slf4j.LoggerFactory
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import sg.gov.tech.molbagencyportalbackend.dto.ApplicationRFAListDTO
import sg.gov.tech.molbagencyportalbackend.dto.ApplicationRFARequestParams
import sg.gov.tech.molbagencyportalbackend.dto.CancelRFARequestDTO
import sg.gov.tech.molbagencyportalbackend.dto.CancelRFAResponseDTO
import sg.gov.tech.molbagencyportalbackend.dto.SendRFADTO
import sg.gov.tech.molbagencyportalbackend.exception.InternalConfigException
import sg.gov.tech.molbagencyportalbackend.service.ApplicationRFAService
import sg.gov.tech.molbagencyportalbackend.util.FeatureToggle
import javax.validation.Valid

@RestController
@RequestMapping("/api")
class ApplicationRFAController(
    private var applicationRFAService: ApplicationRFAService,
    private val featureToggle: FeatureToggle
) {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    @GetMapping("/application/{applicationNumber}/rfa")
    fun getRFAList(
        @PathVariable applicationNumber: String,
        @Valid requestParams: ApplicationRFARequestParams
    ): ApplicationRFAListDTO {
        if (!featureToggle.isRFAEnabled()) throw InternalConfigException(FeatureToggle.RFA_NOT_ENABLED)

        return applicationRFAService.getApplicationRFA(applicationNumber, requestParams)
    }

    @PreAuthorize("hasAuthority('process_application')")
    @PostMapping("/application/{applicationNumber}/sendRFA")
    fun sendRFA(
        @PathVariable applicationNumber: String,
        @RequestBody requestBody: SendRFADTO
    ) {
        if (!featureToggle.isRFAEnabled()) throw InternalConfigException(FeatureToggle.RFA_NOT_ENABLED)

        logger.info("Submitting RFA for application: $applicationNumber")
        applicationRFAService.sendRFA(applicationNumber, requestBody)
    }

    @PreAuthorize("hasAuthority('process_application')")
    @PostMapping("/application/{applicationNumber}/cancelRFA")
    fun cancelRFA(
        @PathVariable applicationNumber: String,
        @RequestBody requestBody: CancelRFARequestDTO
    ): CancelRFAResponseDTO {
        if (!featureToggle.isRFAEnabled()) throw InternalConfigException(FeatureToggle.RFA_NOT_ENABLED)

        logger.info("Cancelling RFA for application: $applicationNumber")
        return applicationRFAService.cancelRFA(applicationNumber, requestBody)
    }
}
