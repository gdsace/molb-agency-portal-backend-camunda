package sg.gov.tech.molbagencyportalbackend.dto.l1t

import com.fasterxml.jackson.databind.JsonNode
import sg.gov.tech.audit.MaskingConverter
import sg.gov.tech.molbagencyportalbackend.annotation.ExcludeFromGeneratedCoverageTest
import sg.gov.tech.molbagencyportalbackend.validator.LicenceTypeExists
import sg.gov.tech.molbagencyportalbackend.validator.ValidApplication
import javax.persistence.Convert
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Pattern

@ExcludeFromGeneratedCoverageTest
@ValidApplication
data class CreateApplicationRequestDTO(
    @field:NotBlank(message = "operation is missing or empty")
    val operation: String,
    @field:Valid
    val application: ApplicationDTO,
    @field:Valid
    val payment: PaymentDTO?,
    val version: VersionDTO
)

@ExcludeFromGeneratedCoverageTest
data class ApplicationDTO(
    @field:Valid
    val general: GeneralDTO,
    @field:Valid
    val profile: ProfileDTO,
    @field:Valid
    val applicant: ApplicantDTO,
    val company: CompanyDTO,
    @field:Valid
    val filer: FilerDTO,
    val licence: JsonNode,
) {
    fun getRemarks(): String? =
        licence.get("remarks")?.get("remarks")?.asText()
}

@ExcludeFromGeneratedCoverageTest
data class PaymentDTO(
    val paymentAmount: String,
    val paymentDate: String,
    val paymentMode: String,
    val receiptNumber: String,
    val transactionNumber: String
)

@ExcludeFromGeneratedCoverageTest
data class ApplicantDTO(
    val salutation: String,
    val name: String,
    val id: IdDTO,
    val email: String,
    val contactNumber: String,
    val address: AddressDTO?,
)

@ExcludeFromGeneratedCoverageTest
data class CompanyDTO(
    val companyName: String?,
    val uen: String?,
    val entityType: String?,
    val registeredAddress: AddressDTO?,
)

@ExcludeFromGeneratedCoverageTest
data class FilerDTO(
    val contactNumber: String?,
    val email: String?,
    @field:Valid
    val id: IdDTO,
    val name: String?,
    val salutation: String?
)

@ExcludeFromGeneratedCoverageTest
data class GeneralDTO(
    @field:NotBlank(message = "applicationNumber value invalid")
    val applicationNumber: String,
    val dateSent: String,
    val licenceName: String,
    @field:LicenceTypeExists
    val licenceID: String,
    @field:Pattern(regexp = "new|amend|renew|cancel", message = "transactionType value is invalid")
    val transactionType: String
)

@ExcludeFromGeneratedCoverageTest
data class ProfileDTO(
    @field:Pattern(
        regexp = "As an applicant|On behalf of applicant",
        message = "applyAs value is invalid"
    )
    val applyAs: String
)

@ExcludeFromGeneratedCoverageTest
data class AddressDTO(
    val blockNo: String?,
    val buildingName: String?,
    val floor: String?,
    val postalCode: String?,
    val streetName: String?,
    val unit: String?
)

@ExcludeFromGeneratedCoverageTest
data class IdDTO(
    // filer will be "" for when applying as applicant
    @field:Pattern(regexp = "NRIC|FIN|Passport|", message = "idType invalid")
    val idType: String?,
    @Convert(converter = MaskingConverter::class)
    val idNumber: String?,
)

data class VersionDTO(
    val agencyCode: String,
    val agencyLicenceType: String,
    val agencyName: String,
    val agencyOperationType: String,
    val formName: String,
    val id: String,
    val licenceName: String,
    val operationTypeId: String,
    var schema: Map<String, Any>,
    val settings: Map<String, Any>,
    val status: String
)
