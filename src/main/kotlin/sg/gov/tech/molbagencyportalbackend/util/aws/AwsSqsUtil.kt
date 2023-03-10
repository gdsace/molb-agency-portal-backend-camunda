package sg.gov.tech.molbagencyportalbackend.util.aws

import com.nimbusds.jose.shaded.json.JSONObject
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import sg.gov.tech.molbagencyportalbackend.integration.job.JobConfig
import sg.gov.tech.molbagencyportalbackend.integration.job.L1TUpdateStatusPayload

@Component
class AwsSqsUtil(private val awsSqsClient: AwsSqsClient) {

    @Value("\${server.host}")
    private lateinit var molbHost: String

    @Value("\${aws.sqs.start-app-workflow-url}")
    private lateinit var startApplicationQueueUrl: String

    @Value("\${aws.sqs.l1t-update-status-url}")
    private lateinit var l1tUpdateStatusQueueUrl: String

    fun formatSqsMessage(message: Any, endpoint: String, method: String): String? {
        val messageBody = JSONObject()
        messageBody["apiUrl"] = endpoint
        messageBody["payload"] = message
        messageBody["apiMethod"] = method
        return messageBody.toJSONString()
    }

    fun sendL1TUpdateStatusSqsMessage(payload: L1TUpdateStatusPayload) = run {
        formatSqsMessage(
            payload,
            molbHost + JobConfig.Endpoint.L1T_UPDATE_STATUS,
            "POST"
        )?.let { awsSqsClient.sendMessage(it, l1tUpdateStatusQueueUrl, payload.referenceNumber) }
    }
}
