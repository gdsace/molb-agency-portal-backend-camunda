package sg.gov.tech.molbagencyportalbackend.service

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verifyOrder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import sg.gov.tech.molbagencyportalbackend.auth.AuthenticationFacade
import sg.gov.tech.molbagencyportalbackend.dao.JwtDao
import sg.gov.tech.molbagencyportalbackend.dto.internal.user.UserModelTransfer
import sg.gov.tech.molbagencyportalbackend.exception.NotAuthorisedException
import sg.gov.tech.molbagencyportalbackend.exception.NotFoundException
import sg.gov.tech.molbagencyportalbackend.fixture.AgencyFixture
import sg.gov.tech.molbagencyportalbackend.fixture.UserFixture
import sg.gov.tech.molbagencyportalbackend.fixture.UserListRequestDTOFixture
import sg.gov.tech.molbagencyportalbackend.fixture.UserListResponseDTOFixture
import sg.gov.tech.molbagencyportalbackend.model.User
import sg.gov.tech.molbagencyportalbackend.repository.AgencyRepository
import sg.gov.tech.molbagencyportalbackend.repository.UserRepository
import sg.gov.tech.testing.MolbUnitTesting
import sg.gov.tech.testing.messageEqualTo
import java.util.Optional

@MolbUnitTesting
class UserServiceTest {
    @MockK
    private lateinit var roleService: RoleService

    @MockK
    private lateinit var userRepository: UserRepository

    @MockK
    private lateinit var userModelTransfer: UserModelTransfer

    @MockK
    private lateinit var jwtDao: JwtDao

    @InjectMockKs
    private lateinit var userService: UserService

    @MockK
    private lateinit var authenticationFacade: AuthenticationFacade

    @MockK
    private lateinit var agencyRepository: AgencyRepository

    @Nested
    inner class GetUsers {
        @Test
        fun `should return user list when request is valid`() {
            val pageRequest = PageRequest.of(
                UserListRequestDTOFixture.userListRequestParam.page,
                UserListRequestDTOFixture.userListRequestParam.limit,
                Sort.by(
                    Sort.Order(
                        Sort.Direction.valueOf(UserListRequestDTOFixture.userListRequestParam.sortOrder.uppercase()),
                        UserListRequestDTOFixture.userListRequestParam.sortField
                    ).ignoreCase()
                )
            )
            val userList = mockk<Page<User>> {
                every { content } returns listOf(UserFixture.userA, UserFixture.userB)
                every { pageable } returns pageRequest
                every { totalElements } returns 2
            }
            every { authenticationFacade.getPrincipalName() } returns "supervisor@tech.gov.sg"
            every { userRepository.findByEmailIgnoreCaseAndIsDeletedFalse(any()) } returns UserFixture.userA
            every {
                userRepository.findAllByAgencyIdAndIsDeletedFalse(
                    UserFixture.userA.agencyId,
                    pageRequest
                )
            } returns userList
            every { userModelTransfer.toDTO(UserFixture.userA) } returns UserListResponseDTOFixture.userDTOA
            every { userModelTransfer.toDTO(UserFixture.userB) } returns UserListResponseDTOFixture.userDTOB

            val userListResponseDTO =
                userService.getAgencyUsers(UserListRequestDTOFixture.userListRequestParam)
            assertEquals(2, userListResponseDTO.totalCount)
            assertEquals("Atest", userListResponseDTO.data[0].name)
        }
    }

    @Nested
    inner class GetAndValidateUserByEmail {
        @Test
        fun `should return User record even if the email does not match case`() {
            every { userRepository.findByEmailIgnoreCaseAndIsDeletedFalse(any()) } returns UserFixture.userA

            val user = userService.getAndValidateUserByEmail("ATEST@TEST.COM")

            assertEquals("atest@test.com", user.email)
        }

        @Test
        fun `should throw not found exception if user does not exist `() {
            every { userRepository.findByEmailIgnoreCaseAndIsDeletedFalse(any()) } returns null

            assertThrows<NotFoundException> {
                userService.getAndValidateUserByEmail("ATEST@TEST.COM")
            }.messageEqualTo("User not found, email: ATEST@TEST.COM")
        }

        @Test
        fun `should throw NotAuthorisedException if user is inactive `() {
            val userEmail = UserFixture.inactiveUser.email
            every { userRepository.findByEmailIgnoreCaseAndIsDeletedFalse(any()) } returns UserFixture.inactiveUser

            assertThrows<NotAuthorisedException> {
                userService.getAndValidateUserByEmail(userEmail)
            }.messageEqualTo("User is Inactive, email: $userEmail")
        }
    }

    @Nested
    inner class GetSingleUser {
        @Test
        fun `should return user when request is valid`() {
            every { userRepository.findById(any()) } returns Optional.of(UserFixture.userA)
            every { userService.getUserByEmailAndIsDeletedFalse(any()) } returns UserFixture.userB
            every { userModelTransfer.toDTO(any()) } returns UserListResponseDTOFixture.userDTOA

            val userDTO = userService.getAgencyUser(1)

            assertEquals(1, userDTO.id)
            assertEquals("atest@test.com", userDTO.email)
        }

        @Test
        fun `should throw not found error if principal is not found`() {
            every { userRepository.findById(any()) } returns Optional.empty()

            assertThrows<NotFoundException> {
                userService.getAgencyUser(1)
            }.messageEqualTo("User not found, userId: 1")
        }

        @Test
        fun `should throw not authorised error if user is not in the same agency as principal`() {
            val principalEmail = "supervisors@tech.gov.sg"

            every { authenticationFacade.getPrincipalName() } returns principalEmail
            every { userRepository.findById(any()) } returns Optional.of(UserFixture.userOfDifferentAgency)
            every { userRepository.findByEmailIgnoreCaseAndIsDeletedFalse(any()) } returns UserFixture.userB

            assertThrows<NotAuthorisedException> {
                userService.getAgencyUser(1)
            }.messageEqualTo(
                "User $principalEmail is not authorised to access other agency user: " +
                    "${UserFixture.userOfDifferentAgency.id}"
            )
        }
    }

