package sg.gov.tech.molbagencyportalbackend.dto.internal

import sg.gov.tech.molbagencyportalbackend.annotation.ExcludeFromGeneratedCoverageTest

@ExcludeFromGeneratedCoverageTest
data class RejectMessagesDTO(
    val internalRemarks: String?,
    val messageToApplicant: String?
)
