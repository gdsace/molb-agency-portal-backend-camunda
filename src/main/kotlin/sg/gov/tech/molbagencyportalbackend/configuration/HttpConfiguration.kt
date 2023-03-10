package sg.gov.tech.molbagencyportalbackend.configuration

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.data.web.config.EnableSpringDataWebSupport
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import sg.gov.tech.molbbackend.configuration.CorsAllowedHostnamesWhitelistProperties

@Configuration
@EnableSpringDataWebSupport
class HttpConfiguration : WebMvcConfigurer {

    @Autowired
    private lateinit var corsAllowedHostnamesWhitelist: CorsAllowedHostnamesWhitelistProperties

    override fun addCorsMappings(registry: CorsRegistry) {
        registry
            .addMapping("/api/**")
            .allowedOrigins(*corsAllowedHostnamesWhitelist.corsWhitelist.toTypedArray())
            .exposedHeaders("Content-Disposition")
            .allowCredentials(true)
            .allowedMethods("PUT", "PATCH", "POST", "GET", "DELETE", "OPTIONS")

        registry
            .addMapping("/auth/**")
            .allowedOrigins(*corsAllowedHostnamesWhitelist.corsWhitelist.toTypedArray())
            .exposedHeaders("Content-Disposition")
            .allowCredentials(true)
            .allowedMethods("POST", "GET", "OPTIONS")

        registry
            .addMapping("/diagnostic/**")
            .allowedOrigins(*corsAllowedHostnamesWhitelist.corsWhitelist.toTypedArray())
            .exposedHeaders("Content-Disposition")
            .allowCredentials(true)
            .allowedMethods("GET", "OPTIONS")
    }
}
