package sg.gov.tech.molbagencyportalbackend.dto.l1t

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component
import sg.gov.tech.molbagencyportalbackend.annotation.ExcludeFromGeneratedCoverageTest
import sg.gov.tech.molbagencyportalbackend.dto.dds.DDSUploadResponseDTO
import sg.gov.tech.molbagencyportalbackend.model.Application
import sg.gov.tech.molbagencyportalbackend.model.ApplicationStatus
import sg.gov.tech.molbagencyportalbackend.model.Licence
import sg.gov.tech.molbagencyportalbackend.util.DateUtil
import sg.gov.tech.utils.toYN

@ExcludeFromGeneratedCoverageTest
data class L1TStatusPushRequestDTO(
    val result: L1TResult
)

@ExcludeFromGeneratedCoverageTest
data class L1TResult(
    val operation: String,
    val application: List<L1TApplication>?,
    val licence: List<L1TLicence>?
)

@ExcludeFromGeneratedCoverageTest
data class L1TApplication(
    val applicationNumber: String,
    val applicationStatus: String,
    val paymentAmount: String?,
    val licence: List<L1TLicence>?,
    val message: String?,
    val clarificationField: List<String>?
)

@ExcludeFromGeneratedCoverageTest
data class L1TLicence(
    val licenceNumber: String,
    val issueDate: String,
    val startDate: String,
    val expiryDate: String?,
    val status: String,
    val renewalIndicator: String,
    val licenceFile: List<L1TLicenceFile>?
)

@ExcludeFromGeneratedCoverageTest
data class L1TLicenceFile(
    val refId: String,
    val fileName: String
)

@ExcludeFromGeneratedCoverageTest
data class L1TStatusPushResponseDTO(
    var error: L1TError? = null
)

@ExcludeFromGeneratedCoverageTest
data class L1TError(
    var applications: List<L1TErrorMessage?>? = null,
    var licences: List<L1TErrorMessage?>? = null
)

@ExcludeFromGeneratedCoverageTest
data class L1TErrorMessage(
    val referenceNumber: String,
    val message: String
)

@Component
@ExcludeFromGeneratedCoverageTest
class L1TStatusPushRequestTransfer {
    fun createL1TStatusPushRequestDTO(
        operation: String,
        applications: List<L1TApplication>? = null,
        licences: List<L1TLicence>? = null
    ): L1TStatusPushRequestDTO {
        return L1TStatusPushRequestDTO(
            result = L1TResult(
                operation = operation,
                application = applications,
                licence = licences
            )
        )
    }

    fun createL1TApplication(
        application: Application,
        applicationLicence: Licence?,
        clarificationField: JsonNode? = null
    ) =
        L1TApplication(
            applicationNumber = application.applicationNumber,
            applicationStatus = if (application.status.value === ApplicationStatus.WITHDRAWN.value) "WITHDRAWN BY AGENCY" else application.status.value,
            paymentAmount = null,
            licence = applicationLicence?.let { listOf(createL1TLicence(it)) },
            message = application.messageToApplicant,
            clarificationField = clarificationField?.let {
                val reader = ObjectMapper().readerFor(object : TypeReference<List<String>?>() {})
                reader.readValue(it)
            }
        )

    fun createL1TLicence(licence: Licence) =
        L1TLicence(
            licenceNumber = licence.licenceNumber,
            issueDate = DateUtil.dateToString(licence.issueDate, DateUtil.DATEFORMAT_DATE),
            startDate = DateUtil.dateToString(licence.startDate, DateUtil.DATEFORMAT_DATE),
            expiryDate = licence.expiryDate?.let {
                DateUtil.dateToString(it, DateUtil.DATEFORMAT_DATE)
            },
            status = licence.status.value,
            renewalIndicator = licence.dueForRenewal.toYN(),
            licenceFile = licence.licenceDocuments?.map { createL1TLicenceFile(it) }
        )

    private fun createL1TLicenceFile(licenseFile: DDSUploadResponseDTO) =
        L1TLicenceFile(
            refId = licenseFile.documentId,
            fileName = licenseFile.filename
        )
}
