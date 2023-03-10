package sg.gov.tech.molbagencyportalbackend.service

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import sg.gov.tech.molbagencyportalbackend.fixture.AgencyFixture
import sg.gov.tech.molbagencyportalbackend.repository.AgencyRepository
import sg.gov.tech.testing.MolbUnitTesting

@MolbUnitTesting
class AgencyServiceTest {

    @MockK
    private lateinit var agencyRepository: AgencyRepository

    @InjectMockKs
    private lateinit var agencyService: AgencyService

    @Test
    fun `Should return Agency given agencyCode exist`() {
        every { agencyRepository.findByCode(any()) } returns AgencyFixture.agency
        val agency = agencyService.findByCode("valid_agency_code")
        assertEquals(AgencyFixture.agency, agency)
    }

    @Test
    fun `Should return null given agencyCode does not exist`() {
        every { agencyRepository.findByCode(any()) } returns null
        val agency = agencyService.findByCode("invalid_agency_code")
        assertEquals(null, agency)
    }

    @Test
    fun `Should return true given agencyCode exist`() {
        every { agencyRepository.existsByCode(any()) } returns true
        assertTrue(agencyService.existByCode("valid_agency_code"))
    }

    @Test
    fun `Should return false given agencyCode does not exist`() {
        every { agencyRepository.existsByCode(any()) } returns false
        assertFalse(agencyService.existByCode("invalid_agency_code"))
    }
}
