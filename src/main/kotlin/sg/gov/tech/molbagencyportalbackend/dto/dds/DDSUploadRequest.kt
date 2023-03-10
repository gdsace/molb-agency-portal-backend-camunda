package sg.gov.tech.molbagencyportalbackend.dto.dds

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class DDSUploadRequest(
    val domainReferenceId: String,
    val domain: String,
    val agency: String,
    val owners: List<Owners?>
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Owners(
    val uinfin: String,
    val uen: String?
)
