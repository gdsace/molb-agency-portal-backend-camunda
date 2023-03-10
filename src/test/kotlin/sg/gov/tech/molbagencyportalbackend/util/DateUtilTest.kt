package sg.gov.tech.molbagencyportalbackend.util

import io.mockk.every
import io.mockk.mockkStatic
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import sg.gov.tech.testing.MolbUnitTesting
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@MolbUnitTesting
internal class DateUtilTest {

    @Test
    fun `should return formatted date based from current time in millis`() {
        mockkStatic(LocalDateTime::class)
        every { LocalDateTime.now() } returns LocalDateTime.ofInstant(
            Instant.ofEpochMilli(1659509835000L),
            ZoneId.of("Asia/Singapore")
        ) // 2022-08-03 14:57:15.0

        assertEquals("20220803145715000", DateUtil.getTimestamp())
    }
}
