package sg.gov.tech.molbagencyportalbackend.fixture

import sg.gov.tech.molbagencyportalbackend.dto.LicenceApplicantDTO
import sg.gov.tech.molbagencyportalbackend.dto.LicenceApplicationsDTO
import sg.gov.tech.molbagencyportalbackend.dto.LicenceCompanyDTO
import sg.gov.tech.molbagencyportalbackend.dto.LicenceDetailDTO
import sg.gov.tech.molbagencyportalbackend.dto.dds.DDSUploadResponseDTO
import sg.gov.tech.molbagencyportalbackend.dto.dds.LicenceDocumentType
import sg.gov.tech.molbagencyportalbackend.dto.l1t.AddressDTO
import sg.gov.tech.molbagencyportalbackend.dto.l1t.IdDTO
import java.time.LocalDateTime

class LicenceDocumentFixture private constructor() {
    companion object {
        fun document(): DDSUploadResponseDTO {
            return DDSUploadResponseDTO(
                documentType = LicenceDocumentType.LICENCE_DOC,
                documentId = "041e2a9e-2b7e-4f30-b8fd-e8cceabaa42f",
                filename = "sample.pdf"
            )
        }
    }
}

object LicenceApplicationFixture {
    val getApplication = LicenceApplicationsDTO(
        applicationNumber = ApplicationFixture.createApplication_SingpassSelf.applicationNumber,
        updatedAt = LocalDateTime.now(),
        submittedDate = ApplicationFixture.createApplication_SingpassSelf.submittedDate,
        status = ApplicationFixture.createApplication_SingpassSelf.status,
        transactionType = ApplicationFixture.createApplication_SingpassSelf.transactionType,
    )
}

object LicenceDetailDTOFixture {
    val getLicence = LicenceDetailDTO(
        applications = listOf(LicenceApplicationFixture.getApplication),
        applicant = LicenceApplicantDTO(
            name = "John",
            id = IdDTO(
                idType = "NRIC",
                idNumber = "SX  XXXXXXA"
            ),
            address = AddressDTO(
                postalCode = "117438",
                blockNo = "10",
                streetName = "Pasir Panjang Road",
                buildingName = "Mapletree Business City",
                floor = "10",
                unit = ""
            )
        ),
        agency = AgencyFixture.agency,
        company = LicenceCompanyDTO(
            companyName = null,
            uen = null,
            registeredAddress = null
        ),
        licenceNumber = LicenceModelFixture.licence.licenceNumber,
        licenceName = LicenceModelFixture.licence.licenceName,
        loginType = LicenceModelFixture.licence.loginType,
        uen = LicenceModelFixture.licence.uen,
        nric = LicenceModelFixture.licence.nric,
        status = LicenceModelFixture.licence.status.value,
        licenceDocuments = listOf(LicenceDocumentFixture.document()),
        issueDate = LicenceModelFixture.licence.issueDate,
        startDate = LicenceModelFixture.licence.startDate,
        expiryDate = LicenceModelFixture.licence.expiryDate,
        updatedBy = ReviewerDTOFixture.reviewerDTOA,
        updatedAt = LicenceModelFixture.licence.updatedAt,
        licenceIssuanceType = LicenceModelFixture.licence.licenceIssuanceType.value
    )
    val getLicenceWoApplication = LicenceDetailDTO(
        applications = null,
        applicant = null,
        company = null,
        agency = null,
        licenceNumber = LicenceModelFixture.licence.licenceNumber,
        licenceName = LicenceModelFixture.licence.licenceName,
        loginType = LicenceModelFixture.licence.loginType,
        uen = LicenceModelFixture.licence.uen,
        nric = LicenceModelFixture.licence.nric,
        status = LicenceModelFixture.licence.status.value,
        licenceDocuments = listOf(LicenceDocumentFixture.document()),
        issueDate = LicenceModelFixture.licence.issueDate,
        startDate = LicenceModelFixture.licence.startDate,
        expiryDate = LicenceModelFixture.licence.expiryDate,
        updatedBy = ReviewerDTOFixture.reviewerDTOA,
        updatedAt = LicenceModelFixture.licence.updatedAt,
        licenceIssuanceType = LicenceModelFixture.licence.licenceIssuanceType.value
    )
}
