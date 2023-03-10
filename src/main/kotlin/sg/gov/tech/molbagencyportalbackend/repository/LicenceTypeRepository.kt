package sg.gov.tech.molbagencyportalbackend.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import sg.gov.tech.molbagencyportalbackend.model.LicenceType

@Repository
interface LicenceTypeRepository : JpaRepository<LicenceType, Long> {
    fun findByLicenceId(licenceId: String): LicenceType?
    fun existsByLicenceId(licenceId: String): Boolean
}
