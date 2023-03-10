package sg.gov.tech.molbagencyportalbackend

import com.github.tomakehurst.wiremock.common.Json
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder

@SpringBootTest
class MolbagencyportalbackendApplicationTests {

    @Test
    fun contextLoads() {
    }
}

fun MockHttpServletRequestBuilder.jsonContent(obj: Any) =
    this.content(Json.toByteArray(obj))
        .contentType(MediaType.APPLICATION_JSON)
