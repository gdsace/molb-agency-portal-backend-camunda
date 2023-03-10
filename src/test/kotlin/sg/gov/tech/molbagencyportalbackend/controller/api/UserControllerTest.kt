package sg.gov.tech.molbagencyportalbackend.controller.api

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import org.junit.jupiter.api.Test
import sg.gov.tech.molbagencyportalbackend.dto.internal.user.CreateUserRequestDTO
import sg.gov.tech.molbagencyportalbackend.dto.internal.user.EditUserRequestDTO
import sg.gov.tech.molbagencyportalbackend.service.UserService
import sg.gov.tech.testing.MolbUnitTesting
import sg.gov.tech.testing.verifyOnce

@MolbUnitTesting
internal class UserControllerTest {
    @MockK
    private lateinit var userService: UserService

    @InjectMockKs
    private lateinit var userController: UserController

    @Test
    fun `should create user successfully`() {
        every { userService.createAgencyUser(any()) } returns mockk()

        userController.createAgencyUser(
            CreateUserRequestDTO(
                name = "Test",
                email = "test@test.com",
                role = "Test_Role",
                accountStatus = "ACTIVE"
            )
        )

        verifyOnce { userService.createAgencyUser(any()) }
    }

    @Test
    fun `should update user successfully`() {
        val userId = 1L
        every { userService.updateAgencyUser(any(), any()) } returns mockk()

        userController.updateAgencyUser(
            userId,
            EditUserRequestDTO(
                name = "Test",
                role = "Test_Role",
                accountStatus = "ACTIVE"
            )
        )

        verifyOnce { userService.updateAgencyUser(userId, any()) }
    }

    @Test
    fun `should delete user successfully`() {
        val userId = 1L
        every { userService.removeUser(userId) } just runs

        userController.deleteUser(userId)

        verifyOnce { userService.removeUser(userId) }
    }

    @Test
    fun `should should retrieve single user`() {
        val userId = 1L
        every { userService.getAgencyUser(userId) } returns mockk()

        userController.getAgencyUser(userId)

        verifyOnce { userService.getAgencyUser(userId) }
    }
}
