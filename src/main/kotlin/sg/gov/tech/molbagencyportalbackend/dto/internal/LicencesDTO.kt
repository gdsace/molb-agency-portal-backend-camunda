package sg.gov.tech.molbagencyportalbackend.dto.internal

import org.hibernate.validator.constraints.Range
import org.springframework.stereotype.Component
import sg.gov.tech.molbagencyportalbackend.annotation.ExcludeFromGeneratedCoverageTest
import sg.gov.tech.molbagencyportalbackend.dto.ModelTransfer
import sg.gov.tech.molbagencyportalbackend.exception.ExceptionControllerAdvice
import sg.gov.tech.molbagencyportalbackend.model.Licence
import sg.gov.tech.molbagencyportalbackend.model.LicenceStatus
import sg.gov.tech.utils.maskForDisplay
import java.time.LocalDate
import java.time.LocalDateTime
import javax.validation.constraints.Pattern

@ExcludeFromGeneratedCoverageTest
data class LicenceStatisticsDTO(
    val agencyLicenceCount: Int,
    val licenceCount: Int,
    val licences: List<DashboardLicencesDTO>
)

@ExcludeFromGeneratedCoverageTest
data class DashboardLicencesDTO(
    val licenceNumber: String,
    val agencyCode: String,
    val licenceName: String,
    val licenceStatus: LicenceStatus,
    val updatedAt: LocalDateTime,
    val issueDate: LocalDate,
    val expiryDate: LocalDate?,
    val licenceHolderId: String,
    val licenceHolderName: String
)

@ExcludeFromGeneratedCoverageTest
data class AgencyLicencesRequestParams(
    @field:Pattern(regexp = "otherLicences|agencyLicences", message = ExceptionControllerAdvice.INVALID_VALUE_MESSAGE)
    val tab: String,
    @field:Range(min = 0, message = ExceptionControllerAdvice.INVALID_VALUE_MESSAGE)
    val page: Int,
    @field:Pattern(
        regexp = "licenceNumber|licenceName|status|updatedAt|issueDate|expiryDate" +
            "|application.applicantName|licenceType.agency.code",
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
class DashboardLicenceTransfer : ModelTransfer<Licence, DashboardLicencesDTO> {
    override fun toDTO(model: Licence): DashboardLicencesDTO = DashboardLicencesDTO(
        licenceNumber = model.licenceNumber,
        agencyCode = model.licenceType.agency.code,
        licenceName = model.licenceName,
        licenceStatus = model.status,
        updatedAt = model.updatedAt!!,
        issueDate = model.issueDate,
        expiryDate = model.expiryDate,
        licenceHolderId = (
            model.application?.company?.uen?.let {
                "UEN: " + model.application?.company?.uen
            } ?: "NRIC: " + model.application?.applicant?.id?.idNumber?.maskForDisplay()
            ).toString(),
        licenceHolderName = model.application?.applicantName.toString()
    )
}
