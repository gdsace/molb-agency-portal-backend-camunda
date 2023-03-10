package sg.gov.tech.molbagencyportalbackend.exception

import feign.Response
import feign.codec.ErrorDecoder
import org.springframework.http.HttpStatus

class FeignClientExceptionHandler : ErrorDecoder {
    override fun decode(methodKey: String, response: Response): Exception {
        return when (HttpStatus.valueOf(response.status())) {
            HttpStatus.UNSUPPORTED_MEDIA_TYPE ->
                UnsupportedMediaTypeException("${response.reason()}")
            HttpStatus.PAYLOAD_TOO_LARGE ->
                PayloadTooLargeException("${response.reason()}")
            HttpStatus.UNAUTHORIZED ->
                FeignNotAuthorisedException("${response.reason()}")
            HttpStatus.BAD_REQUEST ->
                FeignBadRequestException("${response.request().url()} ${response.reason()}")
            HttpStatus.NOT_FOUND ->
                NotFoundException(response.reason())
            else ->
                Exception("${response.reason()}")
        }
    }
}
