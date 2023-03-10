package sg.gov.tech.molbagencyportalbackend.configuration

import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Configuration
class SecurityHeadersConfiguration {

    @Bean
    fun securityHeadersFilterRegistrationBean(): FilterRegistrationBean<*> {
        val registrationBean = FilterRegistrationBean<Filter>()
        registrationBean.filter = SecurityHeadersFilter()
        registrationBean.addUrlPatterns("/*")
        return registrationBean
    }
}

class SecurityHeadersFilter : OncePerRequestFilter() {
    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        response.addHeader("Referrer-Policy", "strict-origin-when-cross-origin")
        response.addHeader("X-XSS-Protection", "0")
        response.addHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains")
        response.addHeader("Content-Security-Policy", "default-src 'none'")
        filterChain.doFilter(request, response)
    }
}
