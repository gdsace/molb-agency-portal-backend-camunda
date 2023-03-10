package sg.gov.tech.molbagencyportalbackend.service

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import sg.gov.tech.molbagencyportalbackend.dto.internal.user.RoleModelTransfer
import sg.gov.tech.molbagencyportalbackend.fixture.RoleModelFixture
import sg.gov.tech.molbagencyportalbackend.repository.RoleRepository
import sg.gov.tech.testing.MolbUnitTesting

@MolbUnitTesting
internal class RoleServiceTest {
    @MockK
    private lateinit var roleRepository: RoleRepository

    @MockK
    private lateinit var roleModelTransfer: RoleModelTransfer

    @InjectMockKs
    private lateinit var roleService: RoleService

    @Test
    fun `Should return Agency given agencyCode exist`() {
        every { roleRepository.findByCode(any()) } returns RoleModelFixture.role
        val role = roleService.findByCode("valid_role_code")
        Assertions.assertEquals(RoleModelFixture.role, role)
    }

    @Test
    fun `Should return null given agencyCode does not exist`() {
        every { roleRepository.findByCode(any()) } returns null
        val agency = roleService.findByCode("invalid_role_code")
        Assertions.assertEquals(null, agency)
    }

    @Test
    fun `Should return true given agencyCode exist`() {
        every { roleRepository.existsByCode(any()) } returns true
        Assertions.assertTrue(roleService.existByCode("valid_role_code"))
    }

    @Test
    fun `Should return false given agencyCode does not exist`() {
        every { roleRepository.existsByCode(any()) } returns false
        Assertions.assertFalse(roleService.existByCode("invalid_role_code"))
    }

    @Test
    fun `Should return list of all roles`() {
        every { roleRepository.findAll() } returns RoleModelFixture.roleList
        val roleList = roleService.findAll()
        Assertions.assertEquals(3, roleList.size)
        Assertions.assertEquals(1, roleList[0].id)
        Assertions.assertEquals(2, roleList[1].id)
    }

    @Test
    fun `Should return list of all roles as DTO`() {
        every { roleRepository.findAll() } returns RoleModelFixture.roleList
        every { roleModelTransfer.toDTO(RoleModelFixture.roleList[0]) } returns RoleModelFixture.roleAS
        every { roleModelTransfer.toDTO(RoleModelFixture.roleList[1]) } returns RoleModelFixture.roleAO
        val roleListDTO = roleService.getAllRoles()
        Assertions.assertEquals(2, roleListDTO.size)
        Assertions.assertEquals(1, roleListDTO[0].id)
        Assertions.assertEquals("agency_supervisor", roleListDTO[0].code)
        Assertions.assertEquals("Agency Supervisor", roleListDTO[0].name)
        Assertions.assertEquals(2, roleListDTO[1].id)
        Assertions.assertEquals("agency_officer", roleListDTO[1].code)
        Assertions.assertEquals("Agency Officer", roleListDTO[1].name)
    }
}
