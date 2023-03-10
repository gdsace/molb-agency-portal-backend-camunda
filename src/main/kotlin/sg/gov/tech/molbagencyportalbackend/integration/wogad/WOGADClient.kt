package sg.gov.tech.molbagencyportalbackend.integration.wogad

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import sg.gov.tech.molbagencyportalbackend.dto.wogad.WOGADTokenResponse

@FeignClient(
    name = "WOGAD",
    url = "\${integration.wogad.auth-host}",
    path = "\${integration.wogad.tenant-id}/oauth2/v2.0",
    decode404 = true
)
@Profile(value = ["staging", "production"])
@Component
interface WOGADClient {

    @PostMapping("/token")
    fun getAccessToken(@RequestBody request: MultiValueMap<String, Any>): WOGADTokenResponse
}
