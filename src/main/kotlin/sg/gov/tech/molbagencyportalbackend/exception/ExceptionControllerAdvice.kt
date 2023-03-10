package sg.gov.tech.molbagencyportalbackend.exception

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.access.AccessDeniedException
import org.springframework.validation.BindException
import org.springframework.validation.FieldError
import org.springframework.validation.ObjectError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.ServletWebRequest
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import org.springframework.web.util.ContentCachingRequestWrapper
import sg.gov.tech.molbagencyportalbackend.annotation.ExcludeFromGeneratedCoverageTest
import sg.gov.tech.molbagencyportalbackend.dto.ErrorResponseDTO
import sg.gov.tech.molbagencyportalbackend.dto.SubErrorDTO
import sg.gov.tech.molbagencyportalbackend.dto.l1t.L1TErrorDTO
import sg.gov.tech.molbagencyportalbackend.dto.l1t.L1TResponseDTOTransfer
import sg.gov.tech.molbagencyportalbackend.dto.l1t.L1TSubErrorDTO
import sg.gov.tech.molbagencyportalbackend.util.FormatterUtil

@RestControllerAdvice
@ExcludeFromGeneratedCoverageTest
class ExceptionControllerAdvice(
    private val l1TResponseDTOTransfer: L1TResponseDTOTransfer,
    private val objectMapper: ObjectMapper
) : ResponseEntityExceptionHandler() {

    companion object {
        const val ACC_NO_PERMISSION_MESSAGE = "Your account does not have permission to do this"
        const val ADD_ONLY_SAME_AGENCY_MESSAGE = "User can only be added under the same agency as supervisor"
        const val AGENCY_CODE_NOT_SUPPORTED_MESSAGE = "agencyCode not supported"
        const val APPLICATION_ALREADY_ASSIGNED_MESSAGE = "Application already assigned to a different officer"
        const val APPLICATION_NO_EXIST_MESSAGE = "Application Number already exists"
        const val APPLICATION_UPDATED_ELSEWHERE_MESSAGE = "Application has been updated elsewhere"
        const val AUTH_ERROR_MESSAGE = "Authentication error"
        const val CONFIG_ERROR_MESSAGE = "Internal application error"
        const val EMAIL_EXIST_MESSAGE = "Email already exists"
        const val EXPIRY_DATE_NO_EARLIER_MESSAGE = "Expiry Date cannot be earlier than Issue Date or Start Date"
        const val FEIGN_ERROR_MESSAGE = "Error calling external API"
        const val FILER_INFO_MISSING_MESSAGE = "filer info missing"
        const val FILE_DOWNLOAD_ERROR_MESSAGE = "Unable to download file"
        const val INVALID_PAYLOAD_MESSAGE = "Request payload is invalid or malformed"
        const val INVALID_VALUE_MESSAGE = "Invalid value"
        const val LICENCE_EXIST_MESSAGE = "Licence No. already exists."
        const val LICENCE_ID_NOT_SUPPORTED_MESSAGE = "licenceID not supported"
        const val NOT_FOUND_MESSAGE = "Record not found"
        const val PAYLOAD_TOO_LARGE_MESSAGE = "Payload too large"
        const val RFA_EXIST_MESSAGE = "RFA record already exists"
        const val SINGPASS_ADDRESS_EMPTY_MESSAGE = "Singpass applicant address cannot be empty"
        const val START_DATE_NO_EARLIER_MESSAGE = "Start Date cannot be earlier than Issue Date"
        const val UNHANDLED_APPLICATION_ERROR_MESSAGE = "Unhandled application error"
        const val UNSUPPORTED_MEDIA_TYPE_MESSAGE = "Unsupported media type"
        const val VALIDATION_ERROR_MESSAGE = "Validation error"
        const val NEW_USER_NO_PERMISSION = "New User does not have necessary permissions to process the application"
        const val NEW_USER_INACTIVE_MESSAGE = "New User is inactive"
        const val CANT_REASSIGN_APPLICATION = "Application cannot be reassigned at the moment"
        const val CANT_REASSIGN_TO_DIFF_AGENCY = "Cannot reassign application to another User from different Agency"
        const val ACCESS_ERROR_STATUS = "Access Error"
        const val BAD_REQUEST_STATUS = "Bad Request"
        const val CONFIG_ERROR_STATUS = "Config Error"
        const val FILE_DOWNLOAD_ERROR_STATUS = "File download error"
        const val INTERNAL_SERVER_ERROR_STATUS = "Internal Server Error"
        const val NOT_FOUND_STATUS = "Not Found Error"
        const val PAYLOAD_ERROR_STATUS = "Payload Error"
        const val UNSUPPORTED_MEDIA_TYPE_STATUS = "Unsupported media type"
        const val VALIDATION_ERROR_STATUS = "Validation Error"
        const val APPLICANT_ALREADY_RESPONDED = "Applicant has already responded to the RFA"
    }

    fun isL1tRequest(path: String) = path.contains("/resource")

    // Custom response for 400 - HttpMessageNotReadableException
    override fun handleHttpMessageNotReadable(
        ex: HttpMessageNotReadableException,
        headers: HttpHeaders,
        httpStatus: HttpStatus,
        request: WebRequest
    ): ResponseEntity<Any> {
        return handleExceptionResponse(
            ex,
            request,
            httpStatus,
            BAD_REQUEST_STATUS,
            INVALID_PAYLOAD_MESSAGE
        )
    }

    // Custom response for 400 - BindException
    // From @Valid @fields errors in request params
    override fun handleBindException(
        ex: BindException,
        headers: HttpHeaders,
        httpStatus: HttpStatus,
        request: WebRequest
    ): ResponseEntity<Any> {
        return handleExceptionResponse(
            ex,
            request,
            if (isL1tRequest(request.getDescription(false))) HttpStatus.OK else httpStatus,
            BAD_REQUEST_STATUS,
            VALIDATION_ERROR_MESSAGE,
            formatSubErrors(ex.bindingResult.allErrors)
        )
    }

    // Custom response for 400 - MethodArgumentNotValidException
    // From @Valid @fields errors in request body
    override fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        headers: HttpHeaders,
        httpStatus: HttpStatus,
        request: WebRequest
    ): ResponseEntity<Any> {
        return handleExceptionResponse(
            ex,
            request,
            if (isL1tRequest(request.getDescription(false))) HttpStatus.OK else httpStatus,
            BAD_REQUEST_STATUS,
            VALIDATION_ERROR_MESSAGE,
            formatSubErrors(ex.bindingResult.allErrors)
        )
    }

    // The only exception that does not have generic message
    @ExceptionHandler(ValidationException::class)
    fun handleValidationException(
        ex: ValidationException,
        request: WebRequest
    ): ResponseEntity<Any> {
        return handleExceptionResponse(
            ex,
            request,
            HttpStatus.CONFLICT,
            VALIDATION_ERROR_STATUS,
            ex.responseMessage
        )
    }

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFoundException(ex: NotFoundException, request: WebRequest): ResponseEntity<Any> {
        return handleExceptionResponse(
            ex,
            request,
            HttpStatus.NOT_FOUND,
            NOT_FOUND_STATUS,
            NOT_FOUND_MESSAGE
        )
    }

    @ExceptionHandler(InternalConfigException::class)
    fun handleInternalConfigException(
        ex: InternalConfigException,
        request: WebRequest
    ): ResponseEntity<Any> {
        return handleExceptionResponse(
            ex,
            request,
            HttpStatus.INTERNAL_SERVER_ERROR,
            CONFIG_ERROR_STATUS,
            CONFIG_ERROR_MESSAGE
        )
    }

    @ExceptionHandler(FileDownloadException::class)
    fun handleFileDownloadException(
        ex: FileDownloadException,
        request: WebRequest
    ): ResponseEntity<Any> {
        return handleExceptionResponse(
            ex,
            request,
            HttpStatus.INTERNAL_SERVER_ERROR,
            FILE_DOWNLOAD_ERROR_STATUS,
            FILE_DOWNLOAD_ERROR_MESSAGE
        )
    }

    @ExceptionHandler(NotAuthorisedException::class)
    fun handleNotAuthorizedException(
        ex: NotAuthorisedException,
        request: WebRequest
    ): ResponseEntity<Any> {
        return handleExceptionResponse(
            ex,
            request,
            HttpStatus.FORBIDDEN,
            ACCESS_ERROR_STATUS,
            ACC_NO_PERMISSION_MESSAGE
        )
    }

    @ExceptionHandler(AuthException::class)
    fun handleAuthException(ex: AuthException, request: WebRequest): ResponseEntity<Any> {
        return handleExceptionResponse(
            ex,
            request,
            HttpStatus.UNAUTHORIZED,
            ACCESS_ERROR_STATUS,
            AUTH_ERROR_MESSAGE
        )
    }

    // Thrown by @PreAuthorized
    @ExceptionHandler(AccessDeniedException::class)
    fun handleAuthException(ex: AccessDeniedException, request: WebRequest): ResponseEntity<Any> {
        return handleExceptionResponse(
            ex,
            request,
            HttpStatus.FORBIDDEN,
            ACCESS_ERROR_STATUS,
            ACC_NO_PERMISSION_MESSAGE
        )
    }

    @ExceptionHandler(PayloadTooLargeException::class)
    fun handlePayloadTooLargeException(
        ex: PayloadTooLargeException,
        request: WebRequest
    ): ResponseEntity<Any> {
        return handleExceptionResponse(
            ex,
            request,
            HttpStatus.PAYLOAD_TOO_LARGE,
            PAYLOAD_ERROR_STATUS,
            PAYLOAD_TOO_LARGE_MESSAGE
        )
    }

    @ExceptionHandler(UnsupportedMediaTypeException::class)
    fun handleUnsupportedMediaTypeException(
        ex: UnsupportedMediaTypeException,
        request: WebRequest
    ): ResponseEntity<Any> {
        return handleExceptionResponse(
            ex,
            request,
            HttpStatus.UNSUPPORTED_MEDIA_TYPE,
            UNSUPPORTED_MEDIA_TYPE_STATUS,
            UNSUPPORTED_MEDIA_TYPE_MESSAGE
        )
    }

    @ExceptionHandler(FeignBadRequestException::class)
    fun handleFeignBadRequestException(
        ex: FeignBadRequestException,
        request: WebRequest
    ): ResponseEntity<Any> {
        return handleExceptionResponse(
            ex,
            request,
            HttpStatus.INTERNAL_SERVER_ERROR,
            BAD_REQUEST_STATUS,
            FEIGN_ERROR_MESSAGE
        )
    }

    @ExceptionHandler(FeignNotAuthorisedException::class)
    fun handleFeignNotAuthorisedException(
        ex: FeignNotAuthorisedException,
        request: WebRequest
    ): ResponseEntity<Any> {
        return handleExceptionResponse(
            ex,
            request,
            HttpStatus.INTERNAL_SERVER_ERROR,
            ACCESS_ERROR_STATUS,
            FEIGN_ERROR_MESSAGE
        )
    }

    // Catch all handler
    @ExceptionHandler(Exception::class)
    fun handleCatchAllException(ex: Exception, request: WebRequest): ResponseEntity<Any> {
        return handleExceptionResponse(
            ex,
            request,
            HttpStatus.INTERNAL_SERVER_ERROR,
            INTERNAL_SERVER_ERROR_STATUS,
            UNHANDLED_APPLICATION_ERROR_MESSAGE
        )
    }

    private fun formatSubErrors(allErrors: MutableList<ObjectError>): List<SubErrorDTO> {
        val subErrors = mutableListOf<SubErrorDTO>()
        allErrors.forEach { error ->
            subErrors.add(
                SubErrorDTO(
                    (if (error is FieldError) error.field else ""),
                    (if (error is FieldError) error.rejectedValue.toString() else ""),
                    error.defaultMessage.toString()
                )
            )
        }
        return subErrors
    }

    private fun handleExceptionResponse(
        ex: Exception,
        request: WebRequest,
        httpStatus: HttpStatus,
        status: String,
        message: String,
        subErrors: List<SubErrorDTO> = emptyList()
    ): ResponseEntity<Any> {
        logger.error(ex.stackTraceToString())
        if (isL1tRequest(request.getDescription(false))) {
            val nativeRequest =
                (request as ServletWebRequest).nativeRequest as ContentCachingRequestWrapper
            val requestEntityAsString = String(nativeRequest.contentAsByteArray)
            val jsonNode: JsonNode = objectMapper.readTree(requestEntityAsString)
            val operationType: String =
                jsonNode.findValue("operation")?.asText() ?: "defaultOperationType"
            val l1tSubErrors = subErrors.map {
                L1TSubErrorDTO(
                    pointer = it.pointer,
                    rejectedValue = it.rejectedValue,
                    message = it.message
                )
            }
            val response = l1TResponseDTOTransfer.createErrorResponseDTO(
                L1TErrorDTO(
                    status,
                    message,
                    l1tSubErrors,
                    FormatterUtil.formatOperationTypeForPath(operationType)
                )
            )
            return ResponseEntity(response, httpStatus)
        }
        return ResponseEntity(ErrorResponseDTO(message, subErrors), httpStatus)
    }
}
