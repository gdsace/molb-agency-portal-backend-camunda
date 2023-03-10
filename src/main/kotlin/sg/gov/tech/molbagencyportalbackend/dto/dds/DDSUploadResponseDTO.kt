package sg.gov.tech.molbagencyportalbackend.dto.dds

import com.fasterxml.jackson.annotation.JsonValue
import sg.gov.tech.utils.MolbEnum
import javax.persistence.EnumType
import javax.persistence.Enumerated

data class DDSUploadResponseDTO(
    @Enumerated(EnumType.STRING)
    var documentType: LicenceDocumentType? = null,
    val documentId: String,
    val filename: String
)

enum class LicenceDocumentType(@JsonValue override val value: String) : MolbEnum<String> {
    LICENCE_DOC("LicenceDocument"),
    ADDITIONAL_DOC("AdditionalDocument")
}
