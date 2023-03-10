package sg.gov.tech.molbagencyportalbackend.integration.dds

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.multipart.MultipartFile
import sg.gov.tech.molbagencyportalbackend.configuration.MultipartSupportConfig
import sg.gov.tech.molbagencyportalbackend.dto.dds.DDSUploadRequest
import sg.gov.tech.molbagencyportalbackend.dto.dds.DDSUploadResponseDTO

@FeignClient(
    name = "DDS-file-upload",
    url = "\${integration.dashboard-document-service.file-api-host}",
    configuration = [MultipartSupportConfig::class],
    decode404 = true
)
@Component
interface DDSDocumentClient {

    @PostMapping(
        "/v2/documents",
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_JSON_VALUE]
    )
    fun uploadDocument(
        @RequestHeader headers: Map<String, String>,
        @RequestPart(value = "file", required = true) file: MultipartFile,
        @RequestPart(value = "metadata", required = true) metadata: DDSUploadRequest
    ): ResponseEntity<DDSUploadResponseDTO>

    @GetMapping("/documents")
    fun downloadDocument(
        @RequestHeader headers: Map<String, String?>,
        @RequestParam(value = "ids[]") documentId: String,
        @RequestParam(value = "domainReferenceId") domainReferenceId: String
    ): ResponseEntity<String>
}
