package sg.gov.tech.molbagencyportalbackend.dto.l1t

data class L1TSingleDocumentRequest(
    val projectCode: String,
    val objectName: String,
    val downloadFileName: String
)
