package sg.gov.tech.molbagencyportalbackend.auth

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import sg.gov.tech.molbagencyportalbackend.annotation.ExcludeFromGeneratedCoverageTest

@Component
@ExcludeFromGeneratedCoverageTest
class AuthenticationFacade {
    fun getPrincipalName(): String {
        return SecurityContextHolder.getContext().authentication.name
    }
}
