package sg.gov.tech.molbagencyportalbackend.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import sg.gov.tech.molbagencyportalbackend.model.Agency

@Repository
interface AgencyRepository : JpaRepository<Agency, Long> {
    fun findByCode(code: String): Agency?
    fun existsByCode(code: String): Boolean
}
