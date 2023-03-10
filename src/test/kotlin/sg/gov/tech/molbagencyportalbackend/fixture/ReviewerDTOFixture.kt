package sg.gov.tech.molbagencyportalbackend.fixture

import sg.gov.tech.molbagencyportalbackend.dto.internal.user.ReviewerDTO
import sg.gov.tech.molbagencyportalbackend.model.UserStatus

object ReviewerDTOFixture {
    val reviewerDTOA = ReviewerDTO(
        id = 1,
        name = "Atest",
        accountStatus = UserStatus.ACTIVE,
        isDeleted = false
    )
    val reviewerDTOB = ReviewerDTO(
        id = 2,
        name = "Btest",
        accountStatus = UserStatus.ACTIVE,
        isDeleted = false
    )
}
