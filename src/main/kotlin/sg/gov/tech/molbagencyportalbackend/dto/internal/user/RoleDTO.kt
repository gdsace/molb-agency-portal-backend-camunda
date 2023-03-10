package sg.gov.tech.molbagencyportalbackend.dto.internal.user

import org.springframework.stereotype.Component
import sg.gov.tech.molbagencyportalbackend.annotation.ExcludeFromGeneratedCoverageTest
import sg.gov.tech.molbagencyportalbackend.dto.ModelTransfer
import sg.gov.tech.molbagencyportalbackend.model.Role

@ExcludeFromGeneratedCoverageTest
data class RoleDTO(
    val id: Long,
    val code: String,
    val name: String
)

@Component
@ExcludeFromGeneratedCoverageTest
class RoleModelTransfer : ModelTransfer<Role, RoleDTO> {
    override fun toDTO(model: Role): RoleDTO {
        return RoleDTO(
            id = model.id!!,
            code = model.code,
            name = model.name
        )
    }
}
