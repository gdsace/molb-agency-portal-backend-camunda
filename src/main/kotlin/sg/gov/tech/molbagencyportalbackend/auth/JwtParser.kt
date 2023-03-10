package sg.gov.tech.molbagencyportalbackend.auth

import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.impl.DefaultJwtParser
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import sg.gov.tech.molbagencyportalbackend.exception.AuthException

@Component
class JwtParser {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    fun getClaimsFromUnverifiedToken(token: String): Claims {
        val splitToken = token.split(".")
        val unsignedToken = splitToken[0] + "." + splitToken[1] + "."

        try {
            val parser = DefaultJwtParser()
            val jwt = parser.parse(unsignedToken)

            return jwt.body as Claims
        } catch (e: JwtException) {
            logger.debug("Failed to resolve token: $token", e)
            throw AuthException(e)
        }
    }
}
