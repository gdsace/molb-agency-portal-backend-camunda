package sg.gov.tech.molbagencyportalbackend.dto.internal

import sg.gov.tech.molbagencyportalbackend.exception.ExceptionControllerAdvice
import sg.gov.tech.molbagencyportalbackend.validator.ValidApproveRequest
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

@ValidApproveRequest
data class ApproveApplicationRequestDTO(
    @field:Pattern(regexp = "Approved|Partially Approved", message = ExceptionControllerAdvice.INVALID_VALUE_MESSAGE)
    val approvalType: String,
    @field:Size(max = 20, message = ExceptionControllerAdvice.INVALID_VALUE_MESSAGE)
    @field:Pattern(regexp = "[0-9A-Za-z\\-/]+", message = ExceptionControllerAdvice.INVALID_VALUE_MESSAGE)
    val licenceNumber: String,
    @field:Pattern(regexp = "uploadLicence|noLicence", message = ExceptionControllerAdvice.INVALID_VALUE_MESSAGE)
    val licenceIssuanceType: String,
    val issueDate: String,
    val startDate: String,
    val expiryDate: String?,
    val agencyMessageToApplicant: String?,
    val internalRemarks: String?
)
