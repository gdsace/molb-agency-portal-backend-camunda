package sg.gov.tech.molbagencyportalbackend.fixture

import sg.gov.tech.molbagencyportalbackend.dto.internal.user.ReassignUserDTOProjection

object ReassignUsersModelFixture {
    val usersFromAgencyTwo = listOf(
        ReassignUserDTOProjection(
            id = 1,
            name = "Atest",
            email = "atest@test.com"
        ),
        ReassignUserDTOProjection(
            id = 2,
            name = "Btest",
            email = "btest@test.com"
        ),
    )
}
