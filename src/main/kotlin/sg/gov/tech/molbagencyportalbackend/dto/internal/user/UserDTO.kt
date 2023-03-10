package sg.gov.tech.molbagencyportalbackend.dto.internal.user

import org.springframework.stereotype.Component
import sg.gov.tech.molbagencyportalbackend.annotation.ExcludeFromGeneratedCoverageTest
import sg.gov.tech.molbagencyportalbackend.dto.ModelTransfer
import sg.gov.tech.molbagencyportalbackend.model.User
import sg.gov.tech.molbagencyportalbackend.model.UserStatus

@ExcludeFromGeneratedCoverageTest
data class UserDTO(
    val id: Long,
    val name: String,
    val email: String,
    val role: RoleDTO,
    val accountStatus: UserStatus,
    val isDeleted: Boolean
)

@Component
@ExcludeFromGeneratedCoverageTest
class UserModelTransfer : ModelTransfer<User, UserDTO> {
    override fun toDTO(model: User): UserDTO {
        return UserDTO(
            id = model.id!!,
            name = model.name,
            email = model.email,
            role = RoleDTO(
                id = model.role.id!!,
                code = model.role.code,
                name = model.role.name
            ),
            accountStatus = model.status,
            isDeleted = model.isDeleted
        )
    }
}

@ExcludeFromGeneratedCoverageTest
data class ReassignUserDTOProjection(
    val id: Long,
    val name: String,
    val email: String,
)
