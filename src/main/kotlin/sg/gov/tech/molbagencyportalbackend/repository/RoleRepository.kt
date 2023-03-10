package sg.gov.tech.molbagencyportalbackend.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import sg.gov.tech.molbagencyportalbackend.model.Role

@Repository
interface RoleRepository : JpaRepository<Role, Long> {
    fun findByCode(code: String): Role?
    fun existsByCode(code: String): Boolean
}
