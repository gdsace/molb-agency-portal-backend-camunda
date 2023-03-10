package sg.gov.tech.molbagencyportalbackend.dto

import org.hibernate.validator.constraints.Range
import sg.gov.tech.molbagencyportalbackend.annotation.ExcludeFromGeneratedCoverageTest
import sg.gov.tech.molbagencyportalbackend.dto.internal.user.ReviewerDTO
import sg.gov.tech.molbagencyportalbackend.exception.ExceptionControllerAdvice
import sg.gov.tech.molbagencyportalbackend.model.RFAStatus
import java.time.LocalDate
import java.time.LocalDateTime
import javax.validation.constraints.Pattern

@ExcludeFromGeneratedCoverageTest
data class ApplicationRFAListDTO(
    val totalCount: Int,
    val applicationRFA: List<ApplicationRFADTO>
)

@ExcludeFromGeneratedCoverageTest
data class ApplicationRFADTO(
    val rfaNo: Int,
    val sendDate: LocalDateTime?,
    val responseDate: LocalDate?,
    val rfaStatus: RFAStatus,
    val agencyMessageToApplicant: String?,
    val internalRemarks: String?,
    val applicantResponse: String?,
    val updatedBy: ReviewerDTO?
)

@ExcludeFromGeneratedCoverageTest
data class ApplicationRFARequestParams(
    @field:Range(min = 0, message = ExceptionControllerAdvice.INVALID_VALUE_MESSAGE)
    val page: Int,
    @field:Pattern(
        regexp = "rfaNo",
        message = ExceptionControllerAdvice.INVALID_VALUE_MESSAGE
    )
    val sortField: String,
    @field:Pattern(regexp = "asc|desc", message = ExceptionControllerAdvice.INVALID_VALUE_MESSAGE)
    val sortOrder: String,
    @field:Range(min = 1, max = 50, message = ExceptionControllerAdvice.INVALID_VALUE_MESSAGE)
    val limit: Int
)

@ExcludeFromGeneratedCoverageTest
data class CancelRFARequestDTO(
    val agencyMessageToApplicant: String?,
    val internalRemarks: String?
)

@ExcludeFromGeneratedCoverageTest
data class CancelRFAResponseDTO(val applicationStatus: String)
