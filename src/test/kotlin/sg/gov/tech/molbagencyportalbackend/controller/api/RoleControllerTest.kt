package sg.gov.tech.molbagencyportalbackend.controller.api

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import org.junit.jupiter.api.Test
import sg.gov.tech.molbagencyportalbackend.service.RoleService
import sg.gov.tech.testing.MolbUnitTesting
import sg.gov.tech.testing.verifyOnce

@MolbUnitTesting
internal class RoleControllerTest {
    @MockK
    private lateinit var roleService: RoleService

    @InjectMockKs
    private lateinit var roleController: RoleController

    @Test
    fun `should create user successfully`() {
        every { roleService.getAllRoles() } returns mockk()

        roleController.getAgencyUsers()

        verifyOnce { roleService.getAllRoles() }
    }
}
