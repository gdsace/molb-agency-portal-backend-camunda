package sg.gov.tech.molbagencyportalbackend.dto.l1t

data class L1TMultipleDocumentRequest(
    val projectCode: String,
    val files: List<L1TDocument>,
    val downloadFileName: String
)

data class L1TDocument(
    val objectName: String,
    val customFileName: String?
)
