package sg.gov.tech.molbagencyportalbackend.service

import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import sg.gov.tech.molbagencyportalbackend.auth.AuthenticationFacade
import sg.gov.tech.molbagencyportalbackend.dao.JwtDao
import sg.gov.tech.molbagencyportalbackend.dto.internal.user.CreateUserRequestDTO
import sg.gov.tech.molbagencyportalbackend.dto.internal.user.EditUserRequestDTO
import sg.gov.tech.molbagencyportalbackend.dto.internal.user.ReassignUserDTOProjection
import sg.gov.tech.molbagencyportalbackend.dto.internal.user.UserDTO
import sg.gov.tech.molbagencyportalbackend.dto.internal.user.UserListRequestParams
import sg.gov.tech.molbagencyportalbackend.dto.internal.user.UserListResponseDTO
import sg.gov.tech.molbagencyportalbackend.dto.internal.user.UserModelTransfer
import sg.gov.tech.molbagencyportalbackend.exception.NotAuthorisedException
import sg.gov.tech.molbagencyportalbackend.exception.NotFoundException
import sg.gov.tech.molbagencyportalbackend.model.Authority
import sg.gov.tech.molbagencyportalbackend.model.User
import sg.gov.tech.molbagencyportalbackend.model.UserStatus
import sg.gov.tech.molbagencyportalbackend.repository.AgencyRepository
import sg.gov.tech.molbagencyportalbackend.repository.UserRepository
import sg.gov.tech.molbagencyportalbackend.repository.findOne
import kotlin.collections.HashMap

@Service
@Transactional
class UserService(
    private val roleService: RoleService,
    private val userRepository: UserRepository,
    private val agencyRepository: AgencyRepository,
    private val userModelTransfer: UserModelTransfer,
    private val authenticationFacade: AuthenticationFacade,
    private val jwtDao: JwtDao
) {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    fun getUserByEmailAndIsDeletedFalse(email: String): User? =
        userRepository.findByEmailIgnoreCaseAndIsDeletedFalse(email)

    fun getUserById(userId: Long): User? = userRepository.findOne(userId)

    fun existsByEmailAndIsDeletedFalse(email: String): Boolean =
        userRepository.existsByEmailIgnoreCaseAndIsDeletedFalse(email)

    fun getAgencyUsers(requestParams: UserListRequestParams): UserListResponseDTO {
        val principal = getUserByEmailAndIsDeletedFalse(authenticationFacade.getPrincipalName())!!
        val sortField =
            if (requestParams.sortField == "role") "role.name" else requestParams.sortField
        val usersPage =
            userRepository.findAllByAgencyIdAndIsDeletedFalse(
                principal.agencyId,
                PageRequest.of(
                    requestParams.page,
                    requestParams.limit,
                    Sort.by(
                        Sort.Order(
                            Sort.Direction.valueOf(requestParams.sortOrder.uppercase()),
                            sortField
                        ).ignoreCase()
                    )
                )
            ).toUsersPage()
        return UserListResponseDTO(data = usersPage.content, totalCount = usersPage.totalElements)
    }

    fun getAgencyReassignUsers(userId: Long, authorityCode: String): List<ReassignUserDTOProjection> {
        val principal = getUserByEmailAndIsDeletedFalse(authenticationFacade.getPrincipalName())!!
        return userRepository.getUsersForReassignAsProjection(
            principal.agencyId!!,
            authorityCode,
            userId
        )
    }

    fun getAgencyUser(userId: Long): UserDTO {
        val user = getAndValidateUserByUserId(userId)
        return userModelTransfer.toDTO(user)
    }

    private fun Page<User>.toUsersPage() =
        PageImpl(content.map { userModelTransfer.toDTO(it) }, pageable, totalElements)

    fun createAgencyUser(requestDTO: CreateUserRequestDTO): UserDTO {
        val principal = getUserByEmailAndIsDeletedFalse(authenticationFacade.getPrincipalName())!!

        val newUserRole = roleService.findByCode(requestDTO.role)
        val newUserEntity = User(
            agencyId = principal.agencyId,
            name = requestDTO.name,
            email = requestDTO.email.lowercase(),
            status = UserStatus.valueOf(requestDTO.accountStatus),
            role = newUserRole!!
        )

        val newUser = userRepository.save(newUserEntity)

        logger.info("Successfully added user with id: ${newUser.id}")

        return userModelTransfer.toDTO(newUser)
    }

    fun updateAgencyUser(userId: Long, requestDTO: EditUserRequestDTO): UserDTO {
        val user = getAndValidateUserByUserId(userId)
        val oldRole = user.role.copy()

        val userRole = roleService.findByCode(requestDTO.role)

        val updatedUser = userRepository.save(
            user.copy(
                name = requestDTO.name,
                status = UserStatus.valueOf(requestDTO.accountStatus),
                role = userRole!!
            )
        )

        logger.info("Successfully updated user with id: ${updatedUser.id}")

        if (updatedUser.status == UserStatus.INACTIVE || oldRole != userRole) {
            jwtDao.remove(updatedUser.email)
        }

        return userModelTransfer.toDTO(updatedUser)
    }

    fun removeUser(userId: Long) {
        val user = getAndValidateUserByUserId(userId)

        // soft delete user
        userRepository.save(user.copy(isDeleted = true))

        logger.info("Successfully deleted user with id: $userId")
    }

    fun getAndValidateUserByEmail(email: String): User {
        val user = getUserByEmailAndIsDeletedFalse(email)
            ?: throw NotFoundException("User not found, email: $email")

        if (user.status == UserStatus.INACTIVE)
            throw NotAuthorisedException("User is Inactive, email: $email")

        return user
    }

    private fun getAndValidateUserByUserId(userId: Long): User {
        val user = getUserById(userId) ?: throw NotFoundException("User not found, userId: $userId")
        val principal = getUserByEmailAndIsDeletedFalse(authenticationFacade.getPrincipalName())!!

        when {
            user.isDeleted -> throw NotFoundException("User has already been deleted: ${user.id}")
            principal.agencyId != user.agencyId ->
                throw NotAuthorisedException(
                    "User ${authenticationFacade.getPrincipalName()} is not authorised to access other agency user: " +
                        "${user.id}"
                )
        }

        return user
    }

    fun getUserAuthorities(authorities: List<Authority>): List<String> {
        return authorities.map { it.code }
    }

    fun getUserInfo(user: User): HashMap<String, Any> {
        val userInfo = HashMap<String, Any>()
        userInfo["id"] = user.id!!
        val agency = user.agencyId?.let { agencyRepository.findById(it).orElse(null) }
        if (agency != null) {
            userInfo["agencyCode"] = agency.code
            userInfo["agencyName"] = agency.name
            return userInfo
        }
        throw NotFoundException("Agency Not Found with ${user.agencyId}")
    }
}
