package sg.gov.tech.molbagencyportalbackend.dto

import com.fasterxml.jackson.databind.JsonNode

data class SendRFADTO(
    val agencyMessageToApplicant: String,
    val internalRemarks: String,
    val clarificationFields: JsonNode
)
