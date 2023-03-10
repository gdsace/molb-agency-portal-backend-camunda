package sg.gov.tech.molbagencyportalbackend.util

import java.util.UUID

object CommonUtil {
    fun getEmailDomain(email: String): String = email.substring(email.lastIndexOf("@") + 1)
    fun generateUUID(): String = UUID.randomUUID().toString()
}
