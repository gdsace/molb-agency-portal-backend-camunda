package sg.gov.tech.molbagencyportalbackend.fixture

import sg.gov.tech.molbagencyportalbackend.dto.l1t.L1TErrorDTO
import sg.gov.tech.molbagencyportalbackend.dto.l1t.L1TResponseDTO
import sg.gov.tech.molbagencyportalbackend.dto.l1t.L1TResultDTO

object ResourceResponseDTOFixture {
    val result = L1TResultDTO(
        operation = "createApplication",
        applicationNumber = "A1234567",
        licenceName = "Test Licence",
        transactionType = "new"
    )

    val responseSuccess = L1TResponseDTO(
        result = result,
        error = null
    )

    val error = L1TErrorDTO(
        status = "Validation Logic Error",
        message = "validation error",
        subErrors = emptyList(),
        path = "/test",
        version = "1.1"
    )

    val responseError = L1TResponseDTO(
        result = null,
        error = error
    )
}
