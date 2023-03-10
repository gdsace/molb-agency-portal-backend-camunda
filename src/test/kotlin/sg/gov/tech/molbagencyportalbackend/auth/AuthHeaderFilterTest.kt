package sg.gov.tech.molbagencyportalbackend.auth

import com.nimbusds.jose.JWSAlgorithm
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Profile
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import sg.gov.tech.security.SecretUtils

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
internal class AuthHeaderFilterTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `should_allow_access_when_endpoint_is_unsecured`() {
        mockMvc
            .perform(
                get("/api/test")
                    .header("authorization", "authorization-value")
            )
            .andExpect(MockMvcResultMatchers.status().isOk)
    }

    @Test
    fun `should_allow_access_when_secured_endpoint_is_invoked_with_valid_authorization_header`() {

        val privateKey =
            "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCyB4+K1taBRo1sLnrQPeGnf9/FtjjTnRUkDqP6wPkMxt3WzGLIZdd4rQLRUsTvdySfAnOaarO5bEeuq5UjeT0SsomZz+4TdZzCvR2zXRYnoXz5emGpgO/UPseGcgn4OlrWkSyqW6a54c/qLXUh2jsLM+idWVvEDmzYc+prusrxmGP1MIuuql9LDG8B1U07YwfSUJwTqG3xD86N6dL/VYwX+ZzlURkwLnBAkDKoRi7qmuDzXg04WUccIBAe4IjLCrTFAQYgeQBzRcBcOehNrBZenJ5MWL23hvH/wXA5Ch2Vv0BUyWQgOorEjAoSKqv04w9xiPdylLnJGzlgS2OJvfsHAgMBAAECggEAIbKmq/wQbDXjjQebPZGJBgCBZdYhGYURPFr9FPiz64Q093SGejI0v+XXs7F8D2u45dQ7942c6UsiomBeAXHwanqa6x9djBdmDcJoPcwv5l593mcY11HNk5oygDNTycWZ7iValAB3JZYlmcjy8+5v4KX5sNb6NLkaRzphs/SOd341CrPS9pqlXaYPnXd2r+vOMXXA3mfClUc1KCJgSd7tkWb1oMClqLFTyKy6y8SPryhNk4d8RfZXFf6Vv3IzqUjMhdM6k+KKr85xcT7Vh55ZKMk5SMqfL7+cRTSb9hsFyUcgUNthi+/z7YEf9/tlXSC7pJKoZDdviF4x2ndjC7BxeQKBgQDb6lllJKRXsM0pIYVH6ijUuYwx3cvabVSHbBpDB6ByJ27Mu7KEh4/9mSSwhKiB5x8mVpBSqkd2K111c80OeaBGmmSDudgzeeD4xXDPySco13ruI/4/S6Je2paUcLA9jLpgbDg7TDG0gAUPglOmvLtERlAToSm3O+arM8AhgJfYDQKBgQDPPcZhM2s2dnQ+Bfki8aCY5I6g5Q4v+3iHgw+sHHUJF9gzXqvM6F3Nrtv4K+zZnZl43fPufRfsdo47smc4Sm5VraPJLtLallrT2o0b7xxtbqX9TD4uOBP04pMnVwjWJNwlWg4mA9xDoi0YdmMd4+Sqrh2RRfUzDV2ePIhWbGGmYwKBgQCmo4Fxvr45JGORkzDnOWwPJToyXxqlPMscgrGzsQfG+FJhGKlA+2gEhhLjHuKA7J0R8+4Qda8Iv/tlkIYFPexRE5SurBhDiUsLtrCxAn+F1yHzaZqyY6F5trD7chhZy1U+00IYzLetSoSLJ/ozEjfkmFL9TNXLXrx/ZgtkV4i30QKBgGtFe6Emn3FSgTr1lo7q6YkajWVlCyH1Fd93DK63EDGDwSpd76miAaQMxdnAytmdxSoWIUSqRa7lcxwdbWZqyI/a0HO7o/L3bs5IS1BRRbKzWoIxa+I6rsHMYgUz/OPzsmw3ICm3R9VOZe9KoGWBaASdGbrvrksduP7GMIAjm6wnAoGBALqkCh8J8wbhJx4UpIdfW3ofxCkKa0PYhjXEdSK3dDkaAMkReW0xBn9F2PAWF9Rx+CtTjtJfOeNGVvnG+l98ZqjFFu4KEp2FU1AbW8ziS8eWewjDWPpRAZdULa/SSgPLATB9AdUdwea/YCzZn6VuFOkcVXZrgk1bl5d3D1onXoTJ"
        val validAuthorizationHeader = AuthorizationHeaderGenerator().generateHeader(
            "G2B_L1T",
            JWSAlgorithm.RS256,
            SecretUtils.getPrivateKey(privateKey)
        )

        mockMvc
            .perform(
                get("/resource/test")
                    .header("authorization", validAuthorizationHeader)
            )
            .andExpect(MockMvcResultMatchers.status().isOk)
    }

    @Test
    fun `should_restrict_access_when_endpoint_is_called_with_invalid_authorization_header`() {
        mockMvc
            .perform(
                get("/resource/test")
                    .header("authorization", "invalid-authorization-header")
            )
            .andExpect(MockMvcResultMatchers.status().isUnauthorized)
            .andExpect(MockMvcResultMatchers.content().string("Invalid Authorization Header"))
    }

    @Test
    fun `should_restrict_access_when_endpoint_is_called_with_expired_authorization_header`() {

        val expiredAuthHeader =
            "G2B_L1T.1657736955.RS256.74d3bce3-0f54-42ad-ac84-cb80ee8d56aa.BwO2mfQ-ZL45jZHH2mvoJox6KUh6VWvc_H0uWyBjDky5u1V308KFDXm8hJeg6d9GQkqEsAJ_pXiRlzNOR3IFFit8M3j-I4xYmJufv0G7raMCS-XMejqLvV9AvFB338lXWm4eUk8inrWcyez2BmshxyixQaVFwjSQhRItzo19MuYn3u0fiV0DyXdq9VGAde1CuzBg1xFaGl7_li9GWoI3Ts0azM1siCtkdkepToWeR8JtGtyADvQo3d2_9ooDsrp5P3mh8SW7yD8doWzNklxWAX0iXoIEVGKrzHOx492ibb_O75-_wiri7LyWuiTYPuUKRsWEJ-xwGR7IxqamUgyo3A"

        mockMvc
            .perform(
                get("/resource/test")
                    .header("authorization", expiredAuthHeader)
            )
            .andExpect(MockMvcResultMatchers.status().isUnauthorized)
            .andExpect(MockMvcResultMatchers.content().string("Invalid Authorization Header"))
    }
}

@Profile("test")
@RestController
internal class MockController {
    @GetMapping("/api/test")
    fun unsecuredMethod() = Unit

    @GetMapping("/resource/test")
    fun securedMethod() = Unit
}
