package sg.gov.tech.molbagencyportalbackend.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object DateUtil {
    const val DATEFORMAT_DATE = "dd/MM/yyyy"
    const val DATEFORMAT_DATE_TIME = "dd/MM/yyyy HH:mm:ss"

    fun getTimestamp(): String {
        val singaporeTimeZone = ZoneId.of("Asia/Singapore")
        val sdf = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS").withZone(singaporeTimeZone)
        return LocalDateTime.now().format(sdf)
    }

    fun getLocalDateFormat(date: String, currentFormat: String): LocalDate {
        val formatter = DateTimeFormatter.ofPattern(currentFormat)
        return LocalDate.parse(date, formatter)
    }

    fun dateToString(date: LocalDate, format: String): String {
        return date.format(DateTimeFormatter.ofPattern(format))
    }
}
