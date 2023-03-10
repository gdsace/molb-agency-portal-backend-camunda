package sg.gov.tech.molbagencyportalbackend.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import sg.gov.tech.molbagencyportalbackend.model.Licence
import sg.gov.tech.molbagencyportalbackend.model.LicenceStatus
import java.time.LocalDate

@Repository
interface LicenceRepository : JpaRepository<Licence, Long> {
    fun existsByLicenceNumber(licenceNumber: String): Boolean
    fun findByApplicationId(applicationId: Long): Licence?
    fun findByLicenceNumber(licenceNumber: String): Licence?

    fun findAllByStartDateIsLessThanEqualAndStatus(
        runDate: LocalDate,
        status: LicenceStatus
    ): List<Licence>?

    fun findAllByExpiryDateIsLessThanAndStatus(
        runDate: LocalDate,
        status: LicenceStatus
    ): List<Licence>?

    fun countLicenceByApplicationAgencyId(
        agencyId: Long,
    ): Int

    fun countLicenceByApplicationAgencyIdNot(
        agencyId: Long,
    ): Int

    fun findLicenceByApplicationAgencyIdNot(
        agencyId: Long,
        pageable: Pageable
    ): Page<Licence>

    fun findLicenceByApplicationAgencyId(
        agencyId: Long,
        pageable: Pageable
    ): Page<Licence>
}
