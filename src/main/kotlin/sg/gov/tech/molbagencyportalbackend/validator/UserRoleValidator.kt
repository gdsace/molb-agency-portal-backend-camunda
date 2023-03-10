package sg.gov.tech.molbagencyportalbackend.validator

import sg.gov.tech.molbagencyportalbackend.exception.ExceptionControllerAdvice
import sg.gov.tech.molbagencyportalbackend.service.RoleService
import javax.validation.Constraint
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext
import javax.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [UserRoleValidator::class])
annotation class ValidUserRole(
    val message: String = ExceptionControllerAdvice.INVALID_VALUE_MESSAGE,
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

class UserRoleValidator(
    private val roleService: RoleService
) : ConstraintValidator<ValidUserRole, String> {

    override fun isValid(role: String, context: ConstraintValidatorContext): Boolean {
        context.disableDefaultConstraintViolation()

        val error: String = when {
            !roleService.existByCode(role) -> ExceptionControllerAdvice.INVALID_VALUE_MESSAGE
            else -> ""
        }

        return if (error.isNotBlank()) {
            context.addConstraintViolation(error)
            return false
        } else
            true
    }
}
