package sg.gov.tech.molbagencyportalbackend.security

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.jdbc.Sql
import org.springframework.transaction.annotation.Transactional
import sg.gov.tech.molbagencyportalbackend.fixture.LicenceModelFixture
import sg.gov.tech.molbagencyportalbackend.model.Licence
import sg.gov.tech.molbagencyportalbackend.repository.LicenceRepository
import sg.gov.tech.testing.MolbIntegrationTesting
import sg.gov.tech.utils.Masking

@MolbIntegrationTesting
@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = ["classpath:fixtures/sql/seed_licence_test_data.sql"])
internal class DbMaskingTest {
    @Autowired
    private lateinit var masking: Masking

    @Autowired
    private lateinit var licenceRepository: TestLicenceRepository

    @Nested
    inner class AttributeConverterTest {
        private lateinit var savedLicence: Licence

        val newLicence = LicenceModelFixture.licence
        val nricVal = newLicence.nric

        @BeforeEach
        fun setUp() {
            savedLicence = licenceRepository.save(newLicence)
        }

        @Test
        fun `should mask sensitive information when inserting`() {
            val maskedNric = masking.mask(nricVal)
            val rawNricValue = getRawNric()

            assertThat(rawNricValue).isEqualTo(maskedNric)
        }

        @Test
        fun `should mask sensitive information when updating`() {
            val updatedNric = "S1234567A"
            val maskedUpdatedNric = masking.mask(updatedNric)
            licenceRepository.save(
                savedLicence.copy(nric = updatedNric)
            )
            assertThat(getRawNric()).isEqualTo(maskedUpdatedNric)
        }

        @Test
        fun `should unmask sensitive information when fetching value`() {
            assertThat(savedLicence.nric).isEqualTo(nricVal)
        }

        @Test
        fun `should no-op and log warning when unmasking fails`() {
            val unmasked = "80001111"

            licenceRepository.updateRawNric(newLicence.licenceNumber, unmasked)
            val updatedLicence = licenceRepository.findByLicenceNumber(newLicence.licenceNumber)

            if (updatedLicence != null) {
                assertThat(updatedLicence.nric).isEqualTo(unmasked)
            }
        }

        private fun getRawNric() = licenceRepository.getRawNric(newLicence.licenceNumber)
    }
}

interface TestLicenceRepository : LicenceRepository {
    @Query(value = "SELECT nric FROM licence WHERE licence_number =:licenceNumber", nativeQuery = true)
    fun getRawNric(licenceNumber: String): String

    @Modifying
    @Transactional
    @Query(value = "UPDATE licence SET nric = :value WHERE licence_number =:licenceNumber", nativeQuery = true)
    fun updateRawNric(licenceNumber: String, value: String)
}
