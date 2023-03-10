import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import sg.gov.tech.integration.properties.AwsProperties
import sg.gov.tech.molbagencyportalbackend.exception.QueueException
import sg.gov.tech.molbagencyportalbackend.util.aws.AwsSqsClient
import sg.gov.tech.testing.MolbUnitTesting

@MolbUnitTesting
internal class AwsSqsClientTest {

    private lateinit var awsProperties: AwsProperties
    private lateinit var awsSqsClient: AwsSqsClient

    @BeforeEach
    fun before() {
        awsProperties = AwsProperties()
        awsSqsClient = AwsSqsClient(awsProperties)
    }

    @Test
    fun `should throw QueueException if failed to push to queue`() {
        val mockSqsUrl = ""
        assertThrows<QueueException> {
            awsSqsClient.sendMessage("mockMessage", mockSqsUrl)
        }.message.contains(mockSqsUrl)
    }
}
