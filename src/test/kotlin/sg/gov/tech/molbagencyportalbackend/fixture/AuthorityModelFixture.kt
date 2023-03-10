package sg.gov.tech.molbagencyportalbackend.fixture

import sg.gov.tech.molbagencyportalbackend.model.Authority

object AuthorityModelFixture {
    val authority = listOf(
        Authority(
            id = 1,
            name = "Process Application",
            code = "process_application"
        ),
        Authority(
            id = 2,
            name = "Reassign All",
            code = "reassign_all"
        ),

    )
    val officerAuthority = listOf(
        Authority(
            id = 1,
            name = "Process Application",
            code = "process_application"
        ),
        Authority(
            id = 3,
            name = "Reassign Self",
            code = "reassign_self"
        ),
    )
    val systemAdminAuthority = listOf(
        Authority(
            id = 1,
            name = "Delete User",
            code = "delete_user"
        ),
        Authority(
            id = 5,
            name = "Edit User",
            code = "edit_user"
        ),
        Authority(
            id = 4,
            name = "Add User",
            code = "add_user"
        )

    )
}
