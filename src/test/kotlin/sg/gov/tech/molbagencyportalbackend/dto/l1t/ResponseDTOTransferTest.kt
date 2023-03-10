package sg.gov.tech.molbagencyportalbackend.dto.l1t

import io.mockk.impl.annotations.InjectMockKs
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import sg.gov.tech.molbagencyportalbackend.fixture.ApplicationFixture
import sg.gov.tech.molbagencyportalbackend.fixture.ResourceResponseDTOFixture
import sg.gov.tech.testing.MolbUnitTesting

@MolbUnitTesting
internal class ResponseDTOTransferTest {

    private var version: String = "1.1"

    @InjectMockKs
    private lateinit var responseDTOTransfer: L1TResponseDTOTransfer

    @Test
    fun `Should return correctly formatted success response`() {
        val successResponse =
            responseDTOTransfer.createSuccessResponseDTO(ResourceResponseDTOFixture.result)

        assertEquals(ResourceResponseDTOFixture.result, successResponse.result)
        assertNull(successResponse.error)
    }

    @Test
    fun `Should return correctly formatted error response`() {
        val errorResponse =
            responseDTOTransfer.createErrorResponseDTO(ResourceResponseDTOFixture.error)

        assertEquals(ResourceResponseDTOFixture.error, errorResponse.error)
        assertNull(errorResponse.result)
    }

    @Test
    fun `Should return correctly formatted success result`() {
        val result = responseDTOTransfer.createResultDTO(
            "createApplication",
            ApplicationFixture.createApplication_SingpassSelf.applicationNumber,
            ApplicationFixture.createApplication_SingpassSelf.licenceName,
            ApplicationFixture.createApplication_SingpassSelf.transactionType
        )

        assertEquals("createApplication", result.operation)
        assertEquals(
            ApplicationFixture.createApplication_SingpassSelf.applicationNumber,
            result.applicationNumber
        )
    }
}
