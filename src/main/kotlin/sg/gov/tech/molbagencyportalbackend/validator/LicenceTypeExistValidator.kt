package sg.gov.tech.molbagencyportalbackend.validator

import sg.gov.tech.molbagencyportalbackend.exception.ExceptionControllerAdvice
import sg.gov.tech.molbagencyportalbackend.service.LicenceTypeService
import javax.validation.Constraint
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext
import javax.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [LicenceTypeExistsValidator::class])
annotation class LicenceTypeExists(
    val message: String = ExceptionControllerAdvice.INVALID_VALUE_MESSAGE,
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

class LicenceTypeExistsValidator(
    private val licenceTypeService: LicenceTypeService
) : ConstraintValidator<LicenceTypeExists, String?> {

    override fun isValid(id: String?, context: ConstraintValidatorContext): Boolean {
        context.disableDefaultConstraintViolation()

        if (id == null || !licenceTypeService.existByLicenceId(id)) {
            context.addConstraintViolation(ExceptionControllerAdvice.LICENCE_ID_NOT_SUPPORTED_MESSAGE)
            return false
        }

        return true
    }
}
