package sg.gov.tech.molbagencyportalbackend.validator

import sg.gov.tech.molbagencyportalbackend.dto.internal.ApproveApplicationRequestDTO
import sg.gov.tech.molbagencyportalbackend.exception.ExceptionControllerAdvice
import sg.gov.tech.molbagencyportalbackend.service.LicenceService
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle
import javax.validation.Constraint
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext
import javax.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [ApproveRequestValidator::class])
annotation class ValidApproveRequest(
    val message: String = ExceptionControllerAdvice.INVALID_VALUE_MESSAGE,
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

class ApproveRequestValidator(
    private val licenceService: LicenceService
) : ConstraintValidator<ValidApproveRequest, ApproveApplicationRequestDTO> {
    companion object {
        const val LICENCE_NUMBER = "licenceNumber"
        const val ISSUE_DATE = "issueDate"
        const val START_DATE = "startDate"
        const val EXPIRY_DATE = "expiryDate"
    }

    val errorMessageMap = mutableMapOf<String, String>()
    override fun isValid(
        dto: ApproveApplicationRequestDTO,
        context: ConstraintValidatorContext
    ): Boolean {
        context.disableDefaultConstraintViolation()
        errorMessageMap.clear()
        errorMessageMap.putAll(validateRequest(dto))
        return !shouldFailImmediately(context)
    }

    private fun shouldFailImmediately(context: ConstraintValidatorContext): Boolean =
        if (errorMessageMap.isNotEmpty()) {
            errorMessageMap.forEach { context.addConstraintViolationWithPropertyNode(it) }
            true
        } else false

    private fun validateRequest(dto: ApproveApplicationRequestDTO): MutableMap<String, String> {
        val errors = mutableMapOf<String, String>()
        val issueLocalDate: LocalDate? = getLocalDateFormat(dto.issueDate)
        val startLocalDate: LocalDate? = getLocalDateFormat(dto.startDate)
        val expiryLocalDate: LocalDate? = getLocalDateFormat(dto.expiryDate)
        if (issueLocalDate == null) errors[ISSUE_DATE] = ExceptionControllerAdvice.INVALID_VALUE_MESSAGE
        if (startLocalDate == null) errors[START_DATE] = ExceptionControllerAdvice.INVALID_VALUE_MESSAGE
        if (dto.expiryDate != null && expiryLocalDate == null) errors[EXPIRY_DATE] =
            ExceptionControllerAdvice.INVALID_VALUE_MESSAGE
        if (expiryLocalDate != null && startLocalDate != null && issueLocalDate != null) {
            if (expiryLocalDate.isBefore(startLocalDate) || expiryLocalDate.isBefore(issueLocalDate))
                errors[EXPIRY_DATE] = ExceptionControllerAdvice.EXPIRY_DATE_NO_EARLIER_MESSAGE
        }
        if (startLocalDate != null && issueLocalDate != null) {
            if (startLocalDate.isBefore(issueLocalDate))
                errors[START_DATE] = ExceptionControllerAdvice.START_DATE_NO_EARLIER_MESSAGE
        }
        if (licenceService.existsByLicenceNumber(dto.licenceNumber))
            errors[LICENCE_NUMBER] = ExceptionControllerAdvice.LICENCE_EXIST_MESSAGE

        return errors
    }

    private fun getLocalDateFormat(date: String?): LocalDate? {
        return if (!date.isNullOrEmpty()) {
            try {
                val formatter = DateTimeFormatter.ofPattern("dd/MM/uuuu")
                    .withResolverStyle(ResolverStyle.STRICT)
                LocalDate.parse(date, formatter)
            } catch (e: DateTimeParseException) {
                null
            }
        } else null
    }
}
