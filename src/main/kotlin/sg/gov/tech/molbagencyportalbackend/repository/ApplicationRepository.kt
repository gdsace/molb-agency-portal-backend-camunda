package sg.gov.tech.molbagencyportalbackend.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import sg.gov.tech.molbagencyportalbackend.model.Application
import sg.gov.tech.molbagencyportalbackend.model.ApplicationStatus

@Repository
interface ApplicationRepository : JpaRepository<Application, Long> {
    fun existsByApplicationNumber(applicationNumber: String): Boolean

    // for unassignedCases
    fun countAllByAgencyIdAndReviewerIdIsNullAndStatusNotIn(
        agencyId: Long,
        status: List<ApplicationStatus>
    ): Int
    fun findAllByAgencyIdAndReviewerIdIsNullAndStatusNotIn(
        agencyId: Long,
        status: List<ApplicationStatus>,
        pageable: Pageable
    ): Page<Application>

    // for openCases
    fun countAllByAgencyIdAndReviewerIdAndStatusIn(
        agencyId: Long,
        reviewerId: Long,
        status: List<ApplicationStatus>
    ): Int
    fun findAllByAgencyIdAndReviewerIdAndStatusIn(
        agencyId: Long,
        reviewerId: Long,
        status: List<ApplicationStatus>,
        pageable: Pageable
    ): Page<Application>

    fun countAllByAgencyId(
        agencyId: Long
    ): Int

    fun findAllByAgencyId(
        agencyId: Long,
        pageable: Pageable
    ): Page<Application>

    fun findByApplicationNumber(applicationNumber: String): Application?
}
