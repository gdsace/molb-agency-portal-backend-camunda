package sg.gov.tech.molbagencyportalbackend.configuration

import com.google.common.io.BaseEncoding
import org.springframework.beans.factory.annotation.Value
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope
import sg.gov.tech.molbagencyportalbackend.annotation.ExcludeFromGeneratedCoverageTest
import sg.gov.tech.utils.Masking
import sg.gov.tech.utils.singaporeZoneId
import java.time.Clock
import java.util.Locale
import java.util.TimeZone
import javax.annotation.PostConstruct

@Configuration
@ExcludeFromGeneratedCoverageTest
class ApplicationConfiguration {

    @Bean
    fun masking(@Value("\${masking.privateKeyHexadecimal}") privateKeyHexadecimal: String) =
        Masking(privateKeyHexadecimal.hexStringToByteArray())

    private fun String.hexStringToByteArray() = BaseEncoding.base16().decode(uppercase(Locale.getDefault()))

    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
    fun clock(): Clock = Clock.system(singaporeZoneId)

    @PostConstruct
    fun setSystemDefaultTimeZone() {
        TimeZone.setDefault(TimeZone.getTimeZone(singaporeZoneId))
    }

    @Bean("featureToggleSettings")
    @ConfigurationProperties("features")
    fun featureToggleSettings(): Map<String, Boolean> {
        return HashMap()
    }
}
