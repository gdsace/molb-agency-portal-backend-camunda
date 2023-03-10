package sg.gov.tech.molbagencyportalbackend.dto.internal

import sg.gov.tech.molbagencyportalbackend.annotation.ExcludeFromGeneratedCoverageTest
import sg.gov.tech.molbagencyportalbackend.exception.ExceptionControllerAdvice
import javax.validation.constraints.Pattern

@ExcludeFromGeneratedCoverageTest
data class WithdrawApplicationRequestDTO(
    @field:Pattern(regexp = "Approve|Reject", message = ExceptionControllerAdvice.INVALID_VALUE_MESSAGE)
    val requestType: String,
    val agencyMessageToApplicant: String?,
    val internalRemarks: String?
)

@ExcludeFromGeneratedCoverageTest
data class WithdrawApplicationResponseDTO(val applicationStatus: String)
