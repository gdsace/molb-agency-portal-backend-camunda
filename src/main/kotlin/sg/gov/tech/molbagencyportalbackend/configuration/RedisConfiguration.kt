package sg.gov.tech.molbagencyportalbackend.configuration

import io.lettuce.core.ReadFrom
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.connection.RedisPassword
import org.springframework.data.redis.connection.RedisStaticMasterReplicaConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory

@Configuration
class RedisConfiguration {

    @Profile(value = ["local"])
    @Bean
    fun connectionFactory(
        @Value("\${redis.primary}") primary: String,
        @Value("\${redis.replica}") replica: String,
        @Value("\${redis.port}") port: Int,
        @Value("\${redis.token}") token: String
    ): LettuceConnectionFactory {
        val clientConfig = LettuceClientConfiguration.builder()
            .readFrom(ReadFrom.REPLICA_PREFERRED)
            .build()
        val serverConfig = RedisStaticMasterReplicaConfiguration(primary, port).apply {
            addNode(replica, port)
            setPassword(RedisPassword.of(token))
        }
        return LettuceConnectionFactory(serverConfig, clientConfig)
    }

    @Profile(value = ["dev", "qa", "staging", "production"])
    @Bean
    fun secureConnectionFactory(
        @Value("\${redis.primary}") primary: String,
        @Value("\${redis.replica}") replica: String,
        @Value("\${redis.port}") port: Int,
        @Value("\${redis.token}") token: String
    ): LettuceConnectionFactory {
        val clientConfig = LettuceClientConfiguration.builder()
            .readFrom(ReadFrom.REPLICA_PREFERRED)
            .useSsl()
            .build()
        val serverConfig = RedisStaticMasterReplicaConfiguration(primary, port).apply {
            addNode(replica, port)
            setPassword(RedisPassword.of(token))
        }
        return LettuceConnectionFactory(serverConfig, clientConfig)
    }
}
