package sg.gov.tech.molbagencyportalbackend.controller.api

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import sg.gov.tech.molbagencyportalbackend.service.AuthService

@RestController
@RequestMapping("/api")
class ApiAuthController(private val service: AuthService) {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/logout")
    fun logout(authentication: Authentication) {
        logger.info("Logging out user")
        service.logout((authentication.principal as OAuth2AuthenticatedPrincipal).name)
    }

    @PostMapping("/refresh")
    fun refresh(authentication: Authentication): String {
        logger.info("Refreshing tokens")
        return service.refresh((authentication.principal as OAuth2AuthenticatedPrincipal).attributes)
    }
}
