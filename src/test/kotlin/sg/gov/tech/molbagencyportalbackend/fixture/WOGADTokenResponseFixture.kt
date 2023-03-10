package sg.gov.tech.molbagencyportalbackend.fixture

import sg.gov.tech.molbagencyportalbackend.dto.wogad.WOGADTokenResponse

object WOGADTokenResponseFixture {
    val wogadTokenResponse = WOGADTokenResponse(
        tokenType = "token-type",
        scope = "scope",
        expiresIn = 60,
        extExpiresIn = 60,
        accessToken = "access-token",
        idToken = "id-token"
    )
}
