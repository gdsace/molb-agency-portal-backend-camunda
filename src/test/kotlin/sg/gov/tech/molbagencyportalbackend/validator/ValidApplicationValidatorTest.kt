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
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import sg.gov.tech.molbagencyportalbackend.exception.ExceptionControllerAdvice
import sg.gov.tech.molbagencyportalbackend.fixture.CreateApplicationRequestDTOFixture.createApplicationRequest
import sg.gov.tech.molbagencyportalbackend.fixture.CreateApplicationRequestDTOFixture.createApplicationRequest_BlankFilerOnbehalf
import sg.gov.tech.molbagencyportalbackend.fixture.CreateApplicationRequestDTOFixture.createApplicationRequest_NullAddress
import sg.gov.tech.molbagencyportalbackend.service.AgencyService
import sg.gov.tech.molbagencyportalbackend.service.ApplicationService
import sg.gov.tech.molbagencyportalbackend.service.LicenceService
import sg.gov.tech.testing.MolbUnitTesting
import sg.gov.tech.testing.verifyNever
import sg.gov.tech.testing.verifyOnce
import javax.validation.ConstraintValidatorContext

@MolbUnitTesting
class ValidApplicationValidatorTest {
    @MockK
    private lateinit var agencyService: AgencyService

    @MockK
    private lateinit var applicationService: ApplicationService

    @MockK
    private lateinit var licenceService: LicenceService

    @InjectMockKs
    private lateinit var validator: ValidApplicationValidator

    private val context = mockk<ConstraintValidatorContext>()
    private val contextBuilder = mockk<ConstraintValidatorContext.ConstraintViolationBuilder>()

    @BeforeEach
    fun setUp() {
        every { context.disableDefaultConstraintViolation() } just runs
        every { context.buildConstraintViolationWithTemplate(any()) } returns contextBuilder
        every { contextBuilder.addConstraintViolation() } returns context
        every { contextBuilder.addPropertyNode(any()).addConstraintViolation() } returns context

        every { applicationService.existsByApplicationNumber(any()) } returns false
        every { agencyService.existByCode(any()) } returns true
        every { licenceService.existsByLicenceNumber(any()) } returns true
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
        clearMocks(context)
        clearMocks(contextBuilder)
    }

    @Test
    fun `should pass`() {
        assertThat(validator.isValid(createApplicationRequest, context)).isTrue
        verifyNever { contextBuilder.addConstraintViolation() }
        assertThat(validator.errorMessageMap.size).isEqualTo(0)
    }

    @Test
    fun `should fail if agency is not supported`() {
        every { agencyService.existByCode(any()) } returns false

        assertThat(validator.isValid(createApplicationRequest, context)).isFalse
        verifyOnce { contextBuilder.addPropertyNode(any()).addConstraintViolation() }
        assertThat(validator.errorMessageMap.size).isEqualTo(1)
        assertTrue(validator.errorMessageMap.containsValue(ExceptionControllerAdvice.AGENCY_CODE_NOT_SUPPORTED_MESSAGE))
    }

    @Test
    fun `should fail if application number already exists`() {
        every { applicationService.existsByApplicationNumber(any()) } returns true

        assertThat(validator.isValid(createApplicationRequest, context)).isFalse
        verifyOnce { contextBuilder.addPropertyNode(any()).addConstraintViolation() }
        assertThat(validator.errorMessageMap.size).isEqualTo(1)
        assertTrue(validator.errorMessageMap.containsValue(ExceptionControllerAdvice.APPLICATION_NO_EXIST_MESSAGE))
    }

    @Test
    fun `should fail if filer missing given applying on behalf`() {
        assertThat(validator.isValid(createApplicationRequest_BlankFilerOnbehalf, context)).isFalse
        verifyOnce { contextBuilder.addPropertyNode(any()).addConstraintViolation() }
        assertThat(validator.errorMessageMap.size).isEqualTo(1)
        assertTrue(validator.errorMessageMap.containsValue(ExceptionControllerAdvice.FILER_INFO_MISSING_MESSAGE))
    }

    @Test
    fun `should fail if Singpass applicant address is null`() {
        assertThat(validator.isValid(createApplicationRequest_NullAddress, context)).isFalse
        verifyOnce { contextBuilder.addPropertyNode(any()).addConstraintViolation() }
        assertThat(validator.errorMessageMap.size).isEqualTo(1)
        assertTrue(validator.errorMessageMap.containsValue(ExceptionControllerAdvice.SINGPASS_ADDRESS_EMPTY_MESSAGE))
    }
}
