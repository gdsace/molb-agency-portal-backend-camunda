package sg.gov.tech.molbagencyportalbackend.fixture

import sg.gov.tech.molbagencyportalbackend.dto.internal.user.RoleDTO
import sg.gov.tech.molbagencyportalbackend.model.Role

object RoleModelFixture {
    val role = Role(
        name = "Agency Supervisor",
        id = 1,
        code = "agency_supervisor",
        users = mutableListOf(),
        authorities = AuthorityModelFixture.authority
    )
    val officerRole = Role(
        name = "Agency Officer",
        id = 2,
        code = "agency_officer",
        users = mutableListOf(),
        authorities = AuthorityModelFixture.officerAuthority
    )
    val officerReadOnlyRole = Role(
        name = "Agency Officer (Read Only)",
        id = 3,
        code = "agency_officer_ro",
        users = mutableListOf(),
        authorities = mutableListOf()
    )
    val roleList = listOf(
        Role(
            name = "Agency Supervisor",
            id = 1,
            code = "agency_supervisor",
            users = mutableListOf(),
            authorities = AuthorityModelFixture.authority
        ),
        Role(
            name = "Agency Officer",
            id = 2,
            code = "agency_officer",
            users = mutableListOf(),
            authorities = AuthorityModelFixture.officerAuthority
        ),
        Role(
            name = "System Admin",
            id = 3,
            code = "system_admin",
            users = mutableListOf(),
            authorities = AuthorityModelFixture.systemAdminAuthority
        )
    )

    val roleAS = RoleDTO(
        name = "Agency Supervisor",
        id = 1,
        code = "agency_supervisor"
    )

    val roleAO = RoleDTO(
        name = "Agency Officer",
        id = 2,
        code = "agency_officer"
    )

    val roleSA = RoleDTO(
        name = "System Admin",
        id = 3,
        code = "system_admin"
    )
}
