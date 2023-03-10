package sg.gov.tech.molbagencyportalbackend.fixture

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.convertValue
import sg.gov.tech.molbagencyportalbackend.dto.SendRFADTO

object SendRFADTOFixture {
    val RFADTO = SendRFADTO(
        agencyMessageToApplicant = "test agency message",
        internalRemarks = "test internal remarks",
        clarificationFields = ObjectMapper().convertValue("")
    )
}
