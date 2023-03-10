package sg.gov.tech.molbagencyportalbackend.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import sg.gov.tech.molbagencyportalbackend.model.RFA
import sg.gov.tech.molbagencyportalbackend.model.RFAStatus

@Repository
interface RFARepository : JpaRepository<RFA, Long> {
    fun existsByApplicationIdAndStatus(applicationId: Long, status: RFAStatus): Boolean

    fun findAllByApplicationId(applicationId: Long, pageable: Pageable): Page<RFA>

    fun countAllByApplicationId(applicationId: Long): Int

    fun getRFAByApplicationIdAndStatus(applicationId: Long, status: RFAStatus): List<RFA>

    fun getFirstByApplicationIdOrderByIdDesc(applicationId: Long): RFA?
}
