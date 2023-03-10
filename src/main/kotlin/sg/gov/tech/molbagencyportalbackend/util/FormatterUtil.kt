package sg.gov.tech.molbagencyportalbackend.util

import org.springframework.util.Base64Utils

object FormatterUtil {
    fun formatOperationTypeForPath(operationType: String): String = "/$operationType"

    fun encodeStringToBase64(string: String): String = Base64Utils.encodeToString(string.toByteArray())
    fun decodeBase64ToString(base64String: String): String = String(Base64Utils.decodeFromString(base64String))
}
