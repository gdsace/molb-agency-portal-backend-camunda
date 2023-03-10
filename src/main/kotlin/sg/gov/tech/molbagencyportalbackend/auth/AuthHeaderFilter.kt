package sg.gov.tech.molbagencyportalbackend.auth

import org.springframework.web.filter.GenericFilterBean
import sg.gov.tech.auth.AuthorizationHeaderInfo
import sg.gov.tech.molbagencyportalbackend.configuration.SignatureVerificationConfiguration
import sg.gov.tech.security.service.AuthorizationHeaderVerifier
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class AuthHeaderFilter(
    private val signatureVerificationConfiguration: SignatureVerificationConfiguration
) : GenericFilterBean() {

    companion object {
        private const val AUTHORIZATION_HEADER_KEY = "authorization"
    }

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val httpServletRequest: HttpServletRequest = request as HttpServletRequest
        val authHeader = httpServletRequest.getHeader(AUTHORIZATION_HEADER_KEY)
        try {
            verifyHeader(authHeader)
            chain.doFilter(request, response)
        } catch (ex: Exception) {
            val httpServletResponse = response as HttpServletResponse
            val error = "Invalid Authorization Header"

            httpServletResponse.reset()
            httpServletResponse.status = HttpServletResponse.SC_UNAUTHORIZED
            httpServletResponse.setContentLength(error.length)
            httpServletResponse.writer.write(error)
        }
    }

    private fun verifyHeader(header: String): AuthorizationHeaderInfo {
        val authorizationHeaderVerifier = AuthorizationHeaderVerifier()
        val authorizationHeaderInfo: AuthorizationHeaderInfo = authorizationHeaderVerifier.parseHeader(header)
        val domainCredentials =
            signatureVerificationConfiguration.domainCredentialMap.getValue(authorizationHeaderInfo.appId)

        return authorizationHeaderVerifier.verifyHeader(
            authorizationHeaderInfo,
            domainCredentials.authorizationHeaderValidationCert
        )
    }
}
