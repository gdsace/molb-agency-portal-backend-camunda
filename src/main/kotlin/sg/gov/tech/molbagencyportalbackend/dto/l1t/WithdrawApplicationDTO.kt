package sg.gov.tech.molbagencyportalbackend.dto.l1t

import sg.gov.tech.molbagencyportalbackend.annotation.ExcludeFromGeneratedCoverageTest
import sg.gov.tech.molbagencyportalbackend.validator.LicenceTypeExists
import javax.validation.Valid
import javax.validation.constraints.NotBlank

@ExcludeFromGeneratedCoverageTest
data class WithdrawApplicationDTO(
    @field:NotBlank(message = "operation is missing or empty")
    val operation: String,
    @field:Valid
    val application: ApplicationDTO
) {

    @ExcludeFromGeneratedCoverageTest
    data class ApplicationDTO(
        @field:Valid
        val general: GeneralDTO
    )

    @ExcludeFromGeneratedCoverageTest
    data class GeneralDTO(
        @field:NotBlank(message = "applicationNumber value invalid")
        val applicationNumber: String,
        val dateSent: String,
        val licenceName: String,
        @field:LicenceTypeExists
        val licenceID: String
    )
}
