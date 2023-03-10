package sg.gov.tech.molbagencyportalbackend.service

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import sg.gov.tech.molbagencyportalbackend.auth.JwtParser
import sg.gov.tech.molbagencyportalbackend.dao.InMemoryJwtDao
import sg.gov.tech.molbagencyportalbackend.dao.JwtDao
import sg.gov.tech.molbagencyportalbackend.dto.wogad.WOGADTokenRequest
import sg.gov.tech.molbagencyportalbackend.fixture.UserFixture
import sg.gov.tech.molbagencyportalbackend.fixture.WOGADTokenResponseFixture
import sg.gov.tech.molbagencyportalbackend.integration.wogad.WOGADClient
import sg.gov.tech.molbagencyportalbackend.util.CommonUtil
import sg.gov.tech.molbagencyportalbackend.util.FormatterUtil
import sg.gov.tech.testing.MolbUnitTesting
import sg.gov.tech.testing.verifyOnce
import java.util.Date
import java.util.concurrent.TimeUnit

@MolbUnitTesting
class AuthServiceTest {

    private var apTokenExpiry: Int = 60
    private var apTokenSecret: String = "secret"

    @MockK
    private lateinit var wogadClient: WOGADClient

    @MockK
    private lateinit var wogadTokenRequest: WOGADTokenRequest

    @MockK
    private lateinit var jwtParser: JwtParser

    @MockK
    private lateinit var userService: UserService

    @MockK
    private lateinit var jwtDao: JwtDao

    @MockK
    private lateinit var gsibChecker: GsibChecker

    private lateinit var inMemoryJwtDao: InMemoryJwtDao

    private lateinit var dummyGsibChecker: DummyGsibChecker

    @InjectMockKs
    private lateinit var authService: AuthService

    private lateinit var service: AuthService

    @BeforeEach
    fun setUp() {
        inMemoryJwtDao = InMemoryJwtDao()
        dummyGsibChecker = DummyGsibChecker(true)
        service = AuthService(
            wogadClient,
            wogadTokenRequest,
            jwtParser,
            userService,
            inMemoryJwtDao,
            dummyGsibChecker,
            apTokenExpiry,
            apTokenSecret
        )
    }

    @Test
    fun `Should return AP JWT Token given the access_token is valid`() {
        val wogadClaims = mutableMapOf<String, Any>()
        wogadClaims["unique_name"] = "atest@test.com"
        wogadClaims["exp"] = Date(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) + 60)

        val wogAccessToken = Jwts.builder()
            .setClaims(wogadClaims)
            .signWith(SignatureAlgorithm.HS512, "c2VjcmV0")
            .compact()

        every { jwtParser.getClaimsFromUnverifiedToken(any()) } answers { callOriginal() }
        every { userService.getAndValidateUserByEmail(any()) } returns UserFixture.userA
        every { userService.getUserInfo(any()) } returns HashMap()
        every { jwtDao.add(any(), any()) } returns Unit
        every { gsibChecker.isGsib(any()) } returns true

        val apToken = authService.generateAPToken(wogAccessToken)

        val apClaimsFromToken = jwtParser.getClaimsFromUnverifiedToken(apToken)

        assertEquals("atest@test.com", apClaimsFromToken["sub"])
        assertEquals("agency_supervisor", apClaimsFromToken["role"])
    }

    @Test
    fun `Should call WOG AD Auth to retrieve access_token`() {
        val wogadClaims: MultiValueMap<String, Any> = LinkedMultiValueMap()
        wogadClaims["unique_name"] = "atest@test.com"
        wogadClaims["exp"] = Date(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) + 60)

        every { wogadTokenRequest.clientId } returns "client-id"
        every { wogadTokenRequest.scope } returns "scope"
        every { wogadTokenRequest.redirectUri } returns "wogad.redirect.uri"
        every { wogadTokenRequest.grantType } returns "grant-type"
        every { wogadTokenRequest.clientSecret } returns "client-secret"
        every { wogadClient.getAccessToken(any()) } returns WOGADTokenResponseFixture.wogadTokenResponse
        every { jwtParser.getClaimsFromUnverifiedToken(any()) } returns Jwts.claims(wogadClaims.toSingleValueMap())
        every { userService.getAndValidateUserByEmail(any()) } returns UserFixture.userA
        every { userService.getUserInfo(any()) } returns HashMap()
        every { userService.getUserAuthorities(any()) } returns mutableListOf("claim_application")
        every { jwtDao.add(any(), any()) } returns Unit
        every { gsibChecker.isGsib(any()) } returns true

        authService.getWogadToken("test-auth-code")

        verifyOnce { wogadClient.getAccessToken(any()) }
    }

    @Test
    fun `Should invalidate token`() {
        val email = "batman@tech.gov.sg"
        inMemoryJwtDao.tokens += Pair(email, CommonUtil.generateUUID())
        service.logout(email)
        assertTrue(inMemoryJwtDao.tokens.isEmpty())
    }

    @Test
    fun `Should refresh token`() {
        val claims = mapOf(
            "iss" to "Agency Portal",
            "sub" to "batman@tech.gov.sg",
            "exp" to Date(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) + apTokenExpiry),
            "role" to "BATMAN",
            "jti" to CommonUtil.generateUUID()
        )
        inMemoryJwtDao.tokens += Pair(claims["sub"] as String, claims["jti"] as String)
        val token = service.refresh(claims)
        val jws = Jwts.parser()
            .setSigningKey(FormatterUtil.encodeStringToBase64(apTokenSecret))
            .parseClaimsJws(token)
        assertEquals(claims["iss"], jws.body.issuer)
        assertEquals(claims["sub"], jws.body.subject)
        assertTrue((claims["exp"] as Date) < jws.body.expiration)
        assertEquals(claims["role"], jws.body["role"])
        assertNotEquals(claims["jti"], jws.body.id)
        assertEquals(jws.body.id, inMemoryJwtDao.tokens[claims["sub"] as String])
    }
}
