package sg.gov.tech.molbagencyportalbackend.auth

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal
import org.springframework.security.oauth2.core.OAuth2TokenIntrospectionClaimNames
import org.springframework.security.oauth2.server.resource.introspection.BadOpaqueTokenException
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector
import org.springframework.stereotype.Component
import sg.gov.tech.molbagencyportalbackend.annotation.ExcludeFromGeneratedCoverageTest
import sg.gov.tech.molbagencyportalbackend.dao.JwtDao
import sg.gov.tech.molbagencyportalbackend.service.UserService
import sg.gov.tech.molbagencyportalbackend.util.FormatterUtil

@Component
@ExcludeFromGeneratedCoverageTest
class JwtOpaqueTokenIntrospector(
    @Value("\${session.ap-token-secret}") private val secret: String,
    private val jwtDao: JwtDao,
    private val userService: UserService
) : OpaqueTokenIntrospector {

    override fun introspect(token: String): OAuth2AuthenticatedPrincipal = try {
        Jwts.parser()
            .setSigningKey(FormatterUtil.encodeStringToBase64(secret))
            .parseClaimsJws(token)
    } catch (e: JwtException) {
        throw BadOpaqueTokenException("Failed to parse JWT", e)
    }.let {
        if (!jwtDao.contains(it.getBody().getSubject(), it.getBody().getId())) {
            throw BadOpaqueTokenException("Failed to find JWT")
        }
        // seems like below line is not necessarily
        userService.getAndValidateUserByEmail(it.body.subject)
        // got to triple check and see if there are other implications
        // if we are planning to remove
        JwtOAuth2AuthenticatedPrincipal(it)
    }

    class JwtOAuth2AuthenticatedPrincipal(
        private val jws: Jws<Claims>
    ) : OAuth2AuthenticatedPrincipal {

        override fun getName(): String = jws.getBody().getSubject()

        override fun getAttributes(): Map<String, Any>? = jws.getBody().mapValues {
            if (it.key == OAuth2TokenIntrospectionClaimNames.EXP)
                jws.getBody().getExpiration().toInstant()
            else
                it.value
        }

        override fun getAuthorities(): MutableCollection<out GrantedAuthority>? =
            (jws.body["authorities"] as List<String>?)
                ?.map { SimpleGrantedAuthority(it) }
                ?.toMutableList()
    }
}
