package sg.gov.tech.molbagencyportalbackend.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Profile(value = ["staging", "production"])
@Component("gsibChecker")
class IpListGsibChecker(@Value("\${integration.gsib.ip}") private val ipList: String) : GsibChecker {

    override fun isGsib(xForwardedFor: String?): Boolean = xForwardedFor
        .takeIf { !it.isNullOrEmpty() }
        ?.let { it.split(",").intersect(ipList.split(",")).isNotEmpty() }
        ?: false
}
