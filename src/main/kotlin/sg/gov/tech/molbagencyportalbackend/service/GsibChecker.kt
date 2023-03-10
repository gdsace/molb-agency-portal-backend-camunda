package sg.gov.tech.molbagencyportalbackend.service

interface GsibChecker {

    fun isGsib(xForwardedFor: String?): Boolean
}
