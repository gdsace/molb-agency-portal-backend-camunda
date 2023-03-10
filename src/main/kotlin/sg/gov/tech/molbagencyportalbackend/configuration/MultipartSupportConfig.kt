package sg.gov.tech.molbagencyportalbackend.configuration

import feign.codec.ErrorDecoder
import org.springframework.cloud.openfeign.support.JsonFormWriter
import org.springframework.context.annotation.Bean
import sg.gov.tech.molbagencyportalbackend.exception.FeignClientExceptionHandler

class MultipartSupportConfig {
    @Bean
    fun jsonFormWriter(): JsonFormWriter {
        return JsonFormWriter()
    }

    @Bean
    fun errorDecoder(): ErrorDecoder? {
        return FeignClientExceptionHandler()
    }
}
