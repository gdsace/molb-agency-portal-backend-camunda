package sg.gov.tech.molbagencyportalbackend.util.aws

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.sqs.AmazonSQS
import com.amazonaws.services.sqs.AmazonSQSClientBuilder
import com.amazonaws.services.sqs.model.QueueDoesNotExistException
import com.amazonaws.services.sqs.model.SendMessageRequest
import org.slf4j.LoggerFactory
import sg.gov.tech.integration.aws.AwsClient
import sg.gov.tech.integration.properties.AwsProperties
import sg.gov.tech.molbagencyportalbackend.exception.QueueException
import sg.gov.tech.molbagencyportalbackend.util.CommonUtil
import sg.gov.tech.utils.or

class AwsSqsClient(
    awsProperties: AwsProperties,
) : AwsClient(
    awsProperties.accessKey,
    awsProperties.secretKey,
    awsProperties.region.or(Regions.AP_SOUTHEAST_1.getName())
) {

    private var client: AmazonSQS

    private val logger = LoggerFactory.getLogger(this.javaClass)

    init {
        if (this.credentials != null) {
            this.client = AmazonSQSClientBuilder.standard()
                .withRegion(region)
                .withCredentials(AWSStaticCredentialsProvider(credentials))
                .build()
        } else {
            this.client = AmazonSQSClientBuilder.standard()
                .withRegion(region)
                .build()
        }
    }

    // Set messageGroupId for sending to FIFO Queues
    fun sendMessage(message: String, queueUrl: String, messageGroupId: String = "") {
        try {
            logger.info("Sending to SQS: $message")
            val sendMsgRequest = SendMessageRequest(queueUrl, message)
            if (messageGroupId.isNotBlank()) {
                sendMsgRequest.withMessageGroupId(messageGroupId)
                sendMsgRequest.withMessageDeduplicationId(CommonUtil.generateUUID())
            }
            client.sendMessage(sendMsgRequest)
        } catch (ex: QueueDoesNotExistException) {
            logger.error(ex.message, ex)
            throw QueueException("Queue does not exist: $queueUrl")
        } catch (ex: Exception) {
            logger.error(ex.message, ex)
            throw QueueException("Failed to push to queue: $queueUrl")
        }
    }
}
