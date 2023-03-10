package sg.gov.tech.molbagencyportalbackend.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import sg.gov.tech.testing.MolbUnitTesting

@MolbUnitTesting
internal class CommonUtilTest {

    @Test
    fun `should return domain based from input email string`() {
        val domain = CommonUtil.getEmailDomain("XYZ-test_123@domain.com.sg")

        assertEquals("domain.com.sg", domain)
    }
}
