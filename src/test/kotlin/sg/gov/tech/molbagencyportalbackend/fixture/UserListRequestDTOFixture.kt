package sg.gov.tech.molbagencyportalbackend.fixture

import sg.gov.tech.molbagencyportalbackend.dto.internal.user.UserListRequestParams

object UserListRequestDTOFixture {
    val userListRequestParam = UserListRequestParams(
        page = 0,
        sortField = "name",
        sortOrder = "asc",
        limit = 2
    )
}
