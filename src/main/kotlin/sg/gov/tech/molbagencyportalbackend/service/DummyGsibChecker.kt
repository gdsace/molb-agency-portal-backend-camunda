package sg.gov.tech.molbagencyportalbackend.service

import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Profile(value = ["local", "dev", "qa", "test"])
@Component("gsibChecker")
class DummyGsibChecker(private val returnValue: Boolean = true) : GsibChecker {

    override fun isGsib(xForwardedFor: String?): Boolean = returnValue
}
