package sg.gov.tech.molbagencyportalbackend.dto.internal.user

import org.hibernate.validator.constraints.Range
import sg.gov.tech.molbagencyportalbackend.annotation.ExcludeFromGeneratedCoverageTest
import sg.gov.tech.molbagencyportalbackend.exception.ExceptionControllerAdvice
import javax.validation.constraints.Pattern

@ExcludeFromGeneratedCoverageTest
data class UserListRequestParams(
    @field:Range(min = 0, message = ExceptionControllerAdvice.INVALID_VALUE_MESSAGE)
    val page: Int = 0,
    @field:Pattern(regexp = "name|email|role|status", message = ExceptionControllerAdvice.INVALID_VALUE_MESSAGE)
    val sortField: String,
    @field:Pattern(regexp = "asc|desc", message = ExceptionControllerAdvice.INVALID_VALUE_MESSAGE)
    val sortOrder: String,
    @field:Range(min = 1, message = ExceptionControllerAdvice.INVALID_VALUE_MESSAGE)
    val limit: Int = 10
)

@ExcludeFromGeneratedCoverageTest
data class UserListResponseDTO(val data: List<UserDTO>, val totalCount: Long)
