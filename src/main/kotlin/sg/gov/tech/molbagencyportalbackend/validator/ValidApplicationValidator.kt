package sg.gov.tech.molbagencyportalbackend.validator

import sg.gov.tech.molbagencyportalbackend.dto.l1t.CreateApplicationRequestDTO
import sg.gov.tech.molbagencyportalbackend.exception.ExceptionControllerAdvice
import sg.gov.tech.molbagencyportalbackend.model.ApplicationStatus
import sg.gov.tech.molbagencyportalbackend.model.ApplyAs
import sg.gov.tech.molbagencyportalbackend.service.AgencyService
import sg.gov.tech.molbagencyportalbackend.service.ApplicationService
import javax.validation.Constraint
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext
import javax.validation.ConstraintViolation
import javax.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [ValidApplicationValidator::class])
annotation class ValidApplication(
    val message: String = ExceptionControllerAdvice.INVALID_VALUE_MESSAGE,
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

class ValidApplicationValidator(
    private val agencyService: AgencyService,
    private val applicationService: ApplicationService
) : ConstraintValidator<ValidApplication, CreateApplicationRequestDTO> {
    companion object {
        const val AGENCY_CODE_FIELD_PATH = "version.agencyCode"
        const val APPLICATION_NUMBER_FIELD_PATH = "application.general.applicationNumber"
        const val FILER_NAME_FIELD_PATH = "application.filer.name"
        const val APPLICANT_ADDRESS_FIELD_PATH = "application.applicant.address"
    }

    val errorMessageMap = mutableMapOf<String, String>()

    override fun isValid(
        dto: CreateApplicationRequestDTO,
        context: ConstraintValidatorContext
    ): Boolean {
        context.disableDefaultConstraintViolation()

        errorMessageMap.clear()
        errorMessageMap.putAll(validateApplication(dto))

        return !shouldFailImmediately(context)
    }

    private fun validateApplication(dto: CreateApplicationRequestDTO): MutableMap<String, String> {
        val errors = mutableMapOf<String, String>()

        if (applicationService.existsByApplicationNumber(dto.application.general.applicationNumber) &&
            dto.operation == "createApplication"
        )
            errors[APPLICATION_NUMBER_FIELD_PATH] = ExceptionControllerAdvice.APPLICATION_NO_EXIST_MESSAGE

        if (!agencyService.existByCode(dto.version.agencyCode))
            errors[AGENCY_CODE_FIELD_PATH] = ExceptionControllerAdvice.AGENCY_CODE_NOT_SUPPORTED_MESSAGE

        if (dto.application.profile.applyAs == ApplyAs.ON_BEHALF.value &&
            dto.application.filer.name.isNullOrBlank()
        )
            errors[FILER_NAME_FIELD_PATH] = ExceptionControllerAdvice.FILER_INFO_MISSING_MESSAGE

        if (dto.application.company.companyName.isNullOrBlank() &&
            dto.application.applicant.address == null
        )
            errors[APPLICANT_ADDRESS_FIELD_PATH] = ExceptionControllerAdvice.SINGPASS_ADDRESS_EMPTY_MESSAGE

        // checks for clarification end point
        setClarificationValidationErrors(dto, errors)
        return errors
    }

    private fun setClarificationValidationErrors(
        dto: CreateApplicationRequestDTO,
        errors: MutableMap<String, String>
    ) {
        if (dto.operation == "clarification") {
            if (applicationService.existsByApplicationNumber(dto.application.general.applicationNumber)) {
                val application = applicationService.getApplication(dto.application.general.applicationNumber)
                if (application.status !in ApplicationStatus.getRFASubmittedStatuses()) {
                    errors[APPLICATION_NUMBER_FIELD_PATH] =
                        ExceptionControllerAdvice.APPLICATION_UPDATED_ELSEWHERE_MESSAGE
                }
            } else
                errors[APPLICATION_NUMBER_FIELD_PATH] = ExceptionControllerAdvice.NOT_FOUND_MESSAGE
        }
    }

    private fun shouldFailImmediately(context: ConstraintValidatorContext): Boolean =
        if (errorMessageMap.isNotEmpty()) {
            errorMessageMap.forEach { context.addConstraintViolationWithPropertyNode(it) }
            true
        } else false
}

internal fun ConstraintValidatorContext.overrideErrorMessage(message: String) {
    addConstraintViolation(message).disableDefaultConstraintViolation()
}

internal fun ConstraintValidatorContext.addConstraintViolation(message: String) =
    buildConstraintViolationWithTemplate(message).addConstraintViolation()

internal fun ConstraintValidatorContext.addConstraintViolationWithPropertyNode(message: Map.Entry<String, String>) =
    buildConstraintViolationWithTemplate(message.value).addPropertyNode(message.key)
        .addConstraintViolation()

internal fun ConstraintValidatorContext.addConstraintViolation(constraint: ConstraintViolation<*>) =
    addConstraintViolation("${constraint.propertyPath}: ${constraint.message}")
