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
import sg.gov.tech.molbagencyportalbackend.auth.AuthenticationFacade
import sg.gov.tech.molbagencyportalbackend.service.UserService
import sg.gov.tech.testing.MolbUnitTesting
import sg.gov.tech.testing.verifyNever
import sg.gov.tech.testing.verifyOnce
import javax.validation.ConstraintValidatorContext

@MolbUnitTesting
internal class UserEmailValidatorTest {
    @MockK
    private lateinit var userService: UserService

    @InjectMockKs
    private lateinit var validator: UserEmailValidator

    @MockK
    private lateinit var authenticationFacade: AuthenticationFacade

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
        val userEmail = "valid@tech.gov.sg"
        every { authenticationFacade.getPrincipalName() } returns "supervisor@tech.gov.sg"
        every { userService.existsByEmailAndIsDeletedFalse(any()) } returns false

        assertThat(validator.isValid(userEmail, context)).isTrue
        verifyNever { contextBuilder.addConstraintViolation() }
    }

    @Test
    fun `should pass even if email is in different case`() {
        val userEmail = "VALID@TECH.GOV.SG"
        every { authenticationFacade.getPrincipalName() } returns "supervisor@tech.gov.sg"
        every { userService.existsByEmailAndIsDeletedFalse(any()) } returns false

        assertThat(validator.isValid(userEmail, context)).isTrue
        verifyNever { contextBuilder.addConstraintViolation() }
    }

    @Test
    fun `should fail if user email already exists`() {
        val userEmail = "exists@tech.gov.sg"
        every { authenticationFacade.getPrincipalName() } returns "supervisor@tech.gov.sg"
        every { userService.existsByEmailAndIsDeletedFalse(any()) } returns true

        assertThat(validator.isValid(userEmail, context)).isFalse
        verifyOnce { contextBuilder.addConstraintViolation() }
    }

    @Test
    fun `should fail if user email is not in the supported domain`() {
        val userEmail = "invalid_domain@spf.com.sg"
        every { authenticationFacade.getPrincipalName() } returns "supervisor@tech.gov.sg"
        every { userService.existsByEmailAndIsDeletedFalse(any()) } returns false
        assertThat(validator.isValid(userEmail, context)).isFalse
        verifyOnce { contextBuilder.addConstraintViolation() }
    }

    @Test
    fun `should fail if user email is not in the same domain as supervisor`() {
        val userEmail = "diff_domain@moe.gov.sg"
        every { authenticationFacade.getPrincipalName() } returns "supervisor@tech.gov.sg"
        every { userService.existsByEmailAndIsDeletedFalse(any()) } returns false

        assertThat(validator.isValid(userEmail, context)).isFalse
        verifyOnce { contextBuilder.addConstraintViolation() }
    }
}
