package sg.gov.tech.molbagencyportalbackend.controller.job

import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PostMapping
import sg.gov.tech.molbagencyportalbackend.integration.job.JobConfig
import sg.gov.tech.molbagencyportalbackend.service.LicenceService
import sg.gov.tech.molbagencyportalbackend.service.LicenceStatusUpdateJobStatistics

@JobController
class CronJobController(
    private val licenceService: LicenceService
) {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    @PostMapping(JobConfig.Endpoint.CRON_UPDATE_LICENCE_STATUS)
    fun updateLicenceStatus(): LicenceStatusUpdateJobStatistics {
        logger.info("Retrieving licence to be updated to Active")

        val activeStatistics = licenceService.updateActiveLicence()
        val expiredStatistics = licenceService.updateExpiredLicence()

        return LicenceStatusUpdateJobStatistics(
            active = activeStatistics,
            expired = expiredStatistics
        )
    }
}
