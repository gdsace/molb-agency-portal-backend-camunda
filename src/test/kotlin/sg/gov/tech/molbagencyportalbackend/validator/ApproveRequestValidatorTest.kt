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
import sg.gov.tech.molbagencyportalbackend.exception.ExceptionControllerAdvice
import sg.gov.tech.molbagencyportalbackend.fixture.ApproveApplicationRequestDTOFixture
import sg.gov.tech.molbagencyportalbackend.service.LicenceService
import sg.gov.tech.testing.MolbUnitTesting
import sg.gov.tech.testing.verifyNever
import sg.gov.tech.testing.verifyOnce
import javax.validation.ConstraintValidatorContext

@MolbUnitTesting
class ApproveRequestValidatorTest {
    @MockK
    private lateinit var licenceService: LicenceService

    @InjectMockKs
    private lateinit var validator: ApproveRequestValidator

    private val context = mockk<ConstraintValidatorContext>()
    private val contextBuilder = mockk<ConstraintValidatorContext.ConstraintViolationBuilder>()

    @BeforeEach
    fun setUp() {
        every { context.disableDefaultConstraintViolation() } just runs
        every { context.buildConstraintViolationWithTemplate(any()) } returns contextBuilder
        every { contextBuilder.addConstraintViolation() } returns context
        every { contextBuilder.addPropertyNode(any()).addConstraintViolation() } returns context
        every { licenceService.existsByLicenceNumber(any()) } returns false
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
        clearMocks(context)
        clearMocks(contextBuilder)
    }

    @Test
    fun `should pass`() {
        Assertions.assertThat(
            validator.isValid(
                ApproveApplicationRequestDTOFixture.ApproveApplicationRequestNoFiles,
                context
            )
        ).isTrue
        verifyNever { contextBuilder.addConstraintViolation() }
        Assertions.assertThat(validator.errorMessageMap.size).isEqualTo(0)
    }

    @Test
    fun `should fail if licence number already exists`() {
        every { licenceService.existsByLicenceNumber(any()) } returns true
        Assertions.assertThat(
            validator.isValid(
                ApproveApplicationRequestDTOFixture.ApproveApplicationRequestNoFiles,
                context
            )
        ).isFalse
        verifyOnce { contextBuilder.addPropertyNode(any()).addConstraintViolation() }
        Assertions.assertThat(validator.errorMessageMap.size).isEqualTo(1)
        org.junit.jupiter.api.Assertions.assertTrue(
            validator.errorMessageMap
                .containsValue(ExceptionControllerAdvice.LICENCE_EXIST_MESSAGE)
        )
    }

    @Test
    fun `should fail for invalid issue date`() {
        Assertions.assertThat(
            validator.isValid(
                ApproveApplicationRequestDTOFixture.ApproveApplicationRequestNoFiles.copy(
                    issueDate = "39/13/2345"
                ),
                context
            )
        ).isFalse
        verifyOnce { contextBuilder.addPropertyNode(any()).addConstraintViolation() }
        Assertions.assertThat(validator.errorMessageMap.size).isEqualTo(1)
        org.junit.jupiter.api.Assertions.assertTrue(
            validator.errorMessageMap
                .containsValue(ExceptionControllerAdvice.INVALID_VALUE_MESSAGE)
        )
    }

    @Test
    fun `should fail for invalid start date`() {
        Assertions.assertThat(
            validator.isValid(
                ApproveApplicationRequestDTOFixture.ApproveApplicationRequestNoFiles.copy(
                    startDate = ""
                ),
                context
            )
        ).isFalse
        verifyOnce { contextBuilder.addPropertyNode(any()).addConstraintViolation() }
        Assertions.assertThat(validator.errorMessageMap.size).isEqualTo(1)
        org.junit.jupiter.api.Assertions.assertTrue(
            validator.errorMessageMap
                .containsValue(ExceptionControllerAdvice.INVALID_VALUE_MESSAGE)
        )
    }

    @Test
    fun `should fail for invalid expiry date`() {
        Assertions.assertThat(
            validator.isValid(
                ApproveApplicationRequestDTOFixture.ApproveApplicationRequestNoFiles.copy(
                    expiryDate = "31/02/1988"
                ),
                context
            )
        ).isFalse
        verifyOnce { contextBuilder.addPropertyNode(any()).addConstraintViolation() }
        Assertions.assertThat(validator.errorMessageMap.size).isEqualTo(1)
        org.junit.jupiter.api.Assertions.assertTrue(
            validator.errorMessageMap.containsValue(ExceptionControllerAdvice.INVALID_VALUE_MESSAGE)
        )
    }

    @Test
    fun `should fail for start date earlier than issue date`() {
        Assertions.assertThat(
            validator.isValid(
                ApproveApplicationRequestDTOFixture.ApproveApplicationRequestNoFiles.copy(
                    issueDate = "01/02/2022",
                    startDate = "01/01/2022"
                ),
                context
            )
        ).isFalse
        verifyOnce { contextBuilder.addPropertyNode(any()).addConstraintViolation() }
        Assertions.assertThat(validator.errorMessageMap.size).isEqualTo(1)
        org.junit.jupiter.api.Assertions.assertTrue(
            validator.errorMessageMap
                .containsValue(ExceptionControllerAdvice.START_DATE_NO_EARLIER_MESSAGE)
        )
    }

    @Test
    fun `should fail for expiry date earlier than start date or issue date`() {
        Assertions.assertThat(
            validator.isValid(
                ApproveApplicationRequestDTOFixture.ApproveApplicationRequestNoFiles.copy(
                    expiryDate = "01/01/2022",
                    startDate = "01/02/2022",
                    issueDate = "01/02/2022"
                ),
                context
            )
        ).isFalse
        verifyOnce { contextBuilder.addPropertyNode(any()).addConstraintViolation() }
        Assertions.assertThat(validator.errorMessageMap.size).isEqualTo(1)
        org.junit.jupiter.api.Assertions.assertTrue(
            validator.errorMessageMap
                .containsValue(ExceptionControllerAdvice.EXPIRY_DATE_NO_EARLIER_MESSAGE)
        )
    }
}
