package sg.gov.tech.molbagencyportalbackend.dto.wogad

import com.fasterxml.jackson.annotation.JsonProperty
import sg.gov.tech.molbagencyportalbackend.annotation.ExcludeFromGeneratedCoverageTest

@ExcludeFromGeneratedCoverageTest
data class WOGADTokenResponse(
    @JsonProperty("token_type")
    val tokenType: String,
    val scope: String,
    @JsonProperty("expires_in")
    val expiresIn: Int,
    @JsonProperty("ext_expires_in")
    val extExpiresIn: Int,
    @JsonProperty("access_token")
    val accessToken: String,
    @JsonProperty("id_token")
    val idToken: String
)
