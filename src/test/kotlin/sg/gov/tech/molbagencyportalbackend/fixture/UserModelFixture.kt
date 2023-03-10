package sg.gov.tech.molbagencyportalbackend.fixture

import sg.gov.tech.molbagencyportalbackend.model.User
import sg.gov.tech.molbagencyportalbackend.model.UserStatus

object UserModelFixture {
    val userA = User(
        id = 1,
        agencyId = 4,
        name = "Supervisor",
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
        agencyId = 4,
        name = "Btest",
        email = "btest@test.com",
        status = UserStatus.ACTIVE,
        role = RoleModelFixture.officerRole,
        createdBy = null,
        createdAt = null,
        updatedBy = null,
        updatedAt = null
    )
    val userC = User(
        id = 3,
        agencyId = 2,
        name = "Ctest",
        email = "ctest@test.com",
        status = UserStatus.ACTIVE,
        role = RoleModelFixture.role,
        createdBy = null,
        createdAt = null,
        updatedBy = null,
        updatedAt = null
    )

    val officerB = User(
        id = 3,
        agencyId = 4,
        name = "OfficerB",
        email = "btest@test.com",
        status = UserStatus.ACTIVE,
        role = RoleModelFixture.officerRole,
        createdBy = null,
        createdAt = null,
        updatedBy = null,
        updatedAt = null
    )

    val readOnlyUser = User(
        id = 4,
        agencyId = 4,
        name = "readOnlyOfficerA",
        email = "ctest@test.com",
        status = UserStatus.ACTIVE,
        role = RoleModelFixture.officerReadOnlyRole,
        createdBy = null,
        createdAt = null,
        updatedBy = null,
        updatedAt = null
    )

    val supervisorFromAgencyTwo = User(
        id = 7,
        agencyId = 2,
        name = "Ctest",
        email = "ctest@test.com",
        status = UserStatus.ACTIVE,
        role = RoleModelFixture.role,
        createdBy = null,
        createdAt = null,
        updatedBy = null,
        updatedAt = null
    )

    val inActiveUser = User(
        id = 8,
        agencyId = 4,
        name = "UserAB",
        email = "ctest@test.com",
        status = UserStatus.INACTIVE,
        role = RoleModelFixture.role,
        createdBy = null,
        createdAt = null,
        updatedBy = null,
        updatedAt = null
    )

    val deletedUser = User(
        id = 9,
        agencyId = 4,
        name = "UserD",
        email = "ctest@test.com",
        status = UserStatus.ACTIVE,
        isDeleted = true,
        role = RoleModelFixture.role,
        createdBy = null,
        createdAt = null,
        updatedBy = null,
        updatedAt = null
    )
}
