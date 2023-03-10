package sg.gov.tech.molbagencyportalbackend.dao

import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Profile(value = ["test"])
@Component("inMemoryJwtDao")
class InMemoryJwtDao : JwtDao {

    var tokens = emptyMap<String, String>()

    override fun add(email: String, jti: String) {
        tokens += Pair(email, jti)
    }

    override fun contains(email: String, jti: String): Boolean =
        tokens.contains(email) && tokens[email] == jti

    override fun remove(email: String) {
        tokens -= email
    }
}
