package sg.gov.tech.molbagencyportalbackend.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.filter.OncePerRequestFilter
import sg.gov.tech.logging.RequestLoggingFilter
import sg.gov.tech.molbagencyportalbackend.annotation.ExcludeFromGeneratedCoverageTest

@Configuration
@ExcludeFromGeneratedCoverageTest
class RequestLoggingFilterConfiguration {
    @Bean
    fun requestLoggingFilter(): OncePerRequestFilter {
        return RequestLoggingFilter(
            includeQueryString = true,
            includeHeaders = true,
            includeRequestPayload = true,
            includeResponsePayload = true
        )
    }
}
