package sg.gov.tech.molbagencyportalbackend.integration.job

import sg.gov.tech.molbagencyportalbackend.annotation.ExcludeFromGeneratedCoverageTest

@ExcludeFromGeneratedCoverageTest
object JobConfig {
    const val PATH_PREFIX = "/job"

    object Endpoint {
        const val L1T_UPDATE_STATUS = "$PATH_PREFIX/l1t/update-status"
        const val CRON_UPDATE_LICENCE_STATUS = "$PATH_PREFIX/cron/update-licence-status"
    }
}
