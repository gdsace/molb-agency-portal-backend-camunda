package sg.gov.tech.molbagencyportalbackend.fixture

import sg.gov.tech.molbagencyportalbackend.dto.internal.user.CreateUserRequestDTO
import sg.gov.tech.molbagencyportalbackend.dto.internal.user.EditUserRequestDTO
import sg.gov.tech.molbagencyportalbackend.model.User
import sg.gov.tech.molbagencyportalbackend.model.UserStatus

object UserFixture {
    val userA = User(
        id = 1,
        agencyId = 1,
        name = "Atest",
        email = "atest@test.com",
        status = UserStatus.ACTIVE,
        role = RoleModelFixture.role,
        createdBy = null,
        createdAt = null,
        updatedBy = null,
        updatedAt = null
    )

    val userB = User(
        id = 2,
        agencyId = 1,
        name = "Btest",
        email = "btest@test.com",
        status = UserStatus.ACTIVE,
        role = RoleModelFixture.role,
        createdBy = null,
        createdAt = null,
        updatedBy = null,
        updatedAt = null
    )

    val userOfDifferentAgency = User(
        id = 3,
        agencyId = 2,
        name = "diff agency",
        email = "diff@agency.com",
        status = UserStatus.INACTIVE,
        role = RoleModelFixture.role,
        createdBy = null,
        createdAt = null,
        updatedBy = null,
        updatedAt = null
    )

    val inactiveUser = User(
        id = 4,
        agencyId = 1,
        name = "inactive user",
        email = "inactive@agency.com",
        status = UserStatus.INACTIVE,
        role = RoleModelFixture.role,
        createdBy = null,
        createdAt = null,
        updatedBy = null,
        updatedAt = null
    )

    val createUserRequestDTO = CreateUserRequestDTO(
        name = "Test",
        email = "test_create@tech.gov.sg",
        role = "agency_supervisor",
        accountStatus = "INACTIVE"
    )

    val editUserRequestDTO = EditUserRequestDTO(
        name = "Test",
        role = "agency_supervisor",
        accountStatus = "INACTIVE"
    )
}
