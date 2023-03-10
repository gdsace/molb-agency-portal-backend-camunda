package sg.gov.tech.molbagencyportalbackend.dto.wogad

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import sg.gov.tech.molbagencyportalbackend.annotation.ExcludeFromGeneratedCoverageTest
import sg.gov.tech.molbagencyportalbackend.exception.ExceptionControllerAdvice
import javax.validation.constraints.NotEmpty

@Component
@ExcludeFromGeneratedCoverageTest
class WOGADTokenRequest {

    @Value("\${integration.wogad.client-id}")
    lateinit var clientId: String

    @Value("\${integration.wogad.scope}")
    lateinit var scope: String

    @Value("\${integration.wogad.redirect-uri}")
    lateinit var redirectUri: String

    @Value("\${integration.wogad.grant-type}")
    lateinit var grantType: String

    @Value("\${integration.wogad.client-secret}")
    lateinit var clientSecret: String
}

@ExcludeFromGeneratedCoverageTest
data class AuthTokenRequestParams(
    @field:NotEmpty(message = ExceptionControllerAdvice.INVALID_VALUE_MESSAGE)
    val code: String
)
