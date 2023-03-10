package sg.gov.tech.molbagencyportalbackend.validator

import sg.gov.tech.molbagencyportalbackend.auth.AuthenticationFacade
import sg.gov.tech.molbagencyportalbackend.exception.ExceptionControllerAdvice
import sg.gov.tech.molbagencyportalbackend.service.UserService
import sg.gov.tech.molbagencyportalbackend.util.CommonUtil
import javax.validation.Constraint
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext
import javax.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [UserEmailValidator::class])
annotation class ValidUserEmail(
    val message: String = ExceptionControllerAdvice.INVALID_VALUE_MESSAGE,
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

class UserEmailValidator(
    private val userService: UserService,
    private val authenticationFacade: AuthenticationFacade

) : ConstraintValidator<ValidUserEmail, String> {
    companion object {
        private const val GOV_SG_DOMAIN = "gov.sg"
    }

    override fun isValid(email: String, context: ConstraintValidatorContext): Boolean {
        context.disableDefaultConstraintViolation()

        val error: String = when {
            email.isNullOrEmpty() -> ExceptionControllerAdvice.INVALID_VALUE_MESSAGE
            userService.existsByEmailAndIsDeletedFalse(email) -> ExceptionControllerAdvice.EMAIL_EXIST_MESSAGE
            !email.endsWith(GOV_SG_DOMAIN, true) -> ExceptionControllerAdvice.INVALID_VALUE_MESSAGE
            CommonUtil.getEmailDomain(authenticationFacade.getPrincipalName()).lowercase() !=
                CommonUtil.getEmailDomain(email).lowercase() -> ExceptionControllerAdvice.ADD_ONLY_SAME_AGENCY_MESSAGE
            else -> ""
        }

        return if (error.isNotBlank()) {
            context.addConstraintViolation(error)
            return false
        } else {
            true
        }
    }
}
