package sg.gov.tech.molbagencyportalbackend.dto.internal

import org.springframework.stereotype.Component
import sg.gov.tech.molbagencyportalbackend.annotation.ExcludeFromGeneratedCoverageTest
import sg.gov.tech.molbagencyportalbackend.dto.LicenceApplicantDTO
import sg.gov.tech.molbagencyportalbackend.dto.LicenceApplicationsDTO
import sg.gov.tech.molbagencyportalbackend.dto.LicenceCompanyDTO
import sg.gov.tech.molbagencyportalbackend.dto.LicenceDetailDTO
import sg.gov.tech.molbagencyportalbackend.dto.ModelTransfer
import sg.gov.tech.molbagencyportalbackend.dto.internal.user.ReviewerModelTransfer
import sg.gov.tech.molbagencyportalbackend.dto.l1t.IdDTO
import sg.gov.tech.molbagencyportalbackend.model.Licence
import sg.gov.tech.molbagencyportalbackend.service.UserService
import sg.gov.tech.utils.maskForDisplay

@Component
@ExcludeFromGeneratedCoverageTest
class LicenceModelTransfer(
    private val reviewerModelTransfer: ReviewerModelTransfer,
    private val userService: UserService
) : ModelTransfer<Licence, LicenceDetailDTO> {
    override fun toDTO(model: Licence): LicenceDetailDTO = model.toLicenceDTO(showApplications = true)

    fun toDetailDTO(model: Licence, showApplications: Boolean): LicenceDetailDTO =
        model.toLicenceDTO(showApplications = showApplications)

    fun Licence.toLicenceDTO(showApplications: Boolean): LicenceDetailDTO {
        val applicationList = mutableListOf<LicenceApplicationsDTO>()
        application?.let {
            applicationList.add(
                LicenceApplicationsDTO(
                    applicationNumber = it.applicationNumber,
                    status = it.status,
                    transactionType = it.transactionType,
                    updatedAt = it.updatedAt,
                    submittedDate = it.submittedDate
                )
            )
        }
        return LicenceDetailDTO(
            applications = if (showApplications) applicationList else null,
            applicant = application?.applicant?.let {
                LicenceApplicantDTO(
                    name = it.name,
                    id = IdDTO(
                        idType = it.id.idType,
                        idNumber = it.id.idNumber?.maskForDisplay()
                    ),
                    address = it.address
                )
            },
            agency = application?.agency,
            company = application?.company?.let {
                LicenceCompanyDTO(
                    companyName = it.companyName,
                    uen = it.uen,
                    registeredAddress = it.registeredAddress
                )
            },
            licenceNumber = licenceNumber,
            licenceName = licenceName,
            loginType = loginType,
            uen = uen,
            nric = nric.maskForDisplay(),
            status = status.value,
            licenceDocuments = licenceDocuments,
            issueDate = issueDate,
            startDate = startDate,
            expiryDate = expiryDate,
            updatedBy = updatedBy?.let {
                userService.getUserById(it.toLong())
                    ?.let { it1 -> reviewerModelTransfer.toDTO(it1) }
            },
            updatedAt = updatedAt,
            licenceIssuanceType = licenceIssuanceType.value
        )
    }
}
