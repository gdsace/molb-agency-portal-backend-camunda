package sg.gov.tech.molbagencyportalbackend.util

import org.springframework.stereotype.Component
import org.springframework.web.util.ContentCachingRequestWrapper
import sg.gov.tech.molbagencyportalbackend.annotation.ExcludeFromGeneratedCoverageTest
import java.io.IOException
import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest

@Component
@ExcludeFromGeneratedCoverageTest
class RequestFilter : Filter {
    @Throws(IOException::class, ServletException::class)
    override fun doFilter(servletRequest: ServletRequest, servletResponse: ServletResponse, filterChain: FilterChain) {
        val contentCachingRequestWrapper = ContentCachingRequestWrapper(
            servletRequest as HttpServletRequest
        )
        filterChain.doFilter(contentCachingRequestWrapper, servletResponse)
    }
}
