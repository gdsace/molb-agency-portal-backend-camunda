package sg.gov.tech.molbagencyportalbackend.dao

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Profile(value = ["local", "dev", "qa", "staging", "production"])
@Component("jwtDao")
class RedisJwtDao(
    private val redis: RedisTemplate<String, String>,
    @Value("\${session.ap-token-expiry}") private val timeout: Long
) : JwtDao {

    companion object {
        private const val REDIS_PREFIX = "jwt"
    }

    private val logger = LoggerFactory.getLogger(this.javaClass)

    private fun key(email: String): String = "$REDIS_PREFIX:${email.lowercase()}"

    override fun add(email: String, jti: String) {
        val key = key(email)
        logger.info("Adding ($key, $jti)")
        redis.opsForValue().set(key, jti, Duration.ofSeconds(timeout))
    }

    override fun contains(email: String, jti: String): Boolean {
        val key = key(email)
        logger.info("Checking ($key, $jti)")
        return redis.run {
            hasKey(key) && opsForValue().get(key) == jti
        }
    }

    override fun remove(email: String) {
        val key = key(email)
        logger.info("Removing $key")
        redis.delete(key)
    }
}
