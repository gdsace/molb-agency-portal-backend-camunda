package sg.gov.tech.molbagencyportalbackend.service

import com.fasterxml.jackson.databind.JsonNode
import com.nimbusds.jose.JWSAlgorithm
import feign.FeignException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Lazy
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import sg.gov.tech.molbagencyportalbackend.auth.AuthorizationHeaderGenerator
import sg.gov.tech.molbagencyportalbackend.dto.l1t.L1TMultipleDocumentRequest
import sg.gov.tech.molbagencyportalbackend.dto.l1t.L1TSingleDocumentRequest
import sg.gov.tech.molbagencyportalbackend.dto.l1t.L1TStatusPushRequestDTO
import sg.gov.tech.molbagencyportalbackend.dto.l1t.L1TStatusPushRequestTransfer
import sg.gov.tech.molbagencyportalbackend.exception.FeignBadRequestException
import sg.gov.tech.molbagencyportalbackend.integration.job.L1TUpdateStatusPayload
import sg.gov.tech.molbagencyportalbackend.integration.l1t.L1TDocumentClient
import sg.gov.tech.molbagencyportalbackend.integration.l1t.L1TStatusPushClient
import sg.gov.tech.molbagencyportalbackend.model.Application
import sg.gov.tech.molbagencyportalbackend.model.Licence
import sg.gov.tech.security.SecretUtils

@Service
class L1TIntegrationService(
    private val l1TDocumentClient: L1TDocumentClient,
    private val l1tStatusPushClient: L1TStatusPushClient,
    private val l1tStatusPushRequestTransfer: L1TStatusPushRequestTransfer,
    @Lazy private val licenceService: LicenceService
) {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    @Value("\${integration.l1t.app-id}")
    private lateinit var appId: String

    @Value("\${integration.l1t.private-key}")
    private lateinit var privateKey: String

    companion object {
        private const val AUTHORIZATION_HEADER_KEY = "authorization"
    }

    fun downloadSingleFile(payload: L1TSingleDocumentRequest): ResponseEntity<ByteArray> =
        l1TDocumentClient.downloadSingleFile(payload)

    fun downloadMultipleFiles(payload: L1TMultipleDocumentRequest): ResponseEntity<ByteArray> =
        l1TDocumentClient.downloadMultipleFiles(payload)

    fun createL1TApplicationStatusRequest(
        application: Application,
        clarificationFields:
            JsonNode? = null
    ): L1TStatusPushRequestDTO {
        val applicationLicence = licenceService.getByApplicationId(application.id!!)
        val l1tApplication =
            l1tStatusPushRequestTransfer.createL1TApplication(application, applicationLicence, clarificationFields)

        return l1tStatusPushRequestTransfer.createL1TStatusPushRequestDTO(
            "statusPoll",
            listOf(l1tApplication),
            null
        )
    }

    fun createL1TLicenceStatusRequest(licence: Licence): L1TStatusPushRequestDTO {
        val l1tLicence =
            l1tStatusPushRequestTransfer.createL1TLicence(licence)

        return l1tStatusPushRequestTransfer.createL1TStatusPushRequestDTO(
            "statusPoll",
            null,
            listOf(l1tLicence)
        )
    }

    fun updateL1TStatus(payload: L1TUpdateStatusPayload) {
        val headers = getL1TIntegrationHeaderMap()

        try {
            l1tStatusPushClient.updateStatus(headers, payload.l1tStatusPushRequest)
            logger.info("Updated status to L1T ${payload.referenceNumber}")
        } catch (ex: FeignException) {
            logger.error(ex.message)
            throw FeignBadRequestException("Error updating status in L1T")
        }
    }

    private fun getL1TIntegrationHeaderMap(): HashMap<String, String> {
        val headers: HashMap<String, String> = HashMap()
        headers[AUTHORIZATION_HEADER_KEY] = getAuthorizationHeader(appId, privateKey)
        return headers
    }

    private fun getAuthorizationHeader(appId: String, privateKey: String): String {
        return AuthorizationHeaderGenerator().generateHeader(
            appId,
            JWSAlgorithm.RS256,
            SecretUtils.getPrivateKey(privateKey)
        )
    }
}
