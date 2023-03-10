package sg.gov.tech.molbagencyportalbackend.integration.l1t

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import sg.gov.tech.molbagencyportalbackend.annotation.ExcludeFromGeneratedCoverageTest
import sg.gov.tech.molbagencyportalbackend.dto.l1t.L1TStatusPushRequestDTO
import sg.gov.tech.molbagencyportalbackend.dto.l1t.L1TStatusPushResponseDTO

@FeignClient(
    name = "L1T-status-push",
    url = "\${integration.l1t.api-host}",
    decode404 = true
)
@Component
@ExcludeFromGeneratedCoverageTest
interface L1TStatusPushClient {

    @PostMapping("/update-status")
    fun updateStatus(
        @RequestHeader headers: Map<String, String>,
        @RequestBody request: L1TStatusPushRequestDTO
    ): L1TStatusPushResponseDTO
}
