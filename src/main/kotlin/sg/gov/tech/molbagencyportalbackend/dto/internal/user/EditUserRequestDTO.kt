package sg.gov.tech.molbagencyportalbackend.dto.internal.user

import sg.gov.tech.molbagencyportalbackend.annotation.ExcludeFromGeneratedCoverageTest
import sg.gov.tech.molbagencyportalbackend.exception.ExceptionControllerAdvice
import sg.gov.tech.molbagencyportalbackend.validator.ValidUserRole
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Pattern

@ExcludeFromGeneratedCoverageTest
data class EditUserRequestDTO(
    @field:NotBlank(message = ExceptionControllerAdvice.INVALID_VALUE_MESSAGE)
    val name: String,
    @field:ValidUserRole
    val role: String,
    @field:Pattern(regexp = "ACTIVE|INACTIVE", message = ExceptionControllerAdvice.INVALID_VALUE_MESSAGE)
    val accountStatus: String
)
