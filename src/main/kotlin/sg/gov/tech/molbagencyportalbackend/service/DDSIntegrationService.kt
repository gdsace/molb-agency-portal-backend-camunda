package sg.gov.tech.molbagencyportalbackend.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import sg.gov.tech.molbagencyportalbackend.dto.dds.DDSUploadResponseDTO
import sg.gov.tech.molbagencyportalbackend.exception.ExceptionControllerAdvice
import sg.gov.tech.molbagencyportalbackend.exception.FileDownloadException
import sg.gov.tech.molbagencyportalbackend.integration.dds.DDSDocumentClient
import sg.gov.tech.molbagencyportalbackend.integration.dds.DDSRequestHelper
import sg.gov.tech.molbagencyportalbackend.model.Application
import sg.gov.tech.molbagencyportalbackend.model.Licence
import sg.gov.tech.molbagencyportalbackend.util.DateUtil
import java.net.URL

@Service
class DDSIntegrationService(
    private val ddsDocumentClient: DDSDocumentClient,
    private val ddsRequestHelper: DDSRequestHelper
) {
    @Value("\${integration.dashboard-document-service.app-id}")
    private lateinit var appId: String

    @Value("\${integration.dashboard-document-service.private-key}")
    private lateinit var privateKey: String

    private val logger = LoggerFactory.getLogger(this.javaClass)

    /**
     * generates the header and metadata
     * calls the Dashboard document service
     */
    fun ddsUploadFiles(
        licenceNo: String,
        application: Application,
        file: MultipartFile
    ): ResponseEntity<DDSUploadResponseDTO> {
        val header = ddsRequestHelper.getDDSUploadFileHeaderMap(appId, privateKey)
        val metadata = ddsRequestHelper.getDDSRequestBodyMetaData(
            licenceNo,
            application
        )
        logger.debug("METADATA --> ${ObjectMapper().writeValueAsString(metadata)}")
        return ddsDocumentClient.uploadDocument(
            header,
            file,
            metadata
        )
    }

    /**
     * call DDS document download service to get the S3 download URL
     */
    fun ddsDownloadFile(licence: Licence, documentId: String): ResponseEntity<ByteArray> {
        val header = ddsRequestHelper.getDDSDownloadFileHeaderMap(licence, appId, privateKey)
        logger.debug("DOWNLOAD DOC HEADER - $header")
        try {
            val url =
                ddsDocumentClient.downloadDocument(header, documentId, licence.licenceNumber)
            val customFileName =
                "${licence.licenceNumber}_${DateUtil.getTimestamp()}_${licence.getDocumentName(documentId)}"
            return downloadFile(url.body, customFileName)
        } catch (e: Exception) {
            logger.error(ExceptionControllerAdvice.FILE_DOWNLOAD_ERROR_MESSAGE, e)
            throw FileDownloadException(ExceptionControllerAdvice.FILE_DOWNLOAD_ERROR_MESSAGE)
        }
    }

    fun downloadFile(url: String?, fileName: String): ResponseEntity<ByteArray> {
        val fileDownloadUrl = URL(url)
        var output: ByteArray
        fileDownloadUrl.openStream().use {
            output = it.readAllBytes()
            it.close()
        }
        val httpHeaders = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_OCTET_STREAM
            setContentDispositionFormData("attachment", fileName)
        }
        return ResponseEntity<ByteArray>(output, httpHeaders, HttpStatus.OK)
    }
}
