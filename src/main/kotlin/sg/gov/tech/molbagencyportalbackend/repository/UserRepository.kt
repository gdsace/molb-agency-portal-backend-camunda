package sg.gov.tech.molbagencyportalbackend.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import sg.gov.tech.molbagencyportalbackend.dto.internal.user.ReassignUserDTOProjection
import sg.gov.tech.molbagencyportalbackend.model.User

@Repository
interface UserRepository : JpaRepository<User, Long> {
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun findByEmailIgnoreCaseAndIsDeletedFalse(email: String): User?
    fun findAllByAgencyIdAndIsDeletedFalse(agencyId: Long?, pageable: Pageable): Page<User>
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun existsByEmailIgnoreCaseAndIsDeletedFalse(email: String): Boolean

    @Query(nativeQuery = true)
    fun getUsersForReassignAsProjection(agencyId: Long, authorityCode: String, currentUserID: Long):
        List<ReassignUserDTOProjection>
}
