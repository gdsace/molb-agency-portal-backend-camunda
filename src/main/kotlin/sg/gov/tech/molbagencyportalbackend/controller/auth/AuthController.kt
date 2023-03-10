package sg.gov.tech.molbagencyportalbackend.controller.auth

import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import sg.gov.tech.molbagencyportalbackend.dto.wogad.AuthTokenRequestParams
import sg.gov.tech.molbagencyportalbackend.service.AuthService
import javax.servlet.http.HttpServletRequest
import javax.validation.Valid

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService
) {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    @PostMapping("/token")
    fun getAuthToken(@RequestBody @Valid requestParams: AuthTokenRequestParams, request: HttpServletRequest): String {
        logger.info("Retrieving auth token from WOG AD")

        // Get WOGAD Token and generate AP JWT token
        logger.info("Remote address: ${request.remoteAddr}")
        logger.info("X-Forwarded-For: " + request.getHeader("X-Forwarded-For"))
        return authService.getWogadToken(requestParams.code, request.getHeader("X-Forwarded-For"))
    }
}
