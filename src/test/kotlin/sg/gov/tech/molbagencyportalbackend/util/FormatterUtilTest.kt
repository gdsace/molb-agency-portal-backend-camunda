package sg.gov.tech.molbagencyportalbackend.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import sg.gov.tech.testing.MolbUnitTesting

@MolbUnitTesting
internal class FormatterUtilTest {

    @Test
    fun `should return formatted path based from input string`() {
        val path = FormatterUtil.formatOperationTypeForPath("testPath")

        assertEquals("/testPath", path)
    }

    @Test
    fun `should return Base64 format from input string`() {
        val encodedString = FormatterUtil.encodeStringToBase64("secret")

        assertEquals("c2VjcmV0", encodedString)
    }

    @Test
    fun `should return string format from input Base64`() {
        val decodedString = FormatterUtil.decodeBase64ToString("c2VjcmV0")

        assertEquals("secret", decodedString)
    }
}
