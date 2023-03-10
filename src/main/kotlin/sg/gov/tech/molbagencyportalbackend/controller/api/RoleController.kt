package sg.gov.tech.molbagencyportalbackend.controller.api

import org.slf4j.LoggerFactory
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import sg.gov.tech.molbagencyportalbackend.dto.internal.user.RoleDTO
import sg.gov.tech.molbagencyportalbackend.service.RoleService

@RestController
@RequestMapping("/api")
class RoleController(
    private val roleService: RoleService
) {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    @PreAuthorize("hasAuthority('add_user') or hasAuthority('edit_user')")
    @GetMapping("/roles")
    fun getAgencyUsers(): List<RoleDTO> {
        logger.info("Retrieving all roles")
        return roleService.getAllRoles()
    }
}
