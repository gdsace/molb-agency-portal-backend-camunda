package sg.gov.tech.molbagencyportalbackend.integration.job

import sg.gov.tech.molbagencyportalbackend.annotation.ExcludeFromGeneratedCoverageTest
import sg.gov.tech.molbagencyportalbackend.dto.l1t.L1TStatusPushRequestDTO

sealed class JobPayload

@ExcludeFromGeneratedCoverageTest
data class L1TUpdateStatusPayload(
    val referenceNumber: String,
    val l1tStatusPushRequest: L1TStatusPushRequestDTO
) : JobPayload()
