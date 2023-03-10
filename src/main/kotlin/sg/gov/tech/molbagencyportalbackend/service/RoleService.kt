package sg.gov.tech.molbagencyportalbackend.service

import org.springframework.stereotype.Service
import sg.gov.tech.molbagencyportalbackend.dto.internal.user.RoleDTO
import sg.gov.tech.molbagencyportalbackend.dto.internal.user.RoleModelTransfer
import sg.gov.tech.molbagencyportalbackend.model.Role
import sg.gov.tech.molbagencyportalbackend.repository.RoleRepository

@Service
class RoleService(
    private val roleRepository: RoleRepository,
    private val roleModelTransfer: RoleModelTransfer
) {
    fun findByCode(code: String): Role? = roleRepository.findByCode(code)
    fun existByCode(code: String): Boolean = roleRepository.existsByCode(code)
    fun findAll(): List<Role> = roleRepository.findAll()

    fun getAllRoles(): List<RoleDTO> {
        return getAllRolesFilter(listOf("system_admin"))
    }

    private fun getAllRolesFilter(codes: List<String>): List<RoleDTO> =
        findAll().filterNot { it.code in codes }.map { roleModelTransfer.toDTO(it) }
}
