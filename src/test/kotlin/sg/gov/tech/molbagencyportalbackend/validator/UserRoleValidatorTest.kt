package sg.gov.tech.molbagencyportalbackend.validator

import io.mockk.clearAllMocks
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import sg.gov.tech.molbagencyportalbackend.service.RoleService
import sg.gov.tech.testing.MolbUnitTesting
import sg.gov.tech.testing.verifyNever
import sg.gov.tech.testing.verifyOnce
import javax.validation.ConstraintValidatorContext

@MolbUnitTesting
internal class UserRoleValidatorTest {
    @MockK
    private lateinit var roleService: RoleService

    @InjectMockKs
    private lateinit var validator: UserRoleValidator

    private val context = mockk<ConstraintValidatorContext>()
    private val contextBuilder = mockk<ConstraintValidatorContext.ConstraintViolationBuilder>()

    @BeforeEach
    fun setUp() {
        every { context.disableDefaultConstraintViolation() } just runs
        every { context.buildConstraintViolationWithTemplate(any()) } returns contextBuilder
        every { contextBuilder.addConstraintViolation() } returns context
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
        clearMocks(context)
        clearMocks(contextBuilder)
    }

    @Test
    fun `should pass`() {
        val role = "valid_role_code"

        every { roleService.existByCode(any()) } returns true

        assertThat(validator.isValid(role, context)).isTrue()
        verifyNever { contextBuilder.addConstraintViolation() }
    }

    @Test
    fun `should fail if role is not supported`() {
        val role = "invalid_role_code"

        every { roleService.existByCode(any()) } returns false

        assertThat(validator.isValid(role, context)).isFalse()
        verifyOnce { contextBuilder.addConstraintViolation() }
    }
}
