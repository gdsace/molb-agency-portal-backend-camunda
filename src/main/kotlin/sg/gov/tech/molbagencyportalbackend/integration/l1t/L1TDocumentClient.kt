package sg.gov.tech.molbagencyportalbackend.integration.l1t

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.cloud.openfeign.SpringQueryMap
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import sg.gov.tech.molbagencyportalbackend.dto.l1t.L1TMultipleDocumentRequest
import sg.gov.tech.molbagencyportalbackend.dto.l1t.L1TSingleDocumentRequest

@FeignClient(
    name = "L1T-file-upload",
    url = "\${integration.l1t.file-upload-host}",
    decode404 = true
)
@Component
interface L1TDocumentClient {

    @GetMapping("/file-download")
    fun downloadSingleFile(@SpringQueryMap request: L1TSingleDocumentRequest): ResponseEntity<ByteArray>

    @PostMapping("/file-downloads")
    fun downloadMultipleFiles(@RequestBody request: L1TMultipleDocumentRequest): ResponseEntity<ByteArray>
}
