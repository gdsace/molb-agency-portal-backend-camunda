package sg.gov.tech.molbagencyportalbackend.auth

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.mockk.impl.annotations.InjectMockKs
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import sg.gov.tech.molbagencyportalbackend.exception.AuthException
import sg.gov.tech.testing.MolbUnitTesting
import sg.gov.tech.testing.messageEqualTo
import java.util.Date
import java.util.concurrent.TimeUnit

@MolbUnitTesting
class JwtParserTest {

    @InjectMockKs
    private lateinit var jwtParser: JwtParser

    @Test
    fun `Should return Claims given token is valid`() {
        val apClaims = mutableMapOf<String, Any>()

        apClaims["iss"] = "Agency Portal"
        apClaims["sub"] = "test_user@tech.gov.sg"
        apClaims["exp"] = Date(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) + 60)
        apClaims["role"] = "test_role"

        val token = Jwts.builder()
            .setClaims(apClaims)
            .signWith(SignatureAlgorithm.HS512, "c2VjcmV0")
            .compact()

        val claims = jwtParser.getClaimsFromUnverifiedToken(token)
        Assertions.assertEquals("test_user@tech.gov.sg", claims["sub"])
    }

    @Test
    fun `Should throw AuthException given token is expired`() {
        val token = "eyJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJBZ2VuY3kgUG9ydGFsIiwic3ViIjoiRXVuaWNlX1BPSEB0ZWNoLmdvdi5zZyIsImV4cCI6MTY2Mzc2MDMxNCwicm9sZSI6ImFnZW5jeV9vZmZpY2VyX3JvIn0.mNnh95taT6IRdWS3gVVN2qzdLQ1qZQyj5N4fZhVZbMp9RIfq_6VEUu_ltShsGcrlsdkkwxsLKwrUCr2sJtsedQ"

        assertThrows<AuthException> {
            jwtParser.getClaimsFromUnverifiedToken(token)
        }.messageEqualTo("JWT Token is expired")
    }
}
