package sg.gov.tech.molbagencyportalbackend.dto.internal.user

import org.springframework.stereotype.Component
import sg.gov.tech.molbagencyportalbackend.annotation.ExcludeFromGeneratedCoverageTest
import sg.gov.tech.molbagencyportalbackend.dto.ModelTransfer
import sg.gov.tech.molbagencyportalbackend.model.User
import sg.gov.tech.molbagencyportalbackend.model.UserStatus

@ExcludeFromGeneratedCoverageTest
data class ReviewerDTO(
    val id: Long,
    val name: String,
    val accountStatus: UserStatus,
    val isDeleted: Boolean
)
@Component
@ExcludeFromGeneratedCoverageTest
class ReviewerModelTransfer : ModelTransfer<User, ReviewerDTO> {
    override fun toDTO(model: User): ReviewerDTO {
        return ReviewerDTO(
            id = model.id!!,
            name = model.name,
            accountStatus = model.status,
            isDeleted = model.isDeleted
        )
    }
}
