package sg.gov.tech.molbagencyportalbackend.dto.internal

import org.springframework.stereotype.Component
import sg.gov.tech.molbagencyportalbackend.annotation.ExcludeFromGeneratedCoverageTest
import sg.gov.tech.molbagencyportalbackend.dto.ApplicationDetailDTO
import sg.gov.tech.molbagencyportalbackend.dto.ModelTransfer
import sg.gov.tech.molbagencyportalbackend.dto.internal.user.ReviewerModelTransfer
import sg.gov.tech.molbagencyportalbackend.dto.l1t.IdDTO
import sg.gov.tech.molbagencyportalbackend.model.Application
import sg.gov.tech.molbagencyportalbackend.model.ApplicationStatus
import sg.gov.tech.molbagencyportalbackend.service.LicenceService
import sg.gov.tech.molbagencyportalbackend.service.UserService
import sg.gov.tech.molbagencyportalbackend.util.EncryptLicenceDataUtil
import sg.gov.tech.molbagencyportalbackend.util.MaskingUtil.maskLicenceDataField
import sg.gov.tech.utils.maskForDisplay

@Component
@ExcludeFromGeneratedCoverageTest
class ApplicationModelTransfer(
    private val reviewerModelTransfer: ReviewerModelTransfer,
    private val userService: UserService,
    private val licenceService: LicenceService,
    private val encryptLicenceDataUtil: EncryptLicenceDataUtil
) : ModelTransfer<Application, ApplicationDetailDTO> {
    override fun toDTO(model: Application): ApplicationDetailDTO {
        return ApplicationDetailDTO(
            agency = model.agency,
            applicant = model.applicant.copy(
                id = IdDTO(
                    idNumber = model.applicant.id.idNumber?.maskForDisplay(),
                    idType = model.applicant.id.idType
                )
            ),
            applicationNumber = model.applicationNumber,
            applyAs = model.applyAs.value,
            company = model.company,
            filer = model.filer.copy(
                id = IdDTO(
                    idNumber = model.filer.id.idNumber?.maskForDisplay(),
                    idType = model.filer.id.idType
                )
            ),
            formMetaData = model.formMetaData,
            licenceDataFields = encryptLicenceDataUtil.encryptLicenceNode(
                model.licenceDataFields,
                model.formMetaData,
                false
            ).apply {
                maskLicenceDataField(model.formMetaData, String::maskForDisplay)
            },
            licenceName = model.licenceName,
            licenceNumber = licenceService.getByApplicationId(model.id!!)?.licenceNumber,
            loginType = model.loginType,
            submittedDate = model.submittedDate,
            transactionType = model.transactionType,
            updatedBy = when (model.status in ApplicationStatus.getWhitelistRemarkStatuses()) {
                true -> model.updatedBy?.let {
                    userService.getUserById(it.toLong())
                        ?.let { it1 -> reviewerModelTransfer.toDTO(it1) }
                        ?: null
                }

                false -> null
            },
            updatedAt = model.updatedAt,
            reviewer = model.reviewer?.let { reviewerModelTransfer.toDTO(it) },
            status = model.status.value,
            internalRemarks = model.internalRemarks,
            messageToApplicant = model.messageToApplicant
        )
    }
}
