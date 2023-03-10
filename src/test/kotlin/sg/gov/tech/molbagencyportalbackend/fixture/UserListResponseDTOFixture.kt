package sg.gov.tech.molbagencyportalbackend.fixture

import sg.gov.tech.molbagencyportalbackend.dto.internal.user.RoleDTO
import sg.gov.tech.molbagencyportalbackend.dto.internal.user.UserDTO
import sg.gov.tech.molbagencyportalbackend.model.UserStatus

object UserListResponseDTOFixture {
    val userDTOA = UserDTO(
        id = 1,
        name = "Atest",
        email = "atest@test.com",
        role = RoleFixture.roleAgencySupervisor,
        accountStatus = UserStatus.ACTIVE,
        isDeleted = false
    )
    val userDTOB = UserDTO(
        id = 2,
        name = "Btest",
        email = "btest@test.com",
        role = RoleFixture.roleAgencySupervisor,
        accountStatus = UserStatus.ACTIVE,
        isDeleted = false
    )
}

object RoleFixture {
    val roleAgencySupervisor = RoleDTO(
        name = "Agency Supervisor",
        id = 1,
        code = "agency_supervisor"
    )
}
