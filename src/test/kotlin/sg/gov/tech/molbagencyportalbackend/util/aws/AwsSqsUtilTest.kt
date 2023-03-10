package sg.gov.tech.molbagencyportalbackend.util.aws

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.runs
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import sg.gov.tech.molbagencyportalbackend.fixture.L1TUpdateStatusPayloadFixture
import sg.gov.tech.testing.MolbUnitTesting
import sg.gov.tech.testing.verifyOnce

@MolbUnitTesting
internal class AwsSqsUtilTest {

    private var molbHost: String = "http://localhost:8088"

    @MockK
    private lateinit var awsSqsClient: AwsSqsClient

    @InjectMockKs
    private lateinit var awsSqsUtil: AwsSqsUtil

    @Test
    fun `should return correctly formated sqs message`() {
        val mockApiUrl = "$molbHost/mock"
        val mockPayload = ""
        val mockApiMethod = "POST"

        assertEquals(
            "{\"apiMethod\":\"POST\",\"apiUrl\":\"http:\\/\\/localhost:8088\\/mock\",\"payload\":\"\"}",
            awsSqsUtil.formatSqsMessage(mockPayload, mockApiUrl, mockApiMethod)
        )
    }

    @Test
    fun `should send L1T Update Status Sqs Message`() {
        every { awsSqsClient.sendMessage(any(), any(), any()) } just runs
        awsSqsUtil.sendL1TUpdateStatusSqsMessage(L1TUpdateStatusPayloadFixture.getUpdateApplicationStatus)
        verifyOnce { awsSqsClient.sendMessage(any(), any(), any()) }
    }
}
