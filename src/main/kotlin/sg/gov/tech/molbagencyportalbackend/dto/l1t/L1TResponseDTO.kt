package sg.gov.tech.molbagencyportalbackend.dto.l1t

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import sg.gov.tech.molbagencyportalbackend.annotation.ExcludeFromGeneratedCoverageTest

@ExcludeFromGeneratedCoverageTest
data class L1TResponseDTO(
    val result: L1TResultDTO?,
    val error: L1TErrorDTO?
)

@ExcludeFromGeneratedCoverageTest
data class L1TErrorDTO(
    val status: String,
    val message: String,
    val subErrors: List<L1TSubErrorDTO>,
    val path: String,
    val version: String? = ""
)

@ExcludeFromGeneratedCoverageTest
data class L1TResultDTO(
    val operation: String,
    val applicationNumber: String,
    val licenceName: String,
    val transactionType: String? = null
)

@ExcludeFromGeneratedCoverageTest
data class L1TSubErrorDTO(
    val pointer: String,
    val rejectedValue: String,
    val message: String
)

@Component
@ExcludeFromGeneratedCoverageTest
class L1TResponseDTOTransfer {

    @Value("\${integration.l1t.version}")
    private lateinit var version: String

    fun createSuccessResponseDTO(result: L1TResultDTO): L1TResponseDTO {
        return createResponseDTO(result, null)
    }

    fun createErrorResponseDTO(error: L1TErrorDTO): L1TResponseDTO {
        val errorVersion = L1TErrorDTO(
            status = error.status,
            message = error.message,
            subErrors = error.subErrors,
            path = error.path,
            version = version
        )
        return createResponseDTO(null, errorVersion)
    }

    private fun createResponseDTO(result: L1TResultDTO?, error: L1TErrorDTO?): L1TResponseDTO {
        return L1TResponseDTO(
            result = result,
            error = error
        )
    }

    fun createResultDTO(
        operation: String,
        applicationNumber: String,
        licenceName: String,
        transactionType: String
    ): L1TResultDTO {
        return L1TResultDTO(
            operation = operation,
            applicationNumber = applicationNumber,
            licenceName = licenceName,
            transactionType = transactionType
        )
    }
}
