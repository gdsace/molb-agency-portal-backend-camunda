package sg.gov.tech.molbagencyportalbackend.integration.wogad

import io.jsonwebtoken.Jwts
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.util.MultiValueMap
import sg.gov.tech.molbagencyportalbackend.dto.wogad.WOGADTokenResponse

@Profile(value = ["local", "test", "dev", "qa"])
@Component("wogadClient")
class MockWOGADClient : WOGADClient {

    override fun getAccessToken(request: MultiValueMap<String, Any>): WOGADTokenResponse =
        when (request.getFirst("code") as String) {
            "ABCDEFGHI0" -> "Admin@tech.gov.sg"
            "ABCDEFGHI1" -> "Helpdesk@tech.gov.sg"
            "ABCDEFGHI2" -> "Supervisor@tech.gov.sg"
            "ABCDEFGHI3" -> "Officer@tech.gov.sg"
            "ABCDEFGHI4" -> "Officer_readonly@tech.gov.sg"
            "ABCDEFGHI5" -> "Admin2@tech.gov.sg"
            "ABCDEFGHI6" -> "Helpdesk2@tech.gov.sg"
            "ABCDEFGHI7" -> "Supervisor2@tech.gov.sg"
            "ABCDEFGHI8" -> "Officer2@tech.gov.sg"
            "ABCDEFGHI9" -> "Officer_readonly2@tech.gov.sg"
            else -> throw IllegalArgumentException("Unknown auth code")
        }.let {
            Jwts.builder()
                .setClaims(mapOf("unique_name" to it))
                .compact()
        }.let {
            WOGADTokenResponse("", "", 0, 0, it, "")
        }
}
