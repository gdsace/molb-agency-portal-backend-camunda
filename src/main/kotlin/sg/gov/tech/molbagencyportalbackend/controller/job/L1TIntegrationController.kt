package sg.gov.tech.molbagencyportalbackend.controller.job

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus
import sg.gov.tech.molbagencyportalbackend.integration.job.JobConfig
import sg.gov.tech.molbagencyportalbackend.integration.job.L1TUpdateStatusPayload
import sg.gov.tech.molbagencyportalbackend.service.L1TIntegrationService

@JobController
class L1TIntegrationController(
    private val l1tIntegrationService: L1TIntegrationService
) {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(JobConfig.Endpoint.L1T_UPDATE_STATUS)
    fun updateL1TStatus(@RequestBody payload: L1TUpdateStatusPayload) {
        logger.info("Updating application status for ${payload.referenceNumber}")
        l1tIntegrationService.updateL1TStatus(payload)
    }
}
