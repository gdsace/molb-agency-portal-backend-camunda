package sg.gov.tech.molbbackend.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties("cors")
data class CorsAllowedHostnamesWhitelistProperties(
    var corsWhitelist: List<String> = emptyList()
)