    @Nested
    inner class CreateUser {
        @Test
        fun `should create user when valid payload is provided`() {
            every { authenticationFacade.getPrincipalName() } returns "supervisor@tech.gov.sg"
            every { userRepository.findByEmailIgnoreCaseAndIsDeletedFalse(any()) } returns UserFixture.userA
            every { roleService.findByCode(UserFixture.userA.role.code) } returns mockk()
            every { userRepository.save(any()) } returns UserFixture.userA
            every { userModelTransfer.toDTO(any()) } returns mockk()

            userService.createAgencyUser(UserFixture.createUserRequestDTO)

            verifyOrder {
                userRepository.save(any())
                userModelTransfer.toDTO(any())
            }
        }
    }

    @Nested
    inner class EditUser {
        @Test
        fun `should throw not found exception when invalid id is provided`() {
            every { userRepository.findById(any()) } returns Optional.empty()

            assertThrows<NotFoundException> {
                userService.updateAgencyUser(1, UserFixture.editUserRequestDTO)
            }.messageEqualTo("User not found, userId: 1")
        }

        @Test
        fun `should throw not authorised error if user is not in the same agency as principal`() {
            val principalEmail = "supervisors@tech.gov.sg"

            every { authenticationFacade.getPrincipalName() } returns principalEmail
            every { userRepository.findById(any()) } returns Optional.of(UserFixture.userOfDifferentAgency)
            every { userRepository.findByEmailIgnoreCaseAndIsDeletedFalse(any()) } returns UserFixture.userB

            assertThrows<NotAuthorisedException> {
                userService.updateAgencyUser(1, UserFixture.editUserRequestDTO)
            }.messageEqualTo(
                "User $principalEmail is not authorised to access other agency user: " +
                    "${UserFixture.userOfDifferentAgency.id}"
            )
        }

        @Test
        fun `should update user when valid payload is provided`() {
            every { userRepository.findById(any()) } returns Optional.of(UserFixture.userA)
            every { userRepository.findByEmailIgnoreCaseAndIsDeletedFalse(any()) } returns UserFixture.userB
            every { roleService.findByCode(UserFixture.userA.role.code) } returns mockk()
            every { userRepository.save(any()) } returns UserFixture.userA
            every { userModelTransfer.toDTO(any()) } returns mockk()
            every { jwtDao.remove(any()) } returns Unit

            userService.updateAgencyUser(1, UserFixture.editUserRequestDTO)

            verifyOrder {
                userRepository.save(any())
                userModelTransfer.toDTO(any())
            }
        }
    }

    @Nested
    inner class RemoveUser {
        @Test
        fun `should throw not found exception when invalid id is provided`() {
            every { userRepository.findById(any()) } returns Optional.empty()

            assertThrows<NotFoundException> {
                userService.removeUser(1)
            }.messageEqualTo("User not found, userId: 1")
        }

        @Test
        fun `should throw not authorised error if user is not in the same agency as principal`() {
            val principalEmail = "supervisors@tech.gov.sg"

            every { authenticationFacade.getPrincipalName() } returns principalEmail
            every { userRepository.findById(any()) } returns Optional.of(UserFixture.userOfDifferentAgency)
            every { userRepository.findByEmailIgnoreCaseAndIsDeletedFalse(any()) } returns UserFixture.userB

            assertThrows<NotAuthorisedException> {
                userService.removeUser(1)
            }.messageEqualTo(
                "User $principalEmail is not authorised to access other agency user: " +
                    "${UserFixture.userOfDifferentAgency.id}"
            )
        }

        @Test
        fun `should delete user when valid id is provided`() {
            val principalEmail = "supervisors@tech.gov.sg"
            val userId = 1L

            every { authenticationFacade.getPrincipalName() } returns principalEmail
            every { userRepository.existsById(any()) } returns true
            every { userRepository.deleteById(any()) } just runs
            every { userRepository.findById(userId) } returns Optional.of(UserFixture.userA)
            every { userRepository.findByEmailIgnoreCaseAndIsDeletedFalse(any()) } returns UserFixture.userB
            every { userRepository.flush() } just runs

            userService.removeUser(userId)

            verifyOrder {
                userRepository.save(any())
            }
        }

        @Test
        fun `should return agency information if agencyId is provided`() {
            every {
                agencyRepository.findById(UserFixture.userA.agencyId!!).orElse(null)
            } returns AgencyFixture.agency
            val userInfo =
                userService.getUserInfo(UserFixture.userA)
            assertEquals("ta", userInfo!!["agencyCode"])
            assertEquals("Test Agency", userInfo!!["agencyName"])
        }

        @Test
        fun `should throw agency not found if agencyId is provided`() {
            every {
                agencyRepository.findById(UserFixture.userA.agencyId!!).orElse(null)
            } returns null
            assertThrows<NotFoundException> {
                userService.getUserInfo(UserFixture.userA)
            }.messageEqualTo("Agency Not Found with 1")
        }
    }
}
