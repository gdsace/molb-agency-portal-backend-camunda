package sg.gov.tech.molbagencyportalbackend.service

import feign.FeignException
import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import sg.gov.tech.molbagencyportalbackend.auth.JwtParser
import sg.gov.tech.molbagencyportalbackend.dao.JwtDao
import sg.gov.tech.molbagencyportalbackend.dto.wogad.WOGADTokenRequest
import sg.gov.tech.molbagencyportalbackend.exception.AuthException
import sg.gov.tech.molbagencyportalbackend.integration.wogad.WOGADClient
import sg.gov.tech.molbagencyportalbackend.model.User
import sg.gov.tech.molbagencyportalbackend.util.CommonUtil
import sg.gov.tech.molbagencyportalbackend.util.FormatterUtil
import java.util.Date
import java.util.concurrent.TimeUnit

@Service
class AuthService(
    private val wogadClient: WOGADClient,
    private val wogadTokenRequest: WOGADTokenRequest,
    private val jwtParser: JwtParser,
    private val userService: UserService,
    private val jwtDao: JwtDao,
    private val gsibChecker: GsibChecker,
    @Value("\${session.ap-token-expiry}") private var apTokenExpiry: Number,
    @Value("\${session.ap-token-secret}") private var apTokenSecret: String
) {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    fun getWogadToken(code: String, xForwardedFor: String? = null): String {
        val map: MultiValueMap<String, Any> = LinkedMultiValueMap()
        map["client_id"] = wogadTokenRequest.clientId
        map["scope"] = wogadTokenRequest.scope
        map["redirect_uri"] = wogadTokenRequest.redirectUri
        map["grant_type"] = wogadTokenRequest.grantType
        map["client_secret"] = wogadTokenRequest.clientSecret
        map["code"] = code
        return try {
            val response = wogadClient.getAccessToken(map)
            generateAPToken(response.accessToken, xForwardedFor)
        } catch (ex: FeignException) {
            logger.error(ex.message)
            throw AuthException("Error calling WOG AD")
        } catch (ex: JwtException) {
            logger.error(ex.message)
            throw AuthException("Unable to generate JWT token")
        }
    }

    fun generateAPToken(token: String, xForwardedFor: String? = null): String {
        val apTokenSecretBase64 = FormatterUtil.encodeStringToBase64(apTokenSecret)

        val wogClaims: Claims = jwtParser.getClaimsFromUnverifiedToken(token)
        val principal = wogClaims["unique_name"].toString()
        val jti = CommonUtil.generateUUID()

        jwtDao.add(principal, jti)

        val apClaims = mutableMapOf<String, Any>()
        apClaims["iss"] = "Agency Portal"
        apClaims["sub"] = principal
        apClaims["exp"] =
            Date(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) + apTokenExpiry.toInt())
        val user: User = userService.getAndValidateUserByEmail(principal)
        apClaims["role"] = user.role.code
        apClaims["user"] = userService.getUserInfo(user)
        apClaims["authorities"] = userService.getUserAuthorities(user.role.authorities)
        apClaims["jti"] = jti
        apClaims["gsib"] = gsibChecker.isGsib(xForwardedFor)

        return Jwts.builder()
            .setClaims(apClaims)
            .signWith(SignatureAlgorithm.HS512, apTokenSecretBase64)
            .compact()
    }

    fun logout(email: String) {
        jwtDao.remove(email)
    }

    fun refresh(claims: Map<String, Any>): String = CommonUtil.generateUUID().let {
        jwtDao.add(claims["sub"] as String, it)
        Jwts.builder()
            .setClaims(
                mapOf(
                    "iss" to "Agency Portal",
                    "sub" to claims["sub"],
                    "exp" to Date(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) + apTokenExpiry.toInt()),
                    "role" to claims["role"],
                    "user" to claims["user"],
                    "authorities" to claims["authorities"],
                    "jti" to it,
                    "gsib" to claims["gsib"]
                )
            )
            .signWith(SignatureAlgorithm.HS512, FormatterUtil.encodeStringToBase64(apTokenSecret))
            .compact()
    }
}
