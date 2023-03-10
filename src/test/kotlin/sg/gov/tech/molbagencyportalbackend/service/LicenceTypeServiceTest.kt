package sg.gov.tech.molbagencyportalbackend.service

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import sg.gov.tech.molbagencyportalbackend.fixture.LicenceTypeFixture
import sg.gov.tech.molbagencyportalbackend.repository.LicenceTypeRepository
import sg.gov.tech.testing.MolbUnitTesting

@MolbUnitTesting
class LicenceTypeServiceTest {

    @MockK
    private lateinit var licenceTypeRepository: LicenceTypeRepository

    @InjectMockKs
    private lateinit var licenceTypeService: LicenceTypeService

    @Test
    fun `Should return LicenceType given existing licenceTypeId`() {
        every { licenceTypeRepository.findByLicenceId(any()) } returns LicenceTypeFixture.licenceType
        val licenceType = licenceTypeService.findByLicenceId("valid_licenceType_id")
        assertEquals(LicenceTypeFixture.licenceType, licenceType)
    }

    @Test
    fun `Should return null given licenceTypeId does not exist`() {
        every { licenceTypeRepository.findByLicenceId(any()) } returns null
        val licenceType = licenceTypeService.findByLicenceId("invalid_licenceType_id")
        assertEquals(null, licenceType)
    }

    @Test
    fun `Should return true given existing licenceTypeId`() {
        every { licenceTypeRepository.existsByLicenceId(any()) } returns true
        assertTrue(licenceTypeService.existByLicenceId("valid_licenceType_id"))
    }

    @Test
    fun `Should throw false given licenceTypeId does not exist`() {
        every { licenceTypeRepository.existsByLicenceId(any()) } returns false
        assertFalse(licenceTypeService.existByLicenceId("invalid_licenceType_id"))
    }
}
