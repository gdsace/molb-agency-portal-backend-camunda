package sg.gov.tech.molbagencyportalbackend.dto

import sg.gov.tech.molbagencyportalbackend.annotation.ExcludeFromGeneratedCoverageTest
import sg.gov.tech.molbagencyportalbackend.dto.dds.DDSUploadResponseDTO
import sg.gov.tech.molbagencyportalbackend.dto.internal.user.ReviewerDTO
import sg.gov.tech.molbagencyportalbackend.dto.l1t.AddressDTO
import sg.gov.tech.molbagencyportalbackend.dto.l1t.IdDTO
import sg.gov.tech.molbagencyportalbackend.exception.ExceptionControllerAdvice
import sg.gov.tech.molbagencyportalbackend.model.Agency
import sg.gov.tech.molbagencyportalbackend.model.ApplicationStatus
import java.time.LocalDate
import java.time.LocalDateTime
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

@ExcludeFromGeneratedCoverageTest
data class LicenceApplicationsDTO(
    val applicationNumber: String,
    val status: ApplicationStatus,
    val transactionType: String,
    val updatedAt: LocalDateTime?,
    val submittedDate: LocalDateTime
)

@ExcludeFromGeneratedCoverageTest
data class LicenceApplicantDTO(
    val name: String,
    val id: IdDTO,
    val address: AddressDTO?,
)

@ExcludeFromGeneratedCoverageTest
data class LicenceCompanyDTO(
    val companyName: String?,
    val uen: String?,
    val registeredAddress: AddressDTO?,
)

@ExcludeFromGeneratedCoverageTest
data class LicenceDetailDTO(
    val applications: List<LicenceApplicationsDTO>?,
    val applicant: LicenceApplicantDTO?,
    val agency: Agency?,
    val company: LicenceCompanyDTO?,
    val licenceNumber: String,
    val licenceName: String,
    val loginType: String,
    val uen: String?,
    val nric: String?,
    val status: String,
    val licenceDocuments: List<DDSUploadResponseDTO>?,
    val issueDate: LocalDate,
    val startDate: LocalDate,
    val expiryDate: LocalDate?,
    val updatedBy: ReviewerDTO?,
    val updatedAt: LocalDateTime?,
    val licenceIssuanceType: String,
)

@ExcludeFromGeneratedCoverageTest
data class AgencyLicenceDetailRequestParams(
    @field:Size(max = 20, message = ExceptionControllerAdvice.INVALID_VALUE_MESSAGE)
    @field:Pattern(regexp = "[0-9A-Za-z/\\-]+", message = ExceptionControllerAdvice.INVALID_VALUE_MESSAGE)
    val licenceNumber: String
)

@ExcludeFromGeneratedCoverageTest
data class AgencyLicenceDocumentsRequestParams(
    @field:Size(max = 20, message = ExceptionControllerAdvice.INVALID_VALUE_MESSAGE)
    @field:Pattern(regexp = "[0-9A-Za-z/\\-]+", message = ExceptionControllerAdvice.INVALID_VALUE_MESSAGE)
    val licenceNumber: String,
    @field:NotEmpty(message = ExceptionControllerAdvice.INVALID_VALUE_MESSAGE)
    val documentId: String
)
