package sg.gov.tech.molbagencyportalbackend.validator

import io.mockk.clearAllMocks
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import sg.gov.tech.molbagencyportalbackend.service.LicenceTypeService
import sg.gov.tech.testing.MolbUnitTesting
import sg.gov.tech.testing.verifyNever
import sg.gov.tech.testing.verifyOnce
import javax.validation.ConstraintValidatorContext

@MolbUnitTesting
class LicenceTypeExistValidatorTest {
    @MockK
    private lateinit var licenceTypeService: LicenceTypeService

    @InjectMockKs
    private lateinit var validator: LicenceTypeExistsValidator

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
        every { licenceTypeService.existByLicenceId(any()) } returns true
        Assertions.assertThat(validator.isValid("valid_licence_id", context)).isTrue()
        verifyNever { contextBuilder.addConstraintViolation() }
    }

    @Test
    fun `should fail if licence is not supported`() {
        every { licenceTypeService.existByLicenceId(any()) } returns false
        Assertions.assertThat(validator.isValid("invalid_licence_id", context)).isFalse()
        verifyOnce { contextBuilder.addConstraintViolation() }
    }
}
