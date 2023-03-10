package sg.gov.tech.molbagencyportalbackend.controller.diagnostic

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
@RequestMapping("/diagnostic")
class DiagnosticController {

    @GetMapping("/version")
    fun getVersionInfo() =
        VersionInfoResponse(
            System.getProperty("diagnostic.version", "development")
        )

    @GetMapping("/server-time")
    fun getServerTime(): Long =
        Instant.now().epochSecond
}

data class VersionInfoResponse(val version: String)
