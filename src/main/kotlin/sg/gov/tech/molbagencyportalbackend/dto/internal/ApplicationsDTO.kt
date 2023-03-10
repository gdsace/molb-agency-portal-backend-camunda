package sg.gov.tech.molbagencyportalbackend.dto.internal

import org.hibernate.validator.constraints.Range
import org.springframework.stereotype.Component
import sg.gov.tech.molbagencyportalbackend.annotation.ExcludeFromGeneratedCoverageTest
import sg.gov.tech.molbagencyportalbackend.dto.ModelTransfer
import sg.gov.tech.molbagencyportalbackend.dto.internal.user.ReviewerDTO
import sg.gov.tech.molbagencyportalbackend.dto.internal.user.ReviewerModelTransfer
import sg.gov.tech.molbagencyportalbackend.exception.ExceptionControllerAdvice
import sg.gov.tech.molbagencyportalbackend.model.Application
import sg.gov.tech.molbagencyportalbackend.model.ApplicationStatus
import sg.gov.tech.utils.maskForDisplay
import java.time.LocalDateTime
import javax.validation.constraints.Pattern

@ExcludeFromGeneratedCoverageTest
data class ApplicationsDTO(
    val totalCount: Long,
    val data: List<AgencyApplicationsDTO>
)

@ExcludeFromGeneratedCoverageTest
data class AgencyApplicationsDTO(
    val id: Long,
    val applicationNumber: String,
    val licenceName: String,
    val updatedAt: LocalDateTime,
    val submittedDate: LocalDateTime,
    val status: ApplicationStatus,
    val transactionType: String,
    val applicant: AgencyApplicationsApplicantDTO,
    val applicantName: String,
    val reviewer: ReviewerDTO?,
    val caseStatus: String
)

@ExcludeFromGeneratedCoverageTest
data class AgencyApplicationsApplicantDTO(
    val idType: String?,
    val idNumber: String?
)

@ExcludeFromGeneratedCoverageTest
data class AgencyApplicationsRequestParams(
    @field:Range(min = 0, message = ExceptionControllerAdvice.INVALID_VALUE_MESSAGE)
    val page: Int,
    @field:Pattern(
        regexp = "applicationNumber|licenceName|status|updatedAt|submittedDate" +
            "|transactionType|applicantName|officerName|caseStatus",
        message = ExceptionControllerAdvice.INVALID_VALUE_MESSAGE
    )
    val sortField: String,
    @field:Pattern(regexp = "asc|desc", message = ExceptionControllerAdvice.INVALID_VALUE_MESSAGE)
    val sortOrder: String,
    @field:Range(min = 1, max = 50, message = ExceptionControllerAdvice.INVALID_VALUE_MESSAGE)
    val limit: Int
)

@Component
@ExcludeFromGeneratedCoverageTest
class AgencyApplicationTransfer(private val reviewerModelTransfer: ReviewerModelTransfer) :
    ModelTransfer<Application, AgencyApplicationsDTO> {
    override fun toDTO(model: Application): AgencyApplicationsDTO = AgencyApplicationsDTO(
        id = model.id!!,
        applicationNumber = model.applicationNumber,
        licenceName = model.licenceName,
        updatedAt = model.updatedAt!!,
        submittedDate = model.submittedDate,
        status = model.status,
        transactionType = model.transactionType,
        applicant = AgencyApplicationsApplicantDTO(
            idType = model.company.uen?.let { "UEN" } ?: model.applicant.id.idType,
            idNumber = model.company.uen?.let { model.company.uen } ?: model.applicant.id.idNumber?.maskForDisplay()
        ),
        applicantName = model.applicantName,
        reviewer = model.reviewer?.let { reviewerModelTransfer.toDTO(it) },
        caseStatus = model.caseStatus
    )
}
